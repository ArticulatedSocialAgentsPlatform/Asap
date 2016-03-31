/*******************************************************************************
 *******************************************************************************/
package asap.ipaacaiuadapters;

import ipaaca.AbstractIU;
import ipaaca.HandlerFunctor;
import ipaaca.IUEventHandler;
import ipaaca.IUEventType;
import ipaaca.Initializer;
import ipaaca.InputBuffer;
import ipaaca.OutputBuffer;
import ipaaca.util.ComponentNotifier;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import saiba.bml.core.BehaviourBlock;
import saiba.bml.feedback.BMLFeedback;
import asap.bml.ext.bmla.feedback.BMLABlockPredictionFeedback;
import asap.bml.ext.bmla.feedback.BMLABlockProgressFeedback;
import asap.bml.ext.bmla.feedback.BMLAFeedbackParser;
import asap.bml.ext.bmla.feedback.BMLAPredictionFeedback;
import asap.bml.ext.bmla.feedback.BMLASyncPointProgressFeedback;
import asap.realizerport.BMLFeedbackListener;
import asap.realizerport.RealizerPort;

import com.google.common.collect.ImmutableSet;

/**
 * New bridge between ipaaca and ASAPrealizer, unifying request and feedback
 * into persistent IUs. Replacement for initial ipaaca adapter by Herwin.
 * Assumes that the connected realizerport is threadsafe (or at least that
 * its performBML function is).
 * @authors Ramin, Hendrik, Herwin
 */
public class IpaacaToBMLRealizerAdapter implements BMLFeedbackListener
{
    static
    {
        Initializer.initializeIpaacaRsb();
    }
    
    private final InputBuffer inBuffer = new InputBuffer(
    	"IpaacaToBMLRealizerAdapter", 
    	ImmutableSet.of(IpaacaBMLConstants.REALIZER_REQUEST_CATEGORY));
    private final OutputBuffer outBuffer = new OutputBuffer(
    	"IpaacaToBMLRealizerAdapter");
    private final RealizerPort realizerPort;
    private long nextUsedAutoBmlId = 1;
    private HashMap<String, String> bmlIdToIuIdMap;
    private HashMap<String, String> iuIdToBmlIdMap;
    
    public synchronized String autoGenerateBmlId(String prefix)
    {
    	return prefix+"_"+String.valueOf(nextUsedAutoBmlId++);
    }
    
	public String getExceptionStackTrace(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		return sw.toString();
	}

    public IpaacaToBMLRealizerAdapter(RealizerPort port)
    {
    	this.bmlIdToIuIdMap = new HashMap<String, String>();
    	this.iuIdToBmlIdMap = new HashMap<String, String>();
        this.realizerPort = port;        
        realizerPort.addListeners(this);
        EnumSet<IUEventType> types = EnumSet.of(IUEventType.ADDED, IUEventType.MESSAGE);
        EnumSet<IUEventType> updated_types = EnumSet.of(IUEventType.UPDATED);
        inBuffer.registerHandler(new IUEventHandler(new HandlerFunctor()
        {
            @Override
            public void handle(AbstractIU iu, IUEventType type, boolean local)
            {
				synchronized (this) {
					boolean has_errors = false;
					String error_string = "unspecified";
					HashMap<String, String> items = new HashMap<String, String>();
					String requested_bml = "";
					
					String requestType = iu.getPayload().get(IpaacaBMLConstants.REALIZER_REQUEST_TYPE_KEY);
					
					if (requestType.equals(IpaacaBMLConstants.REALIZER_REQUEST_TYPE_BML)) {
						// bml specified in payload key 'request'
						requested_bml = iu.getPayload().get(IpaacaBMLConstants.REALIZER_REQUEST_KEY);
					} else if (requestType.equals(IpaacaBMLConstants.REALIZER_REQUEST_TYPE_BMLFILE)) {
						// path to bml file specified in 'request'
						String requested_bml_file = iu.getPayload().get(IpaacaBMLConstants.REALIZER_REQUEST_KEY);
						try {
							Map<String, Object> attr = Files.readAttributes(Paths.get(requested_bml_file), "size");
							if (((long) attr.get("size")) > 4 * 1024 * 1024) {
								has_errors = true;
								error_string = "Specified file is too large (>4 MiB)";
							} else {
								byte[] encoded = Files.readAllBytes(Paths.get(requested_bml_file));
								//requested_bml = new String(encoded, Charset.defaultCharset());
								requested_bml = new String(encoded, StandardCharsets.UTF_8);
							}
						} catch (Exception e) {
							has_errors = true;
							error_string = "Exception during file open: "+getExceptionStackTrace(e);
						}
					} else {
						has_errors = true;
						error_string = "Unknown request type";
					}
					
					if (has_errors) {
						items.put(IpaacaBMLConstants.IU_STATUS_KEY, "ERROR");
						items.put(IpaacaBMLConstants.IU_ERROR_KEY, error_string);
					} else {
						BehaviourBlock bb = new BehaviourBlock();
						try {
							bb.readXML(requested_bml);
							String actualBmlId = bb.getBmlId();
							if (actualBmlId.equals("AUTO")) {
								String[] path_elements = iu.getOwnerName().split("/");
								String prefix = "autogen";
								if (path_elements.length > 1) {
									prefix = path_elements[path_elements.length - 2];
								}
								bb.setBmlId(autoGenerateBmlId(prefix));
								actualBmlId = bb.getBmlId();
							}
							if (type == IUEventType.ADDED) {
								// only do bookkeeping for IUs proper (not Messages)
								bmlIdToIuIdMap.put(actualBmlId, iu.getUid());
								iuIdToBmlIdMap.put(iu.getUid(), actualBmlId);
							}
							String actualBmlString = bb.toXMLString();
							realizerPort.performBML(actualBmlString);
							items.put(IpaacaBMLConstants.BML_ID_KEY, actualBmlId);
							items.put(IpaacaBMLConstants.IU_STATUS_KEY, "((RECEIVED))");
							if (! requestType.equals(IpaacaBMLConstants.REALIZER_REQUEST_TYPE_BMLFILE)) {
								// send the actual (amended) bml source, but don't do this
								// if opening a (potentially large) bml file was requested
								// (Note: maybe use a dedicated key after all? 'request'
								//  should possibly be the request as provided before, verbatim... ?)
								items.put(IpaacaBMLConstants.REALIZER_REQUEST_KEY, actualBmlString);
							}
						} catch (Exception e) {
							error_string = "Exception during BML parse or perform: "+getExceptionStackTrace(e);
							items.put(IpaacaBMLConstants.IU_STATUS_KEY, "ERROR");
							items.put(IpaacaBMLConstants.IU_ERROR_KEY, error_string);
						}
					}
					if (type == IUEventType.ADDED) {
						// only give feedback for real IUs
						try {
							iu.getPayload().merge(items);
						} catch (ipaaca.IUUpdateFailedException ex) {
							//
						}
					}
				}
           	}
        }, types, ImmutableSet.of(IpaacaBMLConstants.REALIZER_REQUEST_CATEGORY)));
        
        ComponentNotifier notifier = new ComponentNotifier(
        		"IpaacaToBMLRealizerAdapter",
        		"bmlrealizer",
                ImmutableSet.of(IpaacaBMLConstants.REALIZER_FEEDBACK_CATEGORY),
                ImmutableSet.of(IpaacaBMLConstants.REALIZER_REQUEST_CATEGORY),
                outBuffer, 
                inBuffer);
        notifier.initialize();
    }

    @Override
    public void feedback(String feedback)
    {
		synchronized (this) {
			HashMap<String, String> new_payload_items = new HashMap<String, String>();
			BMLFeedback fb;
			try
			{
				fb = BMLAFeedbackParser.parseFeedback(feedback);
			}
			catch (IOException e)
			{
				// shouldn't happen since we parse strings
				throw new AssertionError(e);
			}
			if (fb instanceof BMLABlockProgressFeedback)
			{
				AbstractIU iu_to_update = null;
				BMLABlockProgressFeedback fbBlock = (BMLABlockProgressFeedback) fb;
				if (bmlIdToIuIdMap.containsKey(fbBlock.getBmlId())) {
					iu_to_update = inBuffer.getIU(bmlIdToIuIdMap.get(fbBlock.getBmlId()));
					if (fbBlock.getSyncId().equals("end"))
					{
						new_payload_items.put(IpaacaBMLConstants.IU_STATUS_KEY, "DONE");
					}
					else if (fbBlock.getSyncId().equals("start"))
					{
						new_payload_items.put(IpaacaBMLConstants.IU_STATUS_KEY, "IN_EXEC");
						new_payload_items.put(IpaacaBMLConstants.LAST_SYNC_ID_KEY, fbBlock.getSyncId());
					}
				} else {
					// ignore this BML block: it was not added by us
				}
				if (iu_to_update != null) {
					try{
						iu_to_update.getPayload().merge(new_payload_items);
					} catch (ipaaca.IUUpdateFailedException ex) {
						//
					}
				}
			}
			else if (fb instanceof BMLAPredictionFeedback)
			{
				BMLAPredictionFeedback pf = (BMLAPredictionFeedback) fb;
				// FIXME: no clue how to obtain the correct bml ID here...
				//String bmlid = pf.getBmlBehaviorPredictions().get(0).getBmlId();
				//iu_to_update = inBuffer.getIU(bmlIdToIuIdMap.get(bmlid));
				for (BMLABlockPredictionFeedback bbp : pf.getBMLABlockPredictions())
				{
					if (bmlIdToIuIdMap.containsKey(bbp.getId())) {
						AbstractIU iu_to_update = inBuffer.getIU(bmlIdToIuIdMap.get(bbp.getId()));
						double start = bbp.getPosixStartTime() / 1000.0;
						double end = bbp.getPosixEndTime() / 1000.0;
						new_payload_items.put(IpaacaBMLConstants.IU_PREDICTED_START_TIME_KEY, Double.valueOf(start).toString()); 
						new_payload_items.put(IpaacaBMLConstants.IU_PREDICTED_END_TIME_KEY, Double.valueOf(end).toString());
						if (iu_to_update != null) {
							try{
								iu_to_update.getPayload().merge(new_payload_items);
							} catch (ipaaca.IUUpdateFailedException ex) {
								//
							}
						}
					} else {
						// ignore this BML block: it was not added by us
					}
				}
			}
			else if (fb instanceof BMLASyncPointProgressFeedback)
			{
				BMLASyncPointProgressFeedback sppf = (BMLASyncPointProgressFeedback) fb;
				// cancel if sync point is "start" (already sent in status change, above)
				if (sppf.getSyncId().equals("start")) return;
				if (bmlIdToIuIdMap.containsKey(sppf.getBMLId())) {
					AbstractIU iu_to_update = inBuffer.getIU(bmlIdToIuIdMap.get(sppf.getBMLId()));
					new_payload_items.put(IpaacaBMLConstants.LAST_SYNC_ID_KEY, sppf.getSyncId());
					if (iu_to_update != null) {
						try{
							iu_to_update.getPayload().merge(new_payload_items);
						} catch (ipaaca.IUUpdateFailedException ex) {
							//
						}
					}
				} else {
					// ignore this BML block: it was not added by us
				}
			}
		}
	}

    public void close()
    {
        outBuffer.close();
        inBuffer.close();
    }

}

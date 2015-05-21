/*******************************************************************************
 *******************************************************************************/
package asap.ipaacaiuadapters;

import ipaaca.AbstractIU;
import ipaaca.HandlerFunctor;
import ipaaca.IUEventHandler;
import ipaaca.IUEventType;
import ipaaca.Initializer;
import ipaaca.InputBuffer;
import ipaaca.LocalIU;
import ipaaca.OutputBuffer;
import ipaaca.util.ComponentNotifier;

import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Locale;

import saiba.bml.core.BehaviourBlock;
import saiba.bml.feedback.BMLBlockPredictionFeedback;
import saiba.bml.feedback.BMLBlockProgressFeedback;
import saiba.bml.feedback.BMLFeedback;
import saiba.bml.feedback.BMLFeedbackParser;
import saiba.bml.feedback.BMLPredictionFeedback;
import asap.realizerport.BMLFeedbackListener;
import asap.realizerport.RealizerPort;

import com.google.common.collect.ImmutableSet;

/**
 * New bridge between ipaaca and ASAPrealizer, unifying request and feedback into persistent IUs.
 * Replacement for initial ipaaca adapter by Herwin.
 * Assumes that the connected realizerport is threadsafe (or at least that its performBML function is).
 * @authors Ramin, Hendrik, Herwin
 */
public class IpaacaToBMLRealizerAdapter implements BMLFeedbackListener
{
    static double initTime =  System.currentTimeMillis() / 1000.0;
    static
    {
        //final double initTime =  System.nanoTime() / 1000000000L;
        Initializer.initializeIpaacaRsb();
    }
    

    //private double initTime;
    private final InputBuffer inBuffer = new InputBuffer("IpaacaToBMLRealizerAdapter", ImmutableSet.of("timesyncRequest", IpaacaBMLConstants.REALIZER_REQUEST_CATEGORY));
    private final OutputBuffer outBuffer = new OutputBuffer("IpaacaToBMLRealizerAdapter");
    private final RealizerPort realizerPort;
    private long nextUsedAutoBmlId = 1;
    private HashMap<String, String> bmlIdToIuIdMap;
    private HashMap<String, String> iuIdToBmlIdMap;
    
    public synchronized String autoGenerateBmlId(String prefix)
    {
    	return prefix+"_"+String.valueOf(nextUsedAutoBmlId++);
    }
    
    public IpaacaToBMLRealizerAdapter(RealizerPort port)
    {
    	this.bmlIdToIuIdMap = new HashMap<String, String>();
    	this.iuIdToBmlIdMap = new HashMap<String, String>();
        this.realizerPort = port;        
        realizerPort.addListeners(this);
        EnumSet<IUEventType> types = EnumSet.of(IUEventType.ADDED);
        EnumSet<IUEventType> updated_types = EnumSet.of(IUEventType.UPDATED);
        inBuffer.registerHandler(new IUEventHandler(new HandlerFunctor()
        {
            @Override
            public void handle(AbstractIU iu, IUEventType type, boolean local)
            {
				synchronized (this) {
					BehaviourBlock bb = new BehaviourBlock();
					String requested_bml = iu.getPayload().get(IpaacaBMLConstants.REALIZER_REQUEST_KEY);
					System.out.println("requested bml {{{ "+requested_bml+" }}}");
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
					bmlIdToIuIdMap.put(actualBmlId, iu.getUid());
					iuIdToBmlIdMap.put(iu.getUid(), actualBmlId);
					String actualBmlString = bb.toXMLString();
					realizerPort.performBML(actualBmlString);
					HashMap<String, String> items = new HashMap<String, String>();
					items.put(IpaacaBMLConstants.REALIZER_REQUEST_KEY, actualBmlString);
					items.put(IpaacaBMLConstants.BML_ID_KEY, actualBmlId);
					items.put(IpaacaBMLConstants.IU_STATUS_KEY, "((RECEIVED))");
					try{
						System.out.println("request status update");
						iu.getPayload().merge(items);
						System.out.println("OK status update");
					} catch (ipaaca.IUUpdateFailedException ex) {
						System.out.println("FAILED status update");
						//
					}
				}
           	}
        }, types, ImmutableSet.of(IpaacaBMLConstants.REALIZER_REQUEST_CATEGORY)));
        
        // HACK by Ramin
        // FIXME: this is a minimal reimplementation of the Component timesync code (slave part) from Python ipaaca.util - not yet ported to Java
        inBuffer.registerHandler(new IUEventHandler(new HandlerFunctor()
        {
            @Override
            public void handle(AbstractIU iu, IUEventType type, boolean local)
            {
				synchronized (this) {
					String master = iu.getPayload().get("master");
					String master_t1 = iu.getPayload().get("master_t1");
					String stage = iu.getPayload().get("stage");
					
					if (stage.equals("0")) {
						LocalIU timingIU = new LocalIU();
						timingIU.setCategory("timesyncReply");
						double t1 = System.currentTimeMillis() / 1000.0;
						//HashMap<String, String> items = new HashMap<String, String>();
						timingIU.getPayload().put("master", master);
						timingIU.getPayload().put("master_t1", master_t1);
						timingIU.getPayload().put("slave", "ASAPRealizer");
						timingIU.getPayload().put("slave_t1", Double.valueOf(t1).toString());
						timingIU.getPayload().put("stage", "1");
						//timingIU.getPayload().putAll(items);
						outBuffer.add(timingIU);
					}
				}
           	}
        }, types, ImmutableSet.of("timesyncRequest")));
        outBuffer.registerHandler(new IUEventHandler(new HandlerFunctor()
        {
            @Override
            public void handle(AbstractIU iu, IUEventType type, boolean local)
            {
				synchronized (this) {
					String master = iu.getPayload().get("master");
					String master_t1 = iu.getPayload().get("master_t1");
					String stage = iu.getPayload().get("stage");
					if (stage.equals("2")) {
						double t2 = System.currentTimeMillis() / 1000.0;
						HashMap<String, String> items = new HashMap<String, String>();
						items.put("slave_t2", Double.valueOf(t2).toString());
						items.put("stage", "3");
						iu.getPayload().merge(items);
					} else if (stage.equals("4")) {
						double latency = Double.parseDouble(iu.getPayload().get("latency"));
						double offset = Double.parseDouble(iu.getPayload().get("offset"));
						System.out.println("Master "+master+" determined our timing info: round-trip time: "+latency+"  clock offset: "+offset);
					}
				}
           	}
        }, updated_types, ImmutableSet.of("timesyncReply")));
        // END hack
        
        ComponentNotifier notifier = new ComponentNotifier("IpaacaToBMLRealizerAdapter", "bmlrealizer",
                ImmutableSet.of(IpaacaBMLConstants.REALIZER_FEEDBACK_CATEGORY),ImmutableSet.of(IpaacaBMLConstants.REALIZER_REQUEST_CATEGORY),
                outBuffer, inBuffer);
        notifier.initialize();
    }

    @Override
    public void feedback(String feedback)
    {
		synchronized (this) {
			HashMap<String, String> new_payload_items = new HashMap<String, String>();    	
			/*LocalMessageIU feedbackIU = new LocalMessageIU();
			feedbackIU.setCategory(IpaacaBMLConstants.BML_FEEDBACK_CATEGORY);
			feedbackIU.getPayload().put(IpaacaBMLConstants.BML_FEEDBACK_KEY, feedback);
			outBuffer.add(feedbackIU);
			*/
			BMLFeedback fb;
			try
			{
				fb = BMLFeedbackParser.parseFeedback(feedback);
			}
			catch (IOException e)
			{
				// shouldn't happen since we parse strings
				throw new AssertionError(e);
			}
			if (fb instanceof BMLBlockProgressFeedback)
			{
				AbstractIU iu_to_update = null;
				BMLBlockProgressFeedback fbBlock = (BMLBlockProgressFeedback) fb;
				if (bmlIdToIuIdMap.containsKey(fbBlock.getBmlId())) {
					iu_to_update = inBuffer.getIU(bmlIdToIuIdMap.get(fbBlock.getBmlId()));
					if (fbBlock.getSyncId().equals("end"))
					{
						new_payload_items.put(IpaacaBMLConstants.IU_STATUS_KEY, "DONE");
					}
					else if (fbBlock.getSyncId().equals("start"))
					{
						new_payload_items.put(IpaacaBMLConstants.IU_STATUS_KEY, "IN_EXEC");
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
			else if (fb instanceof BMLPredictionFeedback)
			{
				BMLPredictionFeedback pf = (BMLPredictionFeedback) fb;
				// FIXME: no clue how to obtain the correct bml ID here...
				//String bmlid = pf.getBmlBehaviorPredictions().get(0).getBmlId();
				//iu_to_update = inBuffer.getIU(bmlIdToIuIdMap.get(bmlid));
				double latestPredictedEndTime = -1;
				double earliestPredictedStartTime = 9999999999999999999999999.9;
				for (BMLBlockPredictionFeedback bbp : pf.getBmlBlockPredictions())
				{
					if (bmlIdToIuIdMap.containsKey(bbp.getId())) {
						AbstractIU iu_to_update = inBuffer.getIU(bmlIdToIuIdMap.get(bbp.getId()));            	
						//System.out.println("id="+id);
						double start = bbp.getGlobalStart() + initTime;
						double end = bbp.getGlobalEnd() + initTime;
						if (end>latestPredictedEndTime) { latestPredictedEndTime = end; }
						if (start<earliestPredictedStartTime) { earliestPredictedStartTime = start; }
						new_payload_items.put(IpaacaBMLConstants.IU_PREDICTED_START_TIME_KEY, Double.valueOf(start).toString()); 
						new_payload_items.put(IpaacaBMLConstants.IU_PREDICTED_END_TIME_KEY, Double.valueOf(end).toString());
						double t = System.currentTimeMillis()/1000.0;
						System.out.println("SEND TIME: " + String.format(Locale.ENGLISH, "%.3f", t));
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
				// TODO: also send a global IU/Message with the latest known end time?
			}
		}
	}

    public void close()
    {
        outBuffer.close();
        inBuffer.close();
    }
}

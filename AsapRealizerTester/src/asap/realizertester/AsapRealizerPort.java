package asap.realizertester;

import java.util.HashSet;
import java.util.Set;

import asap.bml.bridge.RealizerPort;
import bml.realizertestport.BMLExceptionListener;
import bml.realizertestport.BMLFeedbackListener;
import bml.realizertestport.BMLWarningListener;
import bml.realizertestport.RealizerTestPort;

public class AsapRealizerPort implements RealizerTestPort
{
    private final RealizerPort realizerPort;

    public AsapRealizerPort(RealizerPort port)
    {
        realizerPort = port;        
    }

    @Override
    public void performBML(String bmlString)
    {
        realizerPort.performBML(bmlString);
    }

    @Override
    public void addBMLWarningListener(final BMLWarningListener l)
    {
        realizerPort.addListeners(new asap.bml.feedback.BMLWarningListener()
        {
            @Override
            public void warn(saiba.bml.feedback.BMLWarningFeedback warn)
            {
                Set<String>failedBehaviours = new HashSet<String>();
                String idSplit[] = warn.getId().split(":");
                if(idSplit.length==2)
                {
                    failedBehaviours.add(idSplit[1]);
                }
                l.warn(new bml.realizertestport.BMLWarningFeedback(warn.getType(), warn.getId(), 0, failedBehaviours, new HashSet<String>(), 
                        warn.getDescription()));
            }
        });
    }

    @Override
    public void addBMLFeedbackListener(final BMLFeedbackListener l)
    {
        realizerPort.addListeners(new asap.bml.feedback.BMLFeedbackListener()
        {
            @Override
            public void blockProgress(saiba.bml.feedback.BMLBlockProgressFeedback fb)
            {
                if(fb.getSyncId().equals("start"))
                {
                l.performanceStart(new bml.realizertestport.BMLPerformanceStartFeedback(fb.getCharacterId(),fb.getBmlId(),fb.getGlobalTime(),
                        fb.getGlobalTime()));
                }
                else if (fb.getSyncId().equals("end"))
                {
                    l.performanceStop(new bml.realizertestport.BMLPerformanceStopFeedback(fb.getCharacterId(),fb.getBmlId(),"",fb.getGlobalTime()));
                }
            }

            @Override
            public void syncProgress(saiba.bml.feedback.BMLSyncPointProgressFeedback fb)
            {
                l.syncProgress(new bml.realizertestport.BMLSyncPointProgressFeedback(fb.getCharacterId(), 
                        fb.getBMLId(), fb.getBehaviourId(), fb.getSyncId(), fb.getTime(), fb.getGlobalTime()));
            }
        });
    }

    @Override
    public void addBMLExceptionListener(final BMLExceptionListener l)
    {
        realizerPort.addListeners(new asap.bml.feedback.BMLWarningListener()
        {
            @Override            
            public void warn(saiba.bml.feedback.BMLWarningFeedback warn)
            {
                Set<String>failedBehaviours = new HashSet<String>();
                String idSplit[] = warn.getId().split(":");
                if(idSplit.length==2)
                {
                    failedBehaviours.add(idSplit[1]);
                }
                l.exception(new bml.realizertestport.BMLExceptionFeedback(warn.getType(), warn.getId(), 0, failedBehaviours, new HashSet<String>(), 
                        warn.getDescription(),false));
            }
        });
        
    }
}

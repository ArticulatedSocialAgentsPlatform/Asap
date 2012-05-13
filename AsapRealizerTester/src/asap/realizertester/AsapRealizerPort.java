package asap.realizertester;

import saiba.bml.bridge.RealizerPort;
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
    public void addBMLExceptionListener(final BMLExceptionListener l)
    {
        realizerPort.addListeners(new saiba.bml.feedback.BMLExceptionListener()
        {
            @Override
            public void exception(saiba.bml.feedback.BMLExceptionFeedback ex)
            {
                l.exception(new bml.realizertestport.BMLExceptionFeedback(ex.getCharacterId(), ex.bmlId, ex.timeStamp, ex.failedBehaviours,
                        ex.failedConstraints, ex.exceptionText, ex.performanceFailed));
            }
        });
    }

    @Override
    public void addBMLWarningListener(final BMLWarningListener l)
    {
        realizerPort.addListeners(new saiba.bml.feedback.BMLWarningListener()
        {
            @Override
            public void warn(saiba.bml.feedback.BMLWarningFeedback warn)
            {
                l.warn(new bml.realizertestport.BMLWarningFeedback(warn.getCharacterId(), warn.bmlId, warn.timeStamp,
                        warn.modifiedBehaviours, warn.modifiedConstraints, warn.warningText));
            }
        });
    }

    @Override
    public void addBMLFeedbackListener(final BMLFeedbackListener l)
    {
        realizerPort.addListeners(new saiba.bml.feedback.BMLFeedbackListener()
        {
            @Override
            public void performanceStart(saiba.bml.feedback.BMLPerformanceStartFeedback fb)
            {
                l.performanceStart(new bml.realizertestport.BMLPerformanceStartFeedback(fb.characterId,fb.bmlId,fb.timeStamp,fb.predictedEnd));
            }

            @Override
            public void performanceStop(saiba.bml.feedback.BMLPerformanceStopFeedback fb)
            {
                l.performanceStop(new bml.realizertestport.BMLPerformanceStopFeedback(fb.characterId,fb.bmlId,fb.reason,fb.timeStamp));
            }

            @Override
            public void syncProgress(saiba.bml.feedback.BMLSyncPointProgressFeedback fb)
            {
                l.syncProgress(new bml.realizertestport.BMLSyncPointProgressFeedback(fb.getCharacterId(), 
                        fb.bmlId, fb.behaviorId, fb.syncId, fb.bmlBlockTime, fb.timeStamp));
            }
        });
    }
}

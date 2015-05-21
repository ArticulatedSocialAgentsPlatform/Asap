/*******************************************************************************
 *******************************************************************************/
package asap.realizer.planunit;


import java.util.Collection;

import asap.realizer.feedback.FeedbackManager;

import com.google.common.collect.ImmutableCollection;

/**
 * Plays/stops a timedplanunit at time t, should start it if needed. 
 * Gathers all playing/stopping exceptions. They can be queried using getTimedPlanUnitPlayException/getTimedPlanUnitStopException.
 * Handled exceptions are removed using clearPlayExceptions/clearStopExceptions.
 * @author welberge
 */
public interface TimedPlanUnitPlayer
{
    void playUnit(TimedPlanUnit su, double t);
    void stopUnit(TimedPlanUnit su, double t);
    ImmutableCollection<TimedPlanUnitPlayException> getPlayExceptions();
    void clearPlayExceptions(Collection<TimedPlanUnitPlayException> removeExceptions);
    ImmutableCollection<TimedPlanUnitPlayException> getStopExceptions();
    void clearStopExceptions(Collection<TimedPlanUnitPlayException> removeExceptions);
    void handleStopExceptions(double t);
    void handlePlayExceptions(double t, FeedbackManager fbManager);
}

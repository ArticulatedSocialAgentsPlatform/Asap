/*******************************************************************************
 *******************************************************************************/
package asap.math;

import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import com.google.common.primitives.Doubles;

/**
 * Solves the timing of several points, given the preferred duration between these points, the importance of maintaining each
 * preferred duration and the (fixed) timing of a subset of the points.
 * @author hvanwelbergen
 */
public final class LinearStretchTemporalResolver
{
    private LinearStretchTemporalResolver()
    {
    }

    public static final double TIME_UNKNOWN = Double.MAX_VALUE;

    private static void solveInterval(int startIndex, int endIndex, double[] times, double[] prefDurations, double[] weights)
    {
        double intervalDuration = times[endIndex] - times[startIndex];
        double preferedDuration = getPreferedDuration(startIndex, endIndex, prefDurations);

        if (preferedDuration < intervalDuration)
        {
            stretchInterval(startIndex, endIndex, times, prefDurations, weights, intervalDuration);
        }
        else
        {
            skewInterval(startIndex, endIndex, times, prefDurations, weights, intervalDuration);
        }
    }

    private static void skewInterval(int startIndex, int endIndex, double[] times, double[] prefDurations, double[] weights,
            double intervalDuration)
    {
        // first skew intervals with the highest weight
        List<Double> weightsList = Doubles.asList(weights);
        TreeSet<Double> sortedWeights = new TreeSet<>(weightsList);

        for (double currentMaxWeight : sortedWeights.descendingSet())
        {
            double stretchPartDur = 0;
            double nonStretchPartDur = 0;

            for (int i = startIndex; i < endIndex; i++)
            {
                if (weights[i] == currentMaxWeight)
                {
                    stretchPartDur += prefDurations[i];
                }
                else if (weights[i] > currentMaxWeight)
                {
                    nonStretchPartDur += times[i + 1] - times[i];
                }
                else
                {
                    nonStretchPartDur += prefDurations[i];
                }
            }

            if (intervalDuration - nonStretchPartDur >= 0)
            {
                double scale = (intervalDuration - nonStretchPartDur) / stretchPartDur;

                for (int i = startIndex; i < endIndex - 1; i++)
                {
                    if (weights[i] == currentMaxWeight)
                    {
                        times[i + 1] = times[i] + prefDurations[i] * scale;
                    }
                    else if (weights[i] < currentMaxWeight)
                    {
                        times[i + 1] = times[i] + prefDurations[i];
                    }
                }
                break;
            }
            else
            {
                for (int i = startIndex; i < endIndex - 1; i++)
                {
                    if (weights[i] == currentMaxWeight)
                    {
                        times[i + 1] = times[i];
                    }
                }
            }
        }
    }

    /**
     * Equally stretch intervals with the highest weight only
     */
    private static void stretchInterval(int startIndex, int endIndex, double[] times, double[] prefDurations, double[] weights,
            double intervalDuration)
    {
        double maxWeight = Doubles.max(Arrays.copyOfRange(weights, startIndex, endIndex));
        double stretchPartDur = 0;
        double nonStretchPartDur = 0;

        int numPartsStretch = 0;
        for (int i = startIndex; i < endIndex; i++)
        {
            if (weights[i] == maxWeight)
            {
                stretchPartDur += prefDurations[i];
                numPartsStretch++;
            }
            else
            {
                nonStretchPartDur += prefDurations[i];
            }
        }
        if (stretchPartDur > 0)
        {
            scaledStretch(startIndex, endIndex, times, prefDurations, weights, intervalDuration, maxWeight, stretchPartDur,
                    nonStretchPartDur);
        }
        else
        {
            equidistantStretch(startIndex, endIndex, times, prefDurations, weights, intervalDuration, maxWeight, 
                    nonStretchPartDur, numPartsStretch);
        }
    }

    private static void equidistantStretch(int startIndex, int endIndex, double[] times, double[] prefDurations, double[] weights,
            double intervalDuration, double maxWeight, double nonStretchPartDur, int numPartsStretch)
    {
        double partDuration = (intervalDuration - nonStretchPartDur) / (double) numPartsStretch;

        for (int i = startIndex; i < endIndex - 1; i++)
        {
            if (weights[i] == maxWeight)
            {
                times[i + 1] = times[i] + partDuration;
            }
            else
            {
                times[i + 1] = times[i] + prefDurations[i];
            }
        }
    }

    private static void scaledStretch(int startIndex, int endIndex, double[] times, double[] prefDurations, double[] weights,
            double intervalDuration, double maxWeight, double stretchPartDur, double nonStretchPartDur)
    {
        double scale = (intervalDuration - nonStretchPartDur) / stretchPartDur;

        for (int i = startIndex; i < endIndex - 1; i++)
        {
            if (weights[i] == maxWeight)
            {
                times[i + 1] = times[i] + prefDurations[i] * scale;
            }
            else
            {
                times[i + 1] = times[i] + prefDurations[i];
            }
        }
    }

    private static void fillToRightWithPrefDurations(double[] times, double[] prefDurations, int start)
    {
        double currentTime = times[start];
        for (int i = start + 1; i < times.length; i++)
        {
            currentTime += prefDurations[i - 1];
            times[i] = currentTime;
        }
    }

    private static void fillToLeftWithPrefDurations(double[] times, double[] prefDurations, int start)
    {
        double currentTime = times[start];
        for (int i = start - 1; i >= 0; i--)
        {
            currentTime -= prefDurations[i];
            times[i] = currentTime;
        }
    }

    private static void forwardSolve(double[] times, double[] prefDurations, double[] weights, int currentIndex)
    {
        int endIndex = getFirstSetIndex(times, currentIndex + 1);
        if (endIndex == -1)
        {
            // no more constraints to the right, fill with desired values
            fillToRightWithPrefDurations(times, prefDurations, currentIndex);
        }
        else
        {
            solveInterval(currentIndex, endIndex, times, prefDurations, weights);
            forwardSolve(times, prefDurations, weights, endIndex);
        }
    }

    private static int getFirstSetIndex(double[] times, int start)
    {
        for (int i = start; i < times.length; i++)
        {
            if (times[i] != TIME_UNKNOWN)
            {
                return i;
            }
        }
        return -1;
    }

    private static double getPreferedDuration(int start, int end, double[] prefDurations)
    {
        double prefDur = 0;
        for (int i = start; i < end; i++)
        {
            prefDur += prefDurations[i];
        }
        return prefDur;
    }

    private static void setStart(double[] times, double[] preferedDurations, double[] weights, double minStart)
    {
        int firstIndex = getFirstSetIndex(times, 0);
        if (firstIndex == -1)
        {
            times[0] = minStart;
        }
        else if (firstIndex > 0)
        {
            double prefDur = getPreferedDuration(0, firstIndex, preferedDurations);
            if (times[firstIndex] - prefDur > minStart)
            {
                fillToLeftWithPrefDurations(times, preferedDurations, firstIndex);
            }
            else
            {
                times[0] = minStart;
            }
        }
    }

    /**
     * Solves all times, given prefered durations, scaling weights and a minimum start time
     * @param times, use LinearStretchTemporalResolver.TIME_UNKNOWN for unkown
     * @param preferedDurations ith element is the duration from i to i+1
     * @param weights weight of segment i (the segment from i to i+1). Segments with higher weights get stretched skewed over those with lower weight.
     * @param minStart minimum start time
     */
    public static double[] solve(double[] times, double[] preferedDurations, double[] weights, double minStart)
    {
        double[] solution = Arrays.copyOf(times, times.length);
        if (times.length > 0)
        {
            setStart(solution, preferedDurations, weights, minStart);
            forwardSolve(solution, preferedDurations, weights, 0);
        }
        return solution;
    }
}

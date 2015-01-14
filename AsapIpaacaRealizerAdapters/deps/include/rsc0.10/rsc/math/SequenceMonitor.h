/* ============================================================
 *
 * This file is a part of RSC project
 *
 * Copyright (C) 2011 by Christian Emmerich <cemmeric at techfak dot uni-bielefeld dot de>
 *
 * This file may be licensed under the terms of the
 * GNU Lesser General Public License Version 3 (the ``LGPL''),
 * or (at your option) any later version.
 *
 * Software distributed under the License is distributed
 * on an ``AS IS'' basis, WITHOUT WARRANTY OF ANY KIND, either
 * express or implied. See the LGPL for the specific language
 * governing rights and limitations.
 *
 * You should have received a copy of the LGPL along with this
 * program. If not, go to http://www.gnu.org/licenses/lgpl.html
 * or write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * The development of this software was supported by:
 *   CoR-Lab, Research Institute for Cognition and Robotics
 *     Bielefeld University
 *
 * ============================================================ */

#pragma once

#include <iostream>
#include <sstream>
#include <stdexcept>
#include <cmath>

#include <boost/shared_ptr.hpp>

#include "rsc/rscexports.h"

namespace rsc {
namespace math {

///////////////////////////////////////////////////////////////////////////////////////////
////////////////////////// METRICS ////////////////////////////////////////////////////////

class Metric;
typedef boost::shared_ptr<Metric> MetricPtr;

/**
 * Defines interface for vector metrics providing a calc-method that
 * calculates the metric of two vectors.
 *
 * @author cemmeric
 */
class RSC_EXPORT Metric {
public:

    virtual ~Metric();

    /**
     * Calculates the distance between v1 and v2.
     *
     * @param v1 vector
     * @param v2 another vector
     * @return distance between the two vectors
     */
    virtual double calc(const double* v1, const double* v2,
            const unsigned int& dim) = 0;

};

/**
 * Euclidean distance between two vectors.
 *
 * @author cemmeric
 */
class RSC_EXPORT EuclidDist: public Metric {
public:

    /**
     * Calculates the Euclidean distance between v1 and v2.
     *
     * @param v1 vector
     * @param v2 another vector
     * @return Euclidean distance between the two vectors
     */
    double calc(const double* v1, const double* v2, const unsigned int& dim);

};

/**
 * Euclidean distance between two vectors.
 *
 * @author cemmeric
 */
class RSC_EXPORT MaximumDist: public Metric {
public:

    /**
     * Distance between two vectors is defined as the maximum absolute
     * component-wise value.
     *
     * @param v1 vector
     * @param v2 another vector
     * @return maximum absolute distance between the two vectors
     */
    double calc(const double* v1, const double* v2, const unsigned int& dim);

};

///////////////////////////////////////////////////////////////////////////////////////////
/////////////////////// METRIC CONDITIONS /////////////////////////////////////////////////

class MetricCondition;
typedef boost::shared_ptr<MetricCondition> MetricConditionPtr;

/**
 * Defines a interface for metric conditions. Provides a method is fulfilled.
 *
 * @author cemmeric
 */
class RSC_EXPORT MetricCondition {
public:

    MetricCondition(MetricPtr m);
    virtual ~MetricCondition();

    /**
     * Tests whether the metric condition is fulfilled for two vectors.
     *
     * @param v1 a vector
     * @param v2 another vector
     * @param dim dimension of the two vectors v1 and v2
     * @return true if v1 and v2 fulfill the metric condition
     */
    virtual bool isFulfilled(const double* v1, const double* v2,
            const unsigned int& dim) = 0;

protected:
    const MetricPtr metric;

};

/**
 * The BelowThreshold - condition tests whether a given metric of two vectors
 * stays below a given upper threshold.
 *
 * @author cemmeric
 */
class RSC_EXPORT BelowThreshold: public MetricCondition {
public:

    BelowThreshold(const MetricPtr m, const double threshold);

    /**
     * tests whether two vectors fulfill the BelowUpperThreshold - condition
     * @param v1 a vector
     * @param v2 another vector
     * @param dim dimension of both given vectors
     * @return true, if metric(v1, v2) < threshold
     */
    bool isFulfilled(const double* v1, const double* v2,
            const unsigned int& dim);

protected:
    const double threshold;

};

/**
 * The AboveThreshold - condition tests whether a given metric of two vectors stays
 * above a given threshold.
 *
 * @author cemmeric
 */
class RSC_EXPORT AboveThreshold: public MetricCondition {
public:

    AboveThreshold(const MetricPtr m, const double threshold);

    /**
     * tests whether two vectors fulfill the AboveThreshold - condition
     * @param v1 a vector
     * @param v2 another vector
     * @param dim dimension of both given vectors
     * @return true, if metric(v1, v2) > threshold
     */
    bool isFulfilled(const double* v1, const double* v2,
            const unsigned int& dim);

protected:
    const double threshold;

};

//////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////// SEQUENCE MONITOR //////////////////////////////////////////////////

/**
 * A monitor for (vector-) sequences. Tests whether the difference of
 * consecutive members of a sequence fulfills a certain metric condition for a
 * certain number of steps or not.
 *
 * A simple application could be convergence testing: Test whether the
 * difference between the current vector and the predecessor is less than a
 * certain threshold value. If this holds for a certain number of consecutive
 * sequence members (window size + 1), the sequence is said to be converged.
 *
 * @author cemmeric
 */
class RSC_EXPORT SequenceMonitor {
public:

    /**
     * Constructor.
     *
     * @param dim dimension of the vectors of the sequence
     * @param window number of consecutive members for which the condition
     *               should hold
     * @param condition the condition that should be fulfilled for consecutive
     *                  sequence members
     */
    SequenceMonitor(const unsigned int dim, const unsigned int window,
            MetricConditionPtr condition);

    ~SequenceMonitor();

    /**
     * Test whether the difference of consecutive sequence members fulfills
     * the condition for the given 'time window' or not.
     *
     * @param v next sequence member
     */
    bool isConditionFulfilled(double* new_v, const unsigned int& dim);

protected:

    void resetCnt();

    const unsigned int dim;
    const unsigned int windowSize;
    MetricConditionPtr metricCondition;

    /**
     * Previous sequence element.
     */
    double* prev_v;
    /**
     * Counts whether the condition is fulfilled long enough.
     */
    int cnt;

};

}
}


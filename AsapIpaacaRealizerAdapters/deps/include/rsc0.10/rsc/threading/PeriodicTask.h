/* ============================================================
 *
 * This file is a part of the RSC project
 *
 * Copyright (C) 2010 by Sebastian Wrede <swrede at techfak dot uni-bielefeld dot de>
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

#include "RepetitiveTask.h"

#include <rsc/misc/langutils.h>

#include <iostream>

namespace rsc {
namespace threading {

/**
 * A specialization of Task that executes a task in a periodic manner by
 * providing an special implementation of #continueExec. A fixed interval
 * is guaranteed.
 *
 * @author swrede
 * @author jwienke
 * @author anordman
 */
class RSC_EXPORT PeriodicTask: public RepetitiveTask {
public:

    /**
     * Constructs a new periodic task with a fixed wait time after each
     * iteration.
     *
     * @param ms time to wait between iterations. No overall-fixed interval
     *           is guaranteed by the implementation. Time is in milliseconds.
     * @param accountProcTime subtracts the processing time from sleep time in
     *           order to guarantee a fixed scheduling interval
     */
    PeriodicTask(const unsigned int& ms, bool accountProcTime = true);

    virtual ~PeriodicTask();

    /**
     * Implements a waiting logic for the continuation of the repetitive task.
     *
     * @return @ctrue until the task is canceled after having waited for the
     *         specified amount of time.
     */
    virtual bool continueExec();

private:
    unsigned int cycleTime;
    rsc::logging::LoggerPtr logger;
    bool fixedScheduling;
    boost::uint64_t nextProcessingStart;
};

}
}


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

#include <map>
#include <boost/thread.hpp>

#include "Task.h"
#include "rsc/rscexports.h"

namespace rsc {
namespace threading {

/**
 * Interface for different scheduling strategies for Task instances.
 *
 * Implementations must not schedule the task if it is already canceled when
 * being scheduled.
 *
 * Implementations must execute the task even if it is canceled before within
 * the time of the specified delay.
 *
 * @author swrede
 * @author jwienke
 */
class RSC_EXPORT TaskExecutor {
public:

    /**
     * Schedules the new task.
     *
     * @param t the new task to schedule
     * @throw std::invalid_argument task to schedule is already canceled
     */
    virtual void schedule(TaskPtr t) = 0;

    /**
     * Schedules a new task to be executed after the specified delay.
     *
     * @param t new task to schedule
     * @param delayMus the delay after which the task should start
     * @throw rsc::misc::UnsupportedOperationException
     *             implementations may throw this exception to indicate that a
     *             scheduling of tasks with a specified delay is not supported
     * @throw std::invalid_argument task to schedule is already canceled
     */
    virtual void schedule(TaskPtr t, const boost::uint64_t& delayMus) = 0;

};

typedef boost::shared_ptr<TaskExecutor> TaskExecutorPtr;

}
}


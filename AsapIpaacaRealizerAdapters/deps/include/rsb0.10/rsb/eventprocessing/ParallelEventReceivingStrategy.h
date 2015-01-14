/* ============================================================
 *
 * This file is a part of the RSB project
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

#include <set>
#include <utility>

#include <boost/shared_ptr.hpp>
#include <boost/thread/shared_mutex.hpp>

#include <rsc/runtime/Properties.h>
#include <rsc/logging/Logger.h>
#include <rsc/threading/OrderedQueueDispatcherPool.h>

#include "../Event.h"
#include "../ParticipantConfig.h"
#include "PushEventReceivingStrategy.h"
#include "rsb/rsbexports.h"

namespace rsb {
namespace eventprocessing {

// optimization brainstorming:
// could a req-req condition even be a static map for one process?
// and matching something like [cid]->invoke?!?
// could even be contained in the specific condition class like a ReplyCondition or a TopicCondition
// -> dtm or some other subscription language?!? maybe this is already too much
// router config may then allow very specific and optimized configurations for
// certain patterns using specific condition classes and ports

/**
 * This push-style event receiving strategy uses one or more threads
 * to filter @ref rsb::Event s and dispatch matching events to @ref
 * rsb::Handler s.
 *
 * @author swrede
 */
class RSB_EXPORT ParallelEventReceivingStrategy: public PushEventReceivingStrategy {
public:
    static EventReceivingStrategy* create(const rsc::runtime::Properties& props);

    ParallelEventReceivingStrategy(unsigned int numThreads = 5);
    virtual ~ParallelEventReceivingStrategy();

    std::string getClassName() const;
    void printContents(std::ostream& stream) const;

    void setHandlerErrorStrategy(
            const ParticipantConfig::ErrorStrategy& strategy);

    // Qualification of HandlerPtr is required since there is another
    // HandlerPtr type in eventprocessing.
    virtual void addHandler(rsb::HandlerPtr handler, const bool& wait);
    virtual void removeHandler(rsb::HandlerPtr handler, const bool& wait);

    virtual void addFilter(filter::FilterPtr filter);
    virtual void removeFilter(filter::FilterPtr filter);

    void handle(EventPtr e);

private:
    // Qualification of HandlerPtr is required since there is another
    // HandlerPtr type in eventprocessing.
    bool filter(rsb::HandlerPtr handler, EventPtr event);
    void deliver(rsb::HandlerPtr handler, EventPtr event);

    void handleDispatchError(const std::string& message);

    rsc::logging::LoggerPtr logger;
    rsc::threading::OrderedQueueDispatcherPool<EventPtr, rsb::Handler> pool;

    mutable boost::shared_mutex filtersMutex;
    std::set<filter::FilterPtr> filters;

    mutable boost::recursive_mutex errorStrategyMutex;
    ParticipantConfig::ErrorStrategy errorStrategy;

};

}
}

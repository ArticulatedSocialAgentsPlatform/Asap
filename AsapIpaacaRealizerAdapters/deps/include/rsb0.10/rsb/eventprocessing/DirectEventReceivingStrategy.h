/* ============================================================
 *
 * This file is part of the RSB project
 *
 * Copyright (C) 2011 Jan Moringen <jmoringe@techfak.uni-bielefeld.de>
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

#include <list>
#include <set>
#include <iostream>

#include <boost/thread/shared_mutex.hpp>

#include <rsc/runtime/Properties.h>
#include <rsc/logging/Logger.h>

#include "../Event.h"
#include "../ParticipantConfig.h"

#include "PushEventReceivingStrategy.h"

#include "rsb/rsbexports.h"

namespace rsb {
namespace eventprocessing {

/**
 * This push-style event receiving strategy filters and dispatches
 * @ref rsb::Event s in the context of the thread calling @ref handle.
 *
 * Even calls to @ref rsb::Handler s run in this thread, so stack
 * exhaustion and deadlocks are possible.
 *
 * Additionally, all locking can be disabled for situation in which it
 * can be guaranteed that only one thread at a time calls any of the
 * classes methods.
 *
 * @author jmoringe
 */
class RSB_EXPORT DirectEventReceivingStrategy: public PushEventReceivingStrategy {
public:
    static EventReceivingStrategy* create(const rsc::runtime::Properties& props);

    DirectEventReceivingStrategy(bool singleThreaded = false);

    virtual ~DirectEventReceivingStrategy();

    void printContents(std::ostream& stream) const;

    void setHandlerErrorStrategy(const ParticipantConfig::ErrorStrategy& strategy);

    // Qualification of HandlerPtr is required since there is another
    // HandlerPtr type in eventprocessing.
    virtual void addHandler(rsb::HandlerPtr handler, const bool& wait);
    virtual void removeHandler(rsb::HandlerPtr handler, const bool& wait);

    virtual void addFilter(filter::FilterPtr filter);
    virtual void removeFilter(filter::FilterPtr filter);

    void handle(EventPtr e);

private:
    typedef std::list<rsb::HandlerPtr> HandlerList;

    // Qualification of HandlerPtr is required since there is another
    // HandlerPtr type in eventprocessing.
    bool filter(EventPtr event);
    void deliver(rsb::HandlerPtr handler, EventPtr event);

    void handleDispatchError(const std::string& message);

    bool filterNoLock(EventPtr e);

    void handleNoLock(EventPtr e);

    rsc::logging::LoggerPtr logger;

    mutable boost::shared_mutex filtersMutex;
    std::set<filter::FilterPtr> filters;

    mutable boost::shared_mutex errorStrategyMutex;
    ParticipantConfig::ErrorStrategy errorStrategy;

    mutable boost::shared_mutex handlerMutex;
    HandlerList handlers;

    bool singleThreaded;
};

}
}

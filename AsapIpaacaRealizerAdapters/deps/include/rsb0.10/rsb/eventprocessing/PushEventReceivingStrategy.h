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

#include <boost/shared_ptr.hpp>

#include "EventReceivingStrategy.h"
#include "rsb/rsbexports.h"

namespace rsb {

class ParticipantConfig;

namespace eventprocessing {

/**
 * This class serves as a base class for event receiving strategy
 * classes that deliver @ref rsb::Event s to @ref rsb::Handler s
 * without triggering by the receiving object. To achieve this, a list
 * of handlers is maintained and dispatching of events is done by
 * calling each handler.
 *
 * @author jmoringe
 */
class RSB_EXPORT PushEventReceivingStrategy: public EventReceivingStrategy {
public:
    /**
     * Defines the strategy to use for handling dispatching errors to the client
     * handler.
     *
     * @param strategy the new strategy to use
     */
    virtual void setHandlerErrorStrategy(
        const ParticipantConfig::ErrorStrategy& strategy) = 0;

    /**
     * Adds a new handler that will be notified about new events.
     *
     * @param handler handler to add
     * @param wait if set to @c true, this method must only return after the
     *             handler has been install completely so that the next event
     *             will be delivered to it
     */
    // Qualification of HandlerPtr is required since there is another
    // HandlerPtr type in eventprocessing.
    virtual void addHandler(rsb::HandlerPtr handler, const bool& wait) = 0;

    /**
     * Removes a handler that will will then not be notified anymore.
     *
     * @param handler handler to remove
     * @param wait if set to @c true, this method must only return after the
     *             handler has been removed completely and will not receive
     *             any more notifications
     */
    virtual void removeHandler(rsb::HandlerPtr handler, const bool& wait) = 0;
};

typedef boost::shared_ptr<PushEventReceivingStrategy> PushEventReceivingStrategyPtr;

}
}

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

#include <rsc/logging/Logger.h>

#include "../Handler.h"
#include "../transport/InPushConnector.h"
#include "InRouteConfigurator.h"
#include "PushEventReceivingStrategy.h"

namespace rsb {
namespace eventprocessing {

/**
 * This specialization of @ref InRouteConfigurator creates and
 * maintains an event receiving setup in which @ref rsb::Event s are
 * delivered using a push-style data flow by invoking registered @ref
 * rsb::Handler s.
 *
 * @author jmoringe
 */
class RSB_EXPORT PushInRouteConfigurator: public InRouteConfigurator {
public:
    PushInRouteConfigurator(const Scope&             scope,
                            const ParticipantConfig& config);
    virtual ~PushInRouteConfigurator();

    std::string getClassName() const;
    void printContents(std::ostream& stream) const;

    /**
     * Adds a new handler that will be notified about received events.
     *
     * @param handler the handler to add
     * @param wait if set to @c true, this call will return only after the
     *             handler has been completely installed and will receive the
     *             next available event
     */
    void handlerAdded(rsb::HandlerPtr handler, const bool& wait);

    /**
     * Removes a previously registered handle.
     *
     * @param handler handler to remove
     * @param wait if set to @c true, this call will return only after the
     *             handler has been completely removed and will not be notified
     *             anymore
     */
    void handlerRemoved(rsb::HandlerPtr handler, const bool& wait);

    /**
     * Sets the desired error strategy to use.
     *
     * @param strategy the strategy to use
     */
    void setErrorStrategy(const ParticipantConfig::ErrorStrategy& strategy);

    void activate();

private:
    rsc::logging::LoggerPtr logger;

    ParticipantConfig::ErrorStrategy errorStrategy;
    PushEventReceivingStrategyPtr eventReceivingStrategy;

    EventReceivingStrategyPtr createEventReceivingStrategy();
};

typedef boost::shared_ptr<PushInRouteConfigurator> PushInRouteConfiguratorPtr;

}
}

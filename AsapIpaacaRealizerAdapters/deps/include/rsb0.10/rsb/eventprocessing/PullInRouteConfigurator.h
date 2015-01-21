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

#include "InRouteConfigurator.h"
#include "rsb/rsbexports.h"

namespace rsb {
namespace eventprocessing {

/**
 * Objects of this @ref InRouteConfigurator class setup and maintain
 * the required components for a pull-style event receiving
 * configuration. In particular, a @ref PullEventReceivingStrategy is
 * instanciated to retrieve events from connectors.
 *
 * @author jmoringe
 */
class RSB_EXPORT PullInRouteConfigurator: public InRouteConfigurator {
public:
    PullInRouteConfigurator(const Scope&             scope,
                            const ParticipantConfig& config);

    /**
     * Create and return a @ref PullEventReceivingStrategy .
     *
     * @return The create @ref PullEventReceivingStrategy .
     */
    EventReceivingStrategyPtr createEventReceivingStrategy();

    std::string getClassName() const;

};

}
}

/* ============================================================
 *
 * This file is part of the rsb-spread project.
 *
 * Copyright (C) 2012, 2013 Jan Moringen <jmoringe@techfak.uni-bielefeld.de>
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
 * ============================================================  */

#pragma once

#include <rsc/logging/Logger.h>

#include <rsb/Event.h>

#include <rsb/protocol/Notification.h>
#include <rsb/protocol/FragmentedNotification.h>

#include <rsb/transport/ConverterSelectingConnector.h>

#include "SpreadMessage.h"
#include "Assembly.h"

#include "rsb/transport/spread/rsbspreadexports.h"

namespace rsb {
namespace transport {
namespace spread {

/**
 *
 *
 * @author jmoringe
 */
class RSBSPREAD_EXPORT MessageHandler : public transport::ConverterSelectingConnector<std::string> {
public:
    MessageHandler(ConverterSelectionStrategyPtr converters);
    virtual ~MessageHandler();

    EventPtr processMessage(SpreadMessagePtr message);

    void setPruning(const bool& pruning);
private:
    rsc::logging::LoggerPtr logger;

    AssemblyPoolPtr assemblyPool;

    rsb::protocol::NotificationPtr handleAndJoinFragmentedNotification(rsb::protocol::FragmentedNotificationPtr notification);
};

}
}
}

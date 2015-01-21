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

#include <rsc/runtime/Properties.h>

#include "../transport/Connector.h"
#include "EventSendingStrategy.h"

namespace rsb {
namespace eventprocessing {

/** This event sending strategy just passes incoming events to its
 * associated @ref rsb::transport::OutConnector s without
 * modification, queueing or anything else.
 *
 * @author jmoringe
 */
class RSB_EXPORT DirectEventSendingStrategy: public EventSendingStrategy {
public:
    static EventSendingStrategy* create (const rsc::runtime::Properties& props);

    void printContents(std::ostream& stream) const;

    void addConnector(transport::OutConnectorPtr connector);
    void removeConnector(transport::OutConnectorPtr connector);

    void process(EventPtr e);
private:
    typedef std::list<transport::OutConnectorPtr> ConnectorList;

    ConnectorList connectors;
};

}
}

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

#include "../eventprocessing/Handler.h"
#include "Connector.h"
#include "rsb/rsbexports.h"

namespace rsb {
namespace transport {

/**
 * Objects of classes which implement this interface can be used to
 * send events by means of one transport mechanism.
 *
 * The handle method of this class has to update the send time of the event
 * meta data according to the time the event was sent on the wire.
 *
 * @author jmoringe
 */
class RSB_EXPORT OutConnector: public Connector,
        public eventprocessing::Handler {
public:
    virtual ~OutConnector();
    typedef boost::shared_ptr<OutConnector> Ptr;
};

typedef OutConnector::Ptr OutConnectorPtr;

}
}

/* ============================================================
 *
 * This file is part of the rsb-spread project.
 *
 * Copyright (C) 2010 by Sebastian Wrede <swrede at techfak dot uni-bielefeld dot de>
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
 * ============================================================ */

#pragma once

#include <string>
#include <map>

#include <boost/shared_ptr.hpp>

#include <rsc/logging/Logger.h>
#include <rsc/misc/UUID.h>

#include <rsb/Scope.h>
#include <rsb/QualityOfServiceSpec.h>

#include <rsb/transport/Connector.h>

#include "MembershipManager.h"
#include "SpreadConnection.h"

#include "rsb/transport/spread/rsbspreadexports.h"

namespace rsb {
namespace transport {
namespace spread {

/**
 * @author swrede
 */
class RSBSPREAD_EXPORT SpreadConnector {
public:
    SpreadConnector(const std::string& host = defaultHost(),
            unsigned int port = defaultPort());

    virtual ~SpreadConnector();

    void activate();
    void deactivate();

    void setQualityOfServiceSpecs(const QualityOfServiceSpec& specs);

    void join(const std::string& name);
    void leave(const std::string& name);

    /**
     * @throw CommException error sending message
     */
    void send(const SpreadMessage& msg);
    void receive(SpreadMessagePtr msg);

    void init(const std::string& host, unsigned int port);

    SpreadConnectionPtr getConnection();

    SpreadMessage::QOS getMessageQoS() const;

    const std::vector<std::string>& makeGroupNames(const Scope& scope) const;
    std::string makeGroupName(const Scope& scope) const;

private:

    rsc::logging::LoggerPtr logger;

    rsc::misc::UUID id;

    volatile bool activated;
    SpreadConnectionPtr con;

    MembershipManagerPtr memberships;

    /**
     * The message type applied to every outgoing message.
     */
    SpreadMessage::QOS messageQoS;

    typedef std::map<QualityOfServiceSpec::Ordering, std::map<
            QualityOfServiceSpec::Reliability, SpreadMessage::QOS> > QoSMap;

    /**
     * Map from 2D input space defined in QualitOfServiceSpec to 1D spread message
     * types. First dimension is ordering, second is reliability.
     */
    static const QoSMap qosMapping;

    static QoSMap buildQoSMapping();

    mutable boost::shared_mutex groupNameCacheMutex;
    typedef std::map<Scope, std::vector<std::string> > GroupNameCache;
    mutable GroupNameCache groupNameCache;

};

typedef boost::shared_ptr<SpreadConnector> SpreadConnectorPtr;

}
}
}

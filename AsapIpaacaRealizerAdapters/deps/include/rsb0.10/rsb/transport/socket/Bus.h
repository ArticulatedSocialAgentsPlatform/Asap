/* ============================================================
 *
 * This file is part of the RSB project
 *
 * Copyright (C) 2011, 2012 Jan Moringen <jmoringe@techfak.uni-bielefeld.de>
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
#include <list>

#include <boost/shared_ptr.hpp>

#include <boost/thread/recursive_mutex.hpp>

#include <rsc/logging/Logger.h>

#include "../../Event.h"
#include "../../Scope.h"

#include "../../eventprocessing/Handler.h"

#include "../../protocol/Notification.h"

#include "BusConnection.h"
#include "ConnectorBase.h"
#include "InConnector.h"

#include "rsb/rsbexports.h"

namespace rsb {
namespace transport {
namespace socket {

/**
 * Instances of this class provide access to a socket-based bus.
 *
 * It is transparent for clients (connectors) of this class whether is
 * accessed by running the bus server or by connecting to the bus
 * server as a client.
 *
 * In-direction connectors add themselves as event sinks using the
 * @ref addSink method.
 *
 * Out-direction connectors submit events to the bus using the @ref
 * handle method.
 *
 * @author jmoringe
 */
class RSB_EXPORT Bus: public eventprocessing::Handler,
                      public boost::enable_shared_from_this<Bus> {
friend class BusConnection;
public:
    Bus(boost::asio::io_service& service, bool tcpnodelay = false);
    virtual ~Bus();

    void addSink(InConnectorPtr sink);
    void removeSink(InConnector* sink);

    void addConnector(ConnectorBase* connector);
    void removeConnector(ConnectorBase* connector);

    /**
     * Adds @a connection to the list of connections of the bus. @a
     * connection should start receiving events, only after being
     * added to the bus.
     *
     * @param connection The connection that should be added.
     */
    void addConnection(BusConnectionPtr connection);

    /**
     * Removes @a connection from the list of connections of this
     * bus. @a connection is not closed or otherwise modified.
     *
     * @param connection The connection that should be removed.
     */
    void removeConnection(BusConnectionPtr connection);

    bool isTcpnodelay() const;

    void handle(EventPtr event);
protected:
    typedef std::list<BusConnectionPtr> ConnectionList;

    ConnectionList getConnections() const;
    boost::recursive_mutex& getConnectionLock();

    virtual void handleIncoming(EventPtr         event,
                                BusConnectionPtr connection);

    virtual void suicide();
private:
    typedef std::list<ConnectorBase*>                ConnectorList;

    typedef std::list<boost::weak_ptr<InConnector> > SinkList;
    typedef std::map<Scope, SinkList>                SinkMap;

    rsc::logging::LoggerPtr  logger;

    boost::asio::io_service& service;

    ConnectionList           connections;
    boost::recursive_mutex   connectionLock;

    ConnectorList            connectors;
    SinkMap                  sinks;
    boost::recursive_mutex   connectorLock;

    bool                     tcpnodelay;

    void printContents(std::ostream& stream) const;
};

}
}
}

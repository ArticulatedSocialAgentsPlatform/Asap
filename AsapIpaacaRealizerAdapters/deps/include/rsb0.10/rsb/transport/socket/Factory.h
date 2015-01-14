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

#include <map>

#include <boost/thread/thread.hpp>

#include <boost/asio.hpp>
#include <boost/asio/ip/tcp.hpp>

#include <rsc/logging/Logger.h>

#include <rsc/patterns/Singleton.h>

#include "Bus.h"
#include "BusServer.h"

namespace rsb {
namespace transport {
namespace socket {

/**
 * The singleton instance of this class is responsible for managing
 * bus provider objects.
 *
 * For each endpoint (i.e. address and port), at most one client or
 * server bus provider can exist.
 *
 * @attention This class is not thread safe and needs to be accessed in a
 *            synchronized manner, except for #getBus.
 *
 * @author jmoringe
 */
class RSB_EXPORT Factory : public rsc::patterns::Singleton<Factory> {
    friend class rsc::patterns::Singleton<Factory>;
    friend class Bus;       // for private member function removeBusClient
    friend class BusServer; // for private member function removeBusServer
public:
    ~Factory();

    /**
     * Returns either a BusClient or Server depending on the chosen
     * @a serverMode and the existence of a server in the current process.
     */
    BusPtr getBus(const Server&          serverMode,
                  const std::string&     host,
                  const boost::uint16_t& port,
                  bool                   tcpnodelay,
                  ConnectorBase*         connector);

private:
    typedef std::pair<std::string, boost::uint16_t>	     Endpoint;
    typedef boost::shared_ptr<boost::asio::ip::tcp::socket>  SocketPtr;

    typedef boost::shared_ptr<boost::asio::io_service::work> WorkPtr;

    typedef std::map<Endpoint, BusPtr>			     BusClientMap;
    typedef std::map<Endpoint, BusServerPtr>		     BusServerMap;

    rsc::logging::LoggerPtr logger;

    BusClientMap            busClients;
    BusServerMap            busServers;
    boost::mutex            busMutex;

    boost::asio::io_service service;
    WorkPtr                 keepAlive;
    boost::thread           thread;

    Factory();

    BusPtr getBusClientFor(const std::string& host,
                           boost::uint16_t    port,
                           bool               tcpnodelay,
                           ConnectorBase*     connector);

    BusServerPtr getBusServerFor(const std::string& host,
                                 boost::uint16_t    port,
                                 bool               tcpnodelay,
                                 ConnectorBase*     connector);

    void removeBusClient(BusPtr bus);

    void removeBusServer(BusPtr bus);

    static void checkOptions(BusPtr bus, bool tcpnodelay);
};

}
}
}

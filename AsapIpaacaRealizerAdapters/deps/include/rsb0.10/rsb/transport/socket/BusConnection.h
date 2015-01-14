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

#include <string>

#include <boost/enable_shared_from_this.hpp>

#include <boost/thread/recursive_mutex.hpp>

#include <boost/asio.hpp>

#include <rsc/logging/Logger.h>
#include <rsc/runtime/Printable.h>

#include "../../Event.h"

#include "../../protocol/Notification.h"

#include "rsb/rsbexports.h"

namespace rsb {
namespace transport {
namespace socket {

// forward declaration
class Bus;
typedef boost::shared_ptr<Bus> BusPtr;

/**
 * Instances of this class implement connections to a socket-based
 * bus.
 *
 * The basic operations provided by this class are receiving an event
 * by calling @ref receiveEvent and submitting an event to the bus by
 * calling @ref sendEvent.
 *
 * In a process which acts as a client for a particular bus, a single
 * instance of this class is connected to the remote bus server and
 * provides access to the bus for all participants in the process.
 *
 * A process which acts as the server for a particular bus, manages
 * (via the @ref BusServer class) one @ref BusConnection object for
 * each client (remote process) connected to the bus.
 *
 * This class is not thread-safe.
 *
 * @author jmoringe
 */
class RSB_EXPORT BusConnection : public boost::enable_shared_from_this<BusConnection>,
                                 public rsc::runtime::Printable {
public:
    typedef boost::shared_ptr<boost::asio::ip::tcp::socket> SocketPtr;

    BusConnection(BusPtr    bus,
                  SocketPtr socket,
                  bool      client,
                  bool      tcpNoDelay = false);

    ~BusConnection();

    void shutdown();

    void startReceiving();

    void sendEvent(EventPtr           event,
                   const std::string& wireSchema);
private:
    typedef boost::weak_ptr<Bus> WeakBusPtr;

    rsc::logging::LoggerPtr logger;

    SocketPtr               socket;

    WeakBusPtr              bus;

    volatile bool           disconnecting;
    volatile bool           activeShutdown;

    boost::recursive_mutex  mutex;

    // Receive buffers
    protocol::Notification  notification;
    std::string             lengthReceiveBuffer;
    std::string             messageReceiveBuffer;

    // Send buffers
    std::string             lengthSendBuffer;
    std::string             messageSendBuffer;

    void performSafeCleanup(const std::string& context);

    void receiveEvent();

    void handleReadLength(const boost::system::error_code& error,
                          size_t                           bytesTransferred);

    void handleReadBody(const boost::system::error_code& error,
                        size_t                           bytesTransferred,
                        size_t                           expected);

    void printContents(std::ostream& stream) const;

    void disconnect();

};

typedef boost::shared_ptr<BusConnection> BusConnectionPtr;

}
}
}

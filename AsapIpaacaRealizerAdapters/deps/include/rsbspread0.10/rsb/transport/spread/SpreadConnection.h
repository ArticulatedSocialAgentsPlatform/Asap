/* ============================================================
 *
 * This file is part of the rsb-spread project
 *
 * Copyright (C) 2010 by Sebastian Wrede <swrede at techfak dot uni-bielefeld dot de>
 * Copyright (C) 2013 Jan Moringen <jmoringe@techfak.uni-bielefeld.de>
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
#include <boost/thread.hpp>
#include <boost/thread/condition.hpp>

#include <rsc/logging/Logger.h>

#include "SpreadMessage.h"

#include "rsb/transport/spread/rsbspreadexports.h"

// forward declaration to avoid exposing sp.h with strange defines that prevent
// other code from compiling
typedef int mailbox;

namespace rsb {
namespace transport {
namespace spread {

RSBSPREAD_EXPORT std::string defaultHost();

RSBSPREAD_EXPORT unsigned int defaultPort();

/**
 * A wrapper class providing an object-oriented interface to the Spread API.
 *
 * @note this class is not thread-safe! Use it only single-threaded! The only
 *       exception to this rule is #interruptReceive. It can be used to kill an
 *       asynchronously operating receiver thread on the connection.
 *
 * @author swrede
 * @author jwienke
 */
class RSBSPREAD_EXPORT SpreadConnection {
public:
    SpreadConnection(const std::string& prefix,
                     const std::string& host   = defaultHost(),
                     unsigned int port         = defaultPort());
    virtual ~SpreadConnection();

    /**
     * @name connection state management
     * @todo is this really necessary?
     */
    //@{

    /**
     * Activates the connection and thereby connects to the spread daemon as
     * configured in the constructor.
     *
     * @throw CommException error connecting to the daemon
     * @throw rsc::misc::IllegalStateException already activated
     */
    void activate();

    /**
     * Disconnects from the daemon.
     *
     * @pre there must be no more reader blocking in #receive
     * @throw rsc::misc::IllegalStateException already deactivated
     */
    void deactivate();

    //@}

    /**
     * @name fundamental message exchange
     */
    //@{

    /**
     * Sends a message on spread.
     *
     * @param msg message to send
     * @throw rsc::misc::IllegalStateException connection was not active
     * @throw CommException communication error sending the message
     */
    void send(const SpreadMessage& msg);

    /**
     * Tries to receive the next message from this connection and blocks until
     * it is available.
     *
     * @param sm out parameter with the message to fill with the read contents
     * @note not all readers in different threads receive all messages, one
     *       message is only received by one thread
     * @throw rsc::misc::IllegalStateException connection was not active
     * @throw CommException communication error receiving a message
     * @throw boost::thread_interrupted if receiving was interrupted using
     *                                  #interruptReceive
     */
    void receive(SpreadMessagePtr sm);

    //@}

    /**
     * Interrupts a potential receiver blocking in the read call some time after
     * this call. The receiver may receive all queued messages before being
     * interrupted.
     *
     * @note this method may explicitly be called from a different thread than
     *       the one blocking in #receive. Nevertheless only one other thread at
     *       a time is allowed call this method.
     * @throw rsc::misc::IllegalStateException connection was not active
     */
    void interruptReceive();

    /**
     * Tells if this instance is connected to spread daemon.
     *
     * @return @c true if connected
     */
    bool isActive();

    /**
     * Returns number of messages sent.
     *
     * @return number of sent messages
     */
    unsigned long getMsgCount();

    /**
     * Returns the internally used mailbox for other low-level functions.
     *
     * @return mailbox
     * @todo why pointer? mailbox is a typedef to int? If pointer is required
     *       use a shared ptr
     */
    mailbox* getMailbox();

private:
    std::string generateId(const std::string& prefix);

    rsc::logging::LoggerPtr logger;
    /**
     * A flag to indicate whether we are connected to spread.
     */
    volatile bool connected;
    /**
     * Handle to the internal spread connection.
     */
    mailbox con;
    /**
     * Host for the spread daemon.
     */
    std::string host;
    /**
     * Port for the spread daemon.
     */
    unsigned int port;
    /**
     * The name of the daemon. Can consists of port and host, e.g.
     * 4803\@localhost or only a port. See SP_connect(3) man-page for
     * details.
     */
    std::string spreadname;
    /**
     * Private name of this connection.
     */
    std::string spreadpg;
    /**
     * User-defined name to be used for this spread connection.
     */
    std::string conId;
    /**
     * Number of message sent via this connection.
     */
    unsigned long msgCount;
};

typedef boost::shared_ptr<SpreadConnection> SpreadConnectionPtr;

}
}
}

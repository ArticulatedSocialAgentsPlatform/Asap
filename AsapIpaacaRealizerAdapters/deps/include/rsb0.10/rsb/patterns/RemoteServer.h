/* ============================================================
 *
 * This file is a part of RSB project
 *
 * Copyright (C) 2010 by Johannes Wienke <jwienke at techfak dot uni-bielefeld dot de>
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

#include <stdexcept>
#include <string>

#include <boost/noncopyable.hpp>
#include <boost/thread/mutex.hpp>

#include <rsc/runtime/TypeStringTools.h>
#include <rsc/logging/Logger.h>
#include <rsc/threading/Future.h>

#include "../Event.h"
#include "../Exception.h"
#include "../Informer.h"
#include "../Listener.h"
#include "../ParticipantConfig.h"
#include "../Scope.h"
#include "rsb/rsbexports.h"

namespace rsb {
namespace patterns {

class WaitingEventHandler;

/**
 * The client side of a request-reply-based communication channel.
 *
 * Objects of this class represent remote servers in a way that allows
 * calling methods on them as if they were local.
 *
 * @author jwienke
 * @author jmoringe
 */
class RSB_EXPORT RemoteServer: public boost::noncopyable {
public:
    typedef rsc::threading::Future<EventPtr> FutureType;
    typedef boost::shared_ptr<FutureType> FuturePtr;

    template <typename O>
    class DataFuture  {
    public:
        DataFuture(FuturePtr target):
            target(target) {
        }

        bool isDone() {
            return this->target->isDone();
        }

        boost::shared_ptr<O> get(double timeout) {
            return boost::static_pointer_cast<O>(this->target->get(timeout)->getData());
        }
    private:
        FuturePtr target;
    };

    /**
     * Construct a new @c RemoteServer object which can be used to
     * call methods of the server at @a scope.
     *
     * @param scope The base scope of the server the methods of which
     * will be called.
     */
    RemoteServer(const Scope& scope, const ParticipantConfig &listenerConfig,
            const ParticipantConfig &informerConfig);
    virtual ~RemoteServer();

    /**
     * Prepares an Event which can be used for a request based on typed data.
     *
     * @param args data for the event
     * @tparam I data type for the data in the event
     * @return event containing the data and appropriate type tags
     */
    template<class I>
    EventPtr prepareRequestEvent(boost::shared_ptr<I> args) {
        EventPtr request(new Event);
        request->setType(rsc::runtime::typeName<I>());
        request->setData(args);
        return request;
    }

    /**
     * Call the method named @a methodName on the remote server,
     * passing it the event @a data as argument and returning an event
     * which contains the value returned by the remote method.
     *
     * @param methodName Name of the method that should be called.
     * @param data An @ref Event object containing the argument object
     * that should be passed to the called method.
     * @return A @ref rsc::threading::TimeoutFuture object from which
     * the result (an @ref EventPtr) of the method call can be
     * obtained at the caller's discretion.
     */
    FuturePtr callAsync(const std::string& methodName, EventPtr data);

    /**
     * Call the method named @a methodName on the remote server,
     * passing it the argument object @a args and returning the value
     * returned by the remote method.
     *
     * @tparam I type of the method call argument object.
     * @tparam O type of the method return value.
     * @param methodName Name of the method that should be called.
     * @param args The argument object that should be passed to the
     * called method.
     * @return A @ref DataFuture object from which the result (a
     * shared_ptr to an object of type @a O) of the method call can be
     * obtained at the caller's discretion.
     */
    template <typename O, typename I>
    DataFuture<O> callAsync(const std::string&    methodName,
                            boost::shared_ptr<I> args) {
        return DataFuture<O>(callAsync(methodName, prepareRequestEvent(args)));
    }

    /**
     * Call the method named @a methodName on the remote server,
     * without arguments and returning the value returned by the
     * remote method.
     *
     * @tparam O type of the method return value.
     * @param methodName Name of the method that should be called.
     * @return A @ref DataFuture object from which the result (a
     * shared_ptr to an object of type @a O) of the method call can be
     * obtained at the caller's discretion.
     */
    template <typename O>
    DataFuture<O> callAsync(const std::string& methodName) {
        EventPtr request(new Event());
        request->setType(rsc::runtime::typeName<void>());
        request->setData(VoidPtr());
        return DataFuture<O>(callAsync(methodName, request));
    }

    /**
     * Call the method named @a methodName on the remote server,
     * passing it the event @a data as argument and returning an event
     * which contains the value returned by the remote method.
     *
     * This method blocks until the computation on the remote side has
     * been completed and the result has been received.
     *
     * @param methodName Name of the method that should be called.
     * @param data An @ref Event object containing the argument object
     * that should be passed to the called method.
     * @param maxReplyWaitTime Maximum number of seconds to wait for a
     * reply from the server when calling a method.
     * @return An @ref Event object containing the result of the
     * method call.
     * @throw rsc::threading::FutureTimeoutException if the method
     * call is not completed within the maximum waiting time.
     * @throw rsc::threading::FutureTaskExecutionException if the
     * method call fails.
     */
    EventPtr call(const std::string& methodName,
                  EventPtr          data,
                  unsigned int      maxReplyWaitTime = 25);

    /**
     * Call the method named @a methodName on the remote server,
     * passing it the argument object @a args and return the value
     * returned by the remote method.
     *
     * This method blocks until the computation on the remote side has
     * been completed and the result has been received.
     *
     * @tparam I type of the method call argument object.
     * @tparam O type of the method return value.
     * @param methodName Name of the method that should be called.
     * @param args The argument object that should be passed to the
     * called method.
     * @param maxReplyWaitTime Maximum number of seconds to wait for a
     * reply from the server when calling a method.
     * @return The result of the method call.
     * @throw rsc::threading::FutureTimeoutException if the method
     * call is not completed within the maximum waiting time.
     * @throw rsc::threading::FutureTaskExecutionException if the
     * method call fails.
     */
    template <typename O, typename I>
    boost::shared_ptr<O> call(const std::string&    methodName,
                              boost::shared_ptr<I> args,
                              unsigned int         maxReplyWaitTime = 25) {
        return callAsync<O>(methodName, args).get(maxReplyWaitTime);
    }

    /**
     * Call the method named @a methodName on the remote server,
     * without arguments and return the value returned by the remote
     * method.
     *
     * This method blocks until the computation on the remote side has
     * been completed and the result has been received.
     *
     * @tparam O type of the method return value.
     * @param methodName Name of the method that should be called.
     * @param maxReplyWaitTime Maximum number of seconds to wait for a
     * reply from the server when calling a method.
     * @return The result of the method call.
     * @throw rsc::threading::FutureTimeoutException if the method
     * call is not completed within the maximum waiting time.
     * @throw rsc::threading::FutureTaskExecutionException if the
     * method call fails.
     */
    template <typename O>
    boost::shared_ptr<O> call(const std::string& methodName,
                              unsigned int       maxReplyWaitTime = 25) {
        return callAsync<O>(methodName).get(maxReplyWaitTime);
    }
private:
    rsc::logging::LoggerPtr logger;

    Scope scope;
    ParticipantConfig listenerConfig;
    ParticipantConfig informerConfig;

    class MethodSet {
    public:
        std::string methodName;
        std::string sendType;
        boost::shared_ptr<WaitingEventHandler> handler;
        ListenerPtr replyListener;
        Informer<void>::Ptr requestInformer;
    };

    boost::mutex methodSetMutex;
    std::map<std::string, MethodSet> methodSets;

    MethodSet getMethodSet(const std::string& methodName,
                           const std::string& sendType);

};

template <>
class RemoteServer::DataFuture<void> {
public:
    DataFuture(FuturePtr target):
        target(target) {
    }

    bool isDone() {
        return this->target->isDone();
    }

    boost::shared_ptr<void> get(double timeout) {
        this->target->get(timeout)->getData();
        return boost::shared_ptr<void>();
    }
private:
    FuturePtr target;
};

typedef boost::shared_ptr<RemoteServer> RemoteServerPtr;

}
}

/* ============================================================
 *
 * This file is a part of RSB project
 *
 * Copyright (C) 2010 by Johannes Wienke <jwienke at techfak dot uni-bielefeld dot de>
 * Copyright (C) 2011, 2012, 2013 Jan Moringen <jmoringe@techfak.uni-bielefeld.de>
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

#include <set>
#include <map>
#include <string>

#include <boost/noncopyable.hpp>
#include <boost/shared_ptr.hpp>

#include <rsc/runtime/TypeStringTools.h>
#include <rsc/logging/Logger.h>

#include "../Informer.h"
#include "../Listener.h"
#include "../ParticipantConfig.h"
#include "../Scope.h"
#include "rsb/rsbexports.h"

namespace rsb {
namespace patterns {

/**
 * The server side of a request-reply-based communication channel.
 *
 * @author jwienke
 */
class RSB_EXPORT Server: public boost::noncopyable {
public:

    /**
     * Callback object interface to implement for registering callable methods.
     *
     * @author jwienke
     */
    class RSB_EXPORT IntlCallback {
    public:

        virtual ~IntlCallback();

        virtual const std::string& getRequestType() const = 0;

        virtual AnnotatedData
                intlCall(const std::string& methodName,
                        boost::shared_ptr<void> input) = 0;

    };

    /**
     * Base class for callback classes.
     *
     * @author jmoringe
     */
    class RSB_EXPORT CallbackBase : public IntlCallback {
    public:
        virtual const std::string& getRequestType() const;
        virtual const std::string& getReplyType() const;
    protected:
        CallbackBase(const std::string& requestType,
                     const std::string& replyType);

        std::string requestType;
        std::string replyType;
    };

    /**
     * A callback which allows any kind of reply type but is restricted to a
     * single request type.
     *
     * @author jwienke
     * @tparam RequestType request type class
     */
    template<class RequestType>
    class RSB_EXPORT AnyReplyTypeCallback : public CallbackBase {
    public:
        // typeid is due to msvc strangeness
        AnyReplyTypeCallback(const std::string& requestType
                 = rsc::runtime::typeName(typeid(RequestType))) :
            CallbackBase(requestType, "") {
        }

        /**
         * Implement this method to perform actions.
         *
         * @param methodName called method
         * @param input input data for the method
         * @return result data for the method with type name added
         * @throw std::exception all exceptions based on this type are
         *                       automatically caught and delivered to the
         *                       remote server
         */
        virtual AnnotatedData call(const std::string& methodName,
                boost::shared_ptr<RequestType> input) = 0;
    private:
        AnnotatedData intlCall(const std::string& methodName,
                boost::shared_ptr<void> input) {
            return call(methodName,
                    boost::static_pointer_cast<RequestType>(input));
        }

    };

    /**
     * Callback object used to register one method for a server.
     *
     * @tparam RequestType the data type of the request payload
     * @tparam ReplyType the data type of the reply payload
     *
     * @author jwienke
     */
    template<class RequestType, class ReplyType>
    class Callback: public CallbackBase {
    public:
        // typeid is due to msvc strangeness
        Callback(const std::string& requestType
                 = rsc::runtime::typeName(typeid(RequestType)),
                 const std::string& replyType
                 = rsc::runtime::typeName(typeid(ReplyType))) :
            CallbackBase(requestType, replyType) {
        }

        /**
         * Implement this method to perform actions.
         *
         * @param methodName called method
         * @param input input data for the method
         * @return result data for the method
         * @throw std::exception all exceptions based on this type are
         *                       automatically caught and delivered to the
         *                       remote server
         */
        virtual boost::shared_ptr<ReplyType> call(const std::string& methodName,
                                                  boost::shared_ptr<RequestType> input) = 0;
    private:
        AnnotatedData intlCall(const std::string& methodName,
                boost::shared_ptr<void> input) {
            return std::make_pair(getReplyType(), call(methodName,
                        boost::static_pointer_cast<RequestType>(input)));
        }
    };

    template<class RequestType>
    class Callback<RequestType, void>: public CallbackBase {
    public:
        // typeid is due to msvc strangeness
        Callback(const std::string& requestType
                 = rsc::runtime::typeName(typeid(RequestType)),
                 const std::string& replyType
                 = rsc::runtime::typeName(typeid(void))) :
            CallbackBase(requestType, replyType) {
        }

        /**
         * Implement this method to perform actions.
         *
         * @param methodName called method
         * @param input input data for the method
         * @throw std::exception all exceptions based on this type are
         *                       automatically caught and delivered to the
         *                       remote server
         */
        virtual void call(const std::string& methodName,
                          boost::shared_ptr<RequestType> input) = 0;
    private:
        AnnotatedData intlCall(const std::string& methodName,
                boost::shared_ptr<void> input) {
            call(methodName, boost::static_pointer_cast<RequestType>(input));
            return make_pair(getReplyType(), boost::shared_ptr<void>());
        }

    };

    template<class ReplyType>
    class Callback<void, ReplyType>: public CallbackBase {
    public:
      // typeid is due to msvc strangeness
      Callback(const std::string& requestType
           = rsc::runtime::typeName(typeid(void)),
           const std::string& replyType
           = rsc::runtime::typeName(typeid(ReplyType))) :
      CallbackBase(requestType, replyType) {
      }

      /**
       * Implement this method to perform actions.
       *
       * @param methodName called method
       * @return result data for the method
       * @throw std::exception all exceptions based on this type are
       *                       automatically caught and delivered to the
       *                       remote server
       */
        virtual boost::shared_ptr<ReplyType> call(
                const std::string& methodName) = 0;
    private:
        AnnotatedData intlCall(const std::string& methodName,
                boost::shared_ptr<void> input) {
            return std::make_pair(getReplyType(), call(methodName));
        }

    };

    typedef boost::shared_ptr<IntlCallback> CallbackPtr;

    Server(const Scope& scope, const ParticipantConfig &listenerConfig,
            const ParticipantConfig &informerConfig);
    virtual ~Server();

    /**
     * Register a new method with the given name.
     *
     * @param methodName method name
     * @param callback callback to execute for the method
     * @throw MethodExistsException thrown if a method with this name already exists
     */
    void registerMethod(const std::string& methodName, CallbackPtr callback);

private:

    Scope scope;
    ParticipantConfig listenerConfig;
    ParticipantConfig informerConfig;

    std::set<ListenerPtr> requestListeners;

    std::map<std::string, Informer<AnyType>::Ptr> methods;

};

// Since this is a complete specialization of Server::Callback, it
// cannot be declared inline in Server like the partial
// specializations (see C++03, §14.7.3/2:).

/**
 * A callback which does not take a request and does not produce a
 * result.
 *
 * @author jmoringe.
 */
template<>
class RSB_EXPORT Server::Callback<void, void>: public Server::CallbackBase {
 public:
    // typeid is due to msvc strangeness
    Callback() :
        Server::CallbackBase(rsc::runtime::typeName(typeid(void)),
                             rsc::runtime::typeName(typeid(void))) {
    }

    /**
     * Implement this method to perform actions.
     *
     * @param methodName called method
     * @throw std::exception all exceptions based on this type are
     *                       automatically caught and delivered to the
     *                       remote server
     */
    virtual void call(const std::string& methodName) = 0;
 private:
    AnnotatedData intlCall(const std::string& methodName,
                           boost::shared_ptr<void> /*input*/) {
        call(methodName);
        return std::make_pair(getReplyType(), boost::shared_ptr<void>());
    }

};

typedef boost::shared_ptr<Server> ServerPtr;

}
}

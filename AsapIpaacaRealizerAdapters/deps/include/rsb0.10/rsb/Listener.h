/* ============================================================
 *
 * This file is a part of the RSB project
 *
 * Copyright (C) 2010 by Sebastian Wrede <swrede at techfak dot uni-bielefeld dot de>
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

#include <boost/scoped_ptr.hpp>

#include <rsc/logging/Logger.h>

#include <boost/shared_ptr.hpp>

#include "rsb/rsbexports.h"
#include "Participant.h"

namespace rsb {

class Event;
typedef boost::shared_ptr<Event> EventPtr;
class Handler;
typedef boost::shared_ptr<Handler> HandlerPtr;

namespace filter {
class Filter;
typedef boost::shared_ptr<Filter> FilterPtr;
}

namespace eventprocessing {
class PushInRouteConfigurator;
typedef boost::shared_ptr<PushInRouteConfigurator> PushInRouteConfiguratorPtr;
}

namespace transport {
class InPushConnector;
typedef boost::shared_ptr<InPushConnector> InPushConnectorPtr;
}

/**
 * A Listener receives events published by @ref rsb::Informer objects
 * by participating in a channel with a suitable scope. @ref Handler
 * objects have to be added to the listener to actually process
 * received events. These events can be filtered for all handlers by adding
 * Filter instances to this class. Filter form a conjunction.
 *
 * Usage example:
 * @code
 * ListenerPtr listener = getFactory().createListener(Scope("/example/informer"));
 * boost::shared_ptr<MyDataHandler> dh(new MyDataHandler());
 * listener->addHandler(dh);
 * @endcode
 *
 * @author swrede
 *
 * @todo use templates in subscriptions only? (however, they need the event info)
 */
class RSB_EXPORT Listener: public Participant {
public:

    /**
     * Constructs a new Listener assigned to the specified scope. The
     * Listener connects to the bus using the supplied connectors.
     *
     * @param connectors a list of connectors that the listener should
     *                   use to communicate with the bus.
     * @param scope the scope where the data is received from.
     * @param config the configuration that was used to setup this listener
     *
     * @note This constructor is exposed for unit tests and such. Use
     * @ref Factory::createListener instead of calling this directly.
     */
    Listener(const std::vector<transport::InPushConnectorPtr>& connectors,
            const Scope& scope, const ParticipantConfig& config);

    virtual ~Listener();

    std::string getClassName() const;

    /**
     * Adds a filter that will be applied after some time (but not immediately
     * after this call) for all handlers.
     *
     * @param filter filter to add
     */
    void addFilter(filter::FilterPtr filter);

    /**
     * Removes a previously installed filter if it is present by pointer
     * comparison some time after this call.
     *
     * @param filter filter to remove if present
     */
    void removeFilter(filter::FilterPtr filter);

    /**
     * Adds a @ref rsb::Handler to the Listener. Events which
     * match the restrictions described by the associated
     * filters are passed to all handlers.
     *
     * @param h a pointer to the Handler.
     * @param wait if set to @c true, this method will return only after the
     *             handler has completely been installed and will receive the
     *             next available message. Otherwise it may return earlier.
     */
    virtual void addHandler(HandlerPtr h, bool wait = true);

    /**
     * Removes a Handler instance to process newly received events.
     *
     * @param h handler to remove if present (comparison based on pointer)
     * @param wait if set to @c true, this method will return only after the
     *             handler has been completely removed from the event processing
     *             and will not be called anymore from this listener.
     */
    void removeHandler(HandlerPtr h, bool wait = true);

private:

    class Impl;
    boost::scoped_ptr<Impl> d;

    void initialize(const std::vector<transport::InPushConnectorPtr>& connectors,
                    const Scope&                                      scope,
                    const ParticipantConfig&                          config);
};

typedef boost::shared_ptr<Listener> ListenerPtr;

}

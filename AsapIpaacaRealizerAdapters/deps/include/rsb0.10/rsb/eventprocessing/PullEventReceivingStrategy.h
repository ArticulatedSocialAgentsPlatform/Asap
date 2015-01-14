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

#include <set>

#include <boost/shared_ptr.hpp>
#include <boost/scoped_ptr.hpp>

#include "EventReceivingStrategy.h"

namespace rsb {

class Event;
typedef boost::shared_ptr<Event> EventPtr;

namespace filter {
class Filter;
typedef boost::shared_ptr<Filter> FilterPtr;
}

namespace transport {
class InPullConnector;
typedef boost::shared_ptr<InPullConnector> InPullConnectorPtr;
}

namespace eventprocessing {

/**
 * Instances of this class retrieve @ref Event s from @ref
 * transport::Connector s when explicitly asked by a client (which
 * usually is a @ref Participant ). The Retrieval works (roughly) as
 * follows:
 *
 * -# The client calls the raiseEvent method of the @c
 *    PullEventReceivingStrategy instance
 * -# The instance calls the raiseEvent methods of its connectors
 * -# If the event has not been discarded during filtering, it is
 *    returned from raiseEvent
 *
 * @author jmoringe
 */
class RSB_EXPORT PullEventReceivingStrategy: public EventReceivingStrategy {
public:
    PullEventReceivingStrategy(const std::set<transport::InPullConnectorPtr>& connectors);
    virtual ~PullEventReceivingStrategy();

    virtual void addFilter(filter::FilterPtr filter);
    virtual void removeFilter(filter::FilterPtr filter);

    /**
     * Retrieve an @ref Event from the connectors and return it.
     * @param block Controls whether the call should block until an
     * event becomes available if that is not the case immediately.
     *
     * @return A pointer to the received event.
     */
    EventPtr raiseEvent(bool block);
private:

    class Impl;
    boost::scoped_ptr<Impl> d;

    std::string getClassName() const;
    void printContents(std::ostream& stream) const;

    void handle(EventPtr event); // not used
};

typedef boost::shared_ptr<PullEventReceivingStrategy> PullEventReceivingStrategyPtr;

}
}

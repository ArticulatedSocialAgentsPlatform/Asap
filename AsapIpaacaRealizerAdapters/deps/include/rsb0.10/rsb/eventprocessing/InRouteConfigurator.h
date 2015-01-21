/* ============================================================
 *
 * This file is a part of the RSB project
 *
 * Copyright (C) 2010 by Sebastian Wrede <swrede at techfak dot uni-bielefeld dot de>
 *               2011 Jan Moringen <jmoringe@techfak.uni-bielefeld.de>
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
#include <set>

#include <boost/shared_ptr.hpp>
#include <boost/scoped_ptr.hpp>
#include <boost/noncopyable.hpp>

#include <rsc/runtime/Printable.h>

#include "rsb/rsbexports.h"
#include "../ParticipantConfig.h"

namespace rsb {

class QualityOfServiceSpec;

class Event;
typedef boost::shared_ptr<Event> EventPtr;
class Scope;

namespace filter {
class Filter;
typedef boost::shared_ptr<Filter> FilterPtr;
}

namespace transport {
class InConnector;
typedef boost::shared_ptr<InConnector> InConnectorPtr;
}

namespace eventprocessing {

class EventReceivingStrategy;
typedef boost::shared_ptr<EventReceivingStrategy> EventReceivingStrategyPtr;

/**
 * A class responsible of configuring the route that processes incoming events
 * from one or more InConnector instances in one Listener. This responsibility
 * includes updates to the route from adding or removing Filter or Handler
 * instances.
 *
 * @author swrede
 */
class RSB_EXPORT InRouteConfigurator: public virtual rsc::runtime::Printable,
                                      private boost::noncopyable {
public:
    typedef std::set<transport::InConnectorPtr> ConnectorSet;

    InRouteConfigurator(const Scope&             scope,
                        const ParticipantConfig& config);
    virtual ~InRouteConfigurator();

    std::string getClassName() const;
    void printContents(std::ostream& stream) const;

    virtual void activate();
    virtual void deactivate();

    const ParticipantConfig::EventProcessingStrategy& getReceivingStrategyConfig() const;

    EventReceivingStrategyPtr getEventReceivingStrategy() const;

    ConnectorSet getConnectors();

    void addConnector(transport::InConnectorPtr connector);
    void removeConnector(transport::InConnectorPtr connector);

    void filterAdded(filter::FilterPtr filter);
    void filterRemoved(filter::FilterPtr filter);

    /**
     * Define the desired quality of service specifications for published
     * events.
     *
     * @param specs QoS specification
     * @throw UnsupportedQualityOfServiceException requirements cannot be met
     */
    void setQualityOfServiceSpecs(const QualityOfServiceSpec& specs);
private:

    class Impl;
    boost::scoped_ptr<Impl> d;

    virtual EventReceivingStrategyPtr createEventReceivingStrategy() = 0;
};

typedef boost::shared_ptr<InRouteConfigurator> InRouteConfiguratorPtr;

}
}

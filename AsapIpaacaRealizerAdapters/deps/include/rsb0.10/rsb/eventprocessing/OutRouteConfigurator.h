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

#include <list>

#include <boost/shared_ptr.hpp>
#include <boost/scoped_ptr.hpp>
#include <boost/noncopyable.hpp>

#include <rsc/runtime/Printable.h>

#include "rsb/rsbexports.h"

namespace rsb {

class Scope;

class Event;
typedef boost::shared_ptr<Event> EventPtr;
class QualityOfServiceSpec;

namespace transport {
class OutConnector;
typedef boost::shared_ptr<OutConnector> OutConnectorPtr;
}

namespace eventprocessing {

/**
 * @author swrede
 * @author jmoringe
 *
 * @todo add configuration, provide preliminary set up interface
 */
class RSB_EXPORT OutRouteConfigurator: public virtual rsc::runtime::Printable,
                                       private boost::noncopyable {
public:
    OutRouteConfigurator(const Scope& scope);
    virtual ~OutRouteConfigurator();

    std::string getClassName() const;
    void printContents(std::ostream& stream) const;

    void activate();
    void deactivate();

    void addConnector(transport::OutConnectorPtr connector);
    void removeConnector(transport::OutConnectorPtr connector);

    /**
     * Publish event to out ports of this router.
     *
     * @param e event to publish
     */
    void publish(EventPtr e);

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

};

typedef boost::shared_ptr<OutRouteConfigurator> OutRouteConfiguratorPtr;

}
}

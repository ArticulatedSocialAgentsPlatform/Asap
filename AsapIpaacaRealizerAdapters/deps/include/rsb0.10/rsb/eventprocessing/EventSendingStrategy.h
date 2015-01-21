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

#include <boost/noncopyable.hpp>
#include <boost/shared_ptr.hpp>

#include <rsc/runtime/Printable.h>

#include "rsb/rsbexports.h"

namespace rsb {

class Event;
typedef boost::shared_ptr<Event> EventPtr;

namespace transport {
class OutConnector;
typedef boost::shared_ptr<OutConnector> OutConnectorPtr;
}

namespace eventprocessing {

/** Implementations of this interface organize the sending of events
 * via @ref rsb::transport::OutConnector s.
 *
 * @author swrede
 * @author jmoringe
 */
class RSB_EXPORT EventSendingStrategy: public virtual rsc::runtime::Printable,
                                       private boost::noncopyable {
public:
    virtual ~EventSendingStrategy();

    /**
     * Add @a connector to the list of connectors to which this
     * strategy should deliver events.
     *
     * @param connector The new @ref rsb::transport::OutConnector .
     */
    virtual void addConnector(transport::OutConnectorPtr connector) = 0;

    /**
     * Remove @a connector from the list of connectors to which
     * this strategy should deliver events.
     *
     * @param connector The @ref rsb::transport::OutConnector that
     *                  should be removed.
     */
    virtual void removeConnector(transport::OutConnectorPtr connector) = 0;

    /**
     * Deliver @a event to all @ref rsb::transport::OutConnector
     * objects associated to this strategy.
     *
     * @param event An @ref rsb::Event that should be delivered to
     *              the connectors.
     */
    virtual void process(EventPtr event) = 0;
};

typedef boost::shared_ptr<EventSendingStrategy> EventSendingStrategyPtr;

}
}

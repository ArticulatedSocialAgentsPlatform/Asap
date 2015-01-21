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

#include <boost/shared_ptr.hpp>

#include "InConnector.h"
#include "rsb/rsbexports.h"

namespace rsb {

class Event;
typedef boost::shared_ptr<Event> EventPtr;

namespace transport {

/**
 * Objects of classes which implement this specialized @ref
 * InConnector interface provide the ability to receive events in
 * pull-style manner by means of one transport mechanism.
 *
 * In general, and especially when used in a constellation with one
 * receiving participant and a single @ref InPullConnector, the
 * pull-style data-flow can be much more efficient than the push-style
 * data-flow provided by @ref InPushConnector.
 *
 * @author jmoringe
 */
class RSB_EXPORT InPullConnector: public virtual InConnector {
public:
    typedef boost::shared_ptr<InPullConnector> Ptr;

    virtual ~InPullConnector();

    virtual EventPtr raiseEvent(bool block) = 0;
};

typedef InPullConnector::Ptr InPullConnectorPtr;

}
}

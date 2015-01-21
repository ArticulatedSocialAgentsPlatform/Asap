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

#include <boost/shared_ptr.hpp>

#include <rsc/runtime/Printable.h>

#include "rsb/rsbexports.h"

namespace rsb {

class Scope;
class QualityOfServiceSpec;

namespace transport {

/**
 * @author swrede
 */
class RSB_EXPORT Connector: public virtual rsc::runtime::Printable {
public:
    virtual ~Connector();

    /**
     * Sets the scope this connector will send/receive events to/from.
     *
     * @param scope scope of the connector
     */
    virtual void setScope(const Scope& scope) = 0;

    /**
     * Activates the connector. Settings made between construction and
     * activation via this method must be applied on a call to this method.
     */
    virtual void activate() = 0;
    virtual void deactivate() = 0;

    /**
     * Requests new QoS settings for publishing events. Does not
     * influence the receiving part.
     *
     * @param specs QoS specification
     * @throw UnsupportedQualityOfServiceException requirements cannot be met
     */
    virtual void setQualityOfServiceSpecs(const QualityOfServiceSpec& specs) = 0;
};

typedef boost::shared_ptr<Connector> ConnectorPtr;

}
}

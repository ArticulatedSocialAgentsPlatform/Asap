/* ============================================================
 *
 * This file is part of the RSB project.
 *
 * Copyright (C) 2012 Jan Moringen <jmoringe@techfak.uni-bielefeld.de>
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
 * ============================================================  */

#pragma once

#include <boost/shared_ptr.hpp>

#include <rsc/logging/Logger.h>

#include "../../Scope.h"

#include "../../eventprocessing/Handler.h"

#include "../InConnector.h"

#include "rsb/rsbexports.h"

namespace rsb {
namespace transport{
namespace inprocess {

/**
 *
 *
 * @author jmoringe
 */
class RSB_EXPORT InConnector: public virtual transport::InConnector,
                              public virtual eventprocessing::Handler {
public:
    virtual ~InConnector();

    void printContents(std::ostream& stream) const;

    virtual void activate();
    virtual void deactivate();

    virtual Scope getScope() const;
    virtual void setScope(const Scope& scope);
protected:
    InConnector();
private:
    rsc::logging::LoggerPtr logger;

    Scope scope;

    bool active;
};

typedef boost::shared_ptr<InConnector> InConnectorPtr;

}
}
}

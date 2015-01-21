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

#include <rsc/runtime/Properties.h>

#include "../OutConnector.h"
#include "Bus.h"
#include "rsb/rsbexports.h"

namespace rsb {
namespace transport{
namespace inprocess {

/**
 * @author jmoringe
 */
class RSB_EXPORT OutConnector: public rsb::transport::OutConnector {
public:
    OutConnector();

    void setScope(const Scope& scope);

    void activate();
    void deactivate();

    void setQualityOfServiceSpecs(const QualityOfServiceSpec& specs);

    void handle(rsb::EventPtr e);

    static rsb::transport::OutConnector* create(const rsc::runtime::Properties& args);
private:
    Bus& bus;
};

}
}
}

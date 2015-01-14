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

#include <rsc/logging/Logger.h>

#include "../OutConnector.h"

#include "ConnectorBase.h"

#include "rsb/rsbexports.h"

namespace rsb {
namespace transport {
namespace socket {

/**
 * Instances of this connector class push events into @ref Bus objects
 * which take care of socket-based broadcast and delivery.
 *
 * @author jmoringe
 */
class RSB_EXPORT OutConnector: public ConnectorBase,
                               public rsb::transport::OutConnector {
public:
    /**
     * @copydoc ConnectorBase::ConnectorBase()
     */
    OutConnector(ConverterSelectionStrategyPtr converters,
                 const std::string&            host,
                 unsigned int                  port,
                 Server                        server,
                 bool                          tcpnodelay);

    virtual ~OutConnector();

    virtual void setScope(const Scope& scope);

    virtual void activate();
    virtual void deactivate();

    void setQualityOfServiceSpecs(const QualityOfServiceSpec& specs);

    void handle(EventPtr e);

    static transport::OutConnector* create(const rsc::runtime::Properties& args);
private:
    rsc::logging::LoggerPtr logger;
};

}
}
}

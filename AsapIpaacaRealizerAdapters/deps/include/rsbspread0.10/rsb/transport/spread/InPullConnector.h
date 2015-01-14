/* ============================================================
 *
 * This file is part of the rsb-spread project.
 *
 * Copyright (C) 2011, 2012, 2013 Jan Moringen <jmoringe@techfak.uni-bielefeld.de>
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

#include <rsc/runtime/Properties.h>

#include <rsb/transport/InPullConnector.h>
#include <rsb/transport/ConverterSelectingConnector.h>

#include "SpreadConnector.h"
#include "MessageHandler.h"

#include "rsb/transport/spread/rsbspreadexports.h"

namespace rsb {
namespace transport {
namespace spread {

/**
 * This class implements pull-style event receiving for the Spread
 * transport.
 *
 * @author jmoringe
 */
class RSBSPREAD_EXPORT InPullConnector: public transport::InPullConnector {
public:
    typedef rsb::transport::ConverterSelectingConnector<std::string>::ConverterSelectionStrategyPtr ConverterSelectionStrategyPtr;

    InPullConnector(ConverterSelectionStrategyPtr converters,
                    const std::string& host = defaultHost(),
                    unsigned int port = defaultPort());
    virtual ~InPullConnector();

    void printContents(std::ostream& stream) const;

    void setScope(const Scope& scope);

    void activate();
    void deactivate();

    void setQualityOfServiceSpecs(const QualityOfServiceSpec& specs);

    EventPtr raiseEvent(bool block);

    static transport::InPullConnector* create(const rsc::runtime::Properties& args);
private:
    rsc::logging::LoggerPtr logger;

    bool active;

    SpreadConnectorPtr connector;
    boost::shared_ptr<Scope> activationScope;
    MessageHandler processor;
};

}
}
}

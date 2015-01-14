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

#include <rsc/threading/TaskExecutor.h>

#include <rsb/transport/InPushConnector.h>
#include <rsb/transport/ConverterSelectingConnector.h>

#include "SpreadConnector.h"
#include "ReceiverTask.h"

#include "rsb/transport/spread/rsbspreadexports.h"

namespace rsb {
namespace transport {
namespace spread {

class ReceiverTask;

/**
 * This class implements push-style event receiving for Spread-based
 * transport.
 *
 * @author jmoringe
 */
class RSBSPREAD_EXPORT InPushConnector: public transport::InPushConnector,
                                  public transport::ConverterSelectingConnector<std::string> {
    friend class ReceiverTask;
public:
    InPushConnector(converter::ConverterSelectionStrategy<std::string>::Ptr converters,
                const std::string& host = defaultHost(),
                unsigned int port = defaultPort());
    virtual ~InPushConnector();

    void printContents(std::ostream& stream) const;

    void setScope(const Scope& scope);

    void activate();
    void deactivate();

    void setQualityOfServiceSpecs(const QualityOfServiceSpec& specs);

    void addHandler(eventprocessing::HandlerPtr handler);
    void removeHandler(eventprocessing::HandlerPtr handler);

    static transport::InPushConnector* create(const rsc::runtime::Properties& args);
private:
    rsc::logging::LoggerPtr logger;

    bool active;

    SpreadConnectorPtr connector;
    boost::shared_ptr<Scope> activationScope;

    rsc::threading::TaskExecutorPtr exec;
    boost::shared_ptr<ReceiverTask> rec;
};

}
}
}

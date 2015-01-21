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

#include <string>
#include <vector>

#include "../config/OptionHandler.h"
#include "../logging/Logger.h"

#include "Manager.h"

#include <rsc/rscexports.h>

namespace rsc {
namespace plugins {

/**
 * Instances of this class can be used to configure the #Manager based
 * on configuration options.
 *
 * Note that the configuration may be performed when the object is
 * destructed.
 *
 * @author jmoringe
 */
class RSC_EXPORT Configurator : public config::OptionHandler {
public:
    /**
     * Constructs a @c Configurator with default plugin search path @a
     * defaultPath.
     *
     * @param manager the manager to configure
     * @param defaultPath A #vector of #boost::filesystem::path
     *                    objects which should be installed as plugin
     *                    search path in case no plugin search path is
     *                    configured.
     */
    Configurator(ManagerPtr manager,
                 const std::vector<boost::filesystem::path>& defaultPath);
    virtual ~Configurator();

    void handleOption(const std::vector<std::string>& key,
                      const std::string& value);

private:
    logging::LoggerPtr logger;

    ManagerPtr manager;

    bool                                 pathSet;
    std::vector<boost::filesystem::path> defaultPath;
    std::set<std::string>                load;

    void addDefaultPath();

    std::vector<std::string> splitValue(const std::string& value) const;
};

}
}

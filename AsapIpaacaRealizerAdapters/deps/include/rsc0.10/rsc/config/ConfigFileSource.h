/* ============================================================
 *
 * This file is part of the RSC project
 *
 * Copyright (C) 2011 Jan Moringen
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

#include <iostream>
#include <map>
#include <string>
#include <vector>

#include "../logging/Logger.h"

#include "ConfigSource.h"
#include "rsc/rscexports.h"

namespace rsc {
namespace config {

/**
 * Objects of this class parse streams that contain configuration
 * information in "ini-file" syntax. Sections and keys are mapped to
 * hierarchical names.
 *
 * Currently, only files with line encoding styles of the respective platform
 * the code is run on are supported. Others may work but without guarantee.
 *
 * @author jmoringe
 */
class RSC_EXPORT ConfigFileSource : public ConfigSource {
public:
    ConfigFileSource(std::istream& stream);

    void provideOptions(OptionHandler& handler);

private:
    logging::LoggerPtr logger;

    std::istream& stream;
    std::string currentSection;

    std::map<std::vector<std::string>, std::string> options;

    bool getOption(std::string& name, std::string& value);
};

}
}

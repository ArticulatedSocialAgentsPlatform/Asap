/* ============================================================
 *
 * This file is a part of the RSC project.
 *
 * Copyright (C) 2012 by Johannes Wienke <jwienke at techfak dot uni-bielefeld dot de>
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

#include "../config/OptionHandler.h"
#include "rsc/rscexports.h"

namespace rsc {
namespace logging {

/**
 * A class which configures the logging tree using configuration subsystem of
 * RSC. The usual logging hierarchy is mapped onto the hierarchy of options in
 * the config interface with a prefix to prevent name clashes. This e.g. means
 * that the root logger can be configured with the default prefix
 * @c rsc.logging. This is called the root option.
 *
 * This class can be used multiple times. Newer options override older ones.
 *
 * @author jwienke
 */
class RSC_EXPORT OptionBasedConfigurator: public config::OptionHandler {
public:

    /**
     * Constructs a new configurator with a specified root option.
     *
     * @param rootOption the root in the option namespace containing the logger
     *                   configurations
     */
    OptionBasedConfigurator(const std::vector<std::string>& rootOption =
            getDefaultRootOption());

    /**
     * Destructor.
     */
    virtual ~OptionBasedConfigurator();

    /**
     * Returns the default config entry assumed for the root logger.
     *
     * @return config option representation of "rsc.logging"
     */
    static std::vector<std::string> getDefaultRootOption();

    /**
     * Returns the option root used by this configurator.
     *
     * @param config option representation for the root option which contains all
     *        logging configurations
     */
    std::vector<std::string> getRootOption() const;

    virtual void handleOption(const std::vector<std::string>& key,
            const std::string& value);

private:

    bool keyStartWithRoot(const std::vector<std::string>& key) const;
    std::string loggerNameFromKey(const std::vector<std::string>& key) const;
    std::string settingFromKey(const std::vector<std::string>& key) const;

    std::vector<std::string> normalizeKey(
            const std::vector<std::string>& key) const;

    std::vector<std::string> rootOption;

};

}
}


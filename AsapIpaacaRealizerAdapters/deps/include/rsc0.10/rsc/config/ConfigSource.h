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

#include "OptionHandler.h"
#include "rsc/rscexports.h"

namespace rsc {
namespace config {

/**
 * Implementations of this interface obtain configuration information
 * somewhere and pass individual configuration options to an
 * @ref OptionHandler instance.
 *
 * @author jmoringe
 */
class RSC_EXPORT ConfigSource {
public:

    virtual ~ConfigSource();

    /**
     * Implementations should pass all configuration options to
     * @a handler.
     *
     * @param handler The handler to which all option should be passed.
     * @throw std::invalid_argument may be thrown when the options this source
     *                              uses in the background are not well-formated
     */
    virtual void provideOptions(OptionHandler& handler) = 0;

protected:

    /**
     * Creates a key vector structure used in the options backend from a
     * dot-separated string.
     *
     * @param inputs dot-separated key
     * @param output key as vector of components
     * @throw invalid_argument invalid key that cannot be split.
     */
    void splitKeyAtDots(const std::string& input,
            std::vector<std::string>& output);

};

}
}

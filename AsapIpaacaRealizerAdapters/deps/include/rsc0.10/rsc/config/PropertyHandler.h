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

#include "../runtime/Properties.h"

#include "OptionHandler.h"
#include "rsc/rscexports.h"

namespace rsc {
namespace config {

/** This implementation of the @ref OptionHandler interface stores all
 * options it receives into a @ref runtime::Properties object.
 *
 * @author jmoringe
 */
class RSC_EXPORT PropertyHandler : public OptionHandler {
public:
    void handleOption(const std::vector<std::string>& key,
                      const std::string& value);
    /** Returns the @ref runtime::Properties object constructed by
     * this handler.
     *
     * @return A constant reference to the properties.
     */
    const runtime::Properties& getProperties() const;
private:
    runtime::Properties result;
};

}
}

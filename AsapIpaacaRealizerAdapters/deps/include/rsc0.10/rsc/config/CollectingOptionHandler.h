/* ============================================================
 *
 * This file is a part of the rsc project.
 *
 * Copyright (C) 2013 by Johannes Wienke <jwienke at techfak dot uni-bielefeld dot de>
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

/**
 * An OptionHandler which collects the options in an instance of
 * @ref Properties.
 *
 * @author jwienke
 */
class RSC_EXPORT CollectingOptionHandler: public OptionHandler {
public:

    /**
     * Creates a new handler which uses an empty @ref Properties instance for
     * collecting options.
     */
    CollectingOptionHandler();

    /**
     * Creates a new handler which will use a copy of the passed @ref Properties
     * instance.
     *
     * @attention The @ref Properties instance passed into this constructor will
     *            not be modified directly. After letting this handler process
     *            options you need to get the modified version using
     *            #getOptions.
     * @param properties A properties instance to start from. Won't be modified
     *                   directly. A copy will be used.
     */
    CollectingOptionHandler(const rsc::runtime::Properties& properties);

    void handleOption(const std::vector<std::string>& key,
            const std::string& value);

    /**
     * Returns the options this handler has collected so far.
     *
     * @return Copy of the @ref Properties object used for collecting options.
     */
    rsc::runtime::Properties getOptions() const;

private:
    rsc::runtime::Properties options;

};

}
}

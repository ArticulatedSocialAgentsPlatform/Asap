/* ============================================================
 *
 * This file is a part of the RSB project
 *
 * Copyright (C) 2010 by Sebastian Wrede <swrede at techfak dot uni-bielefeld dot de>
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

#include "rsb/rsbexports.h"

namespace rsb {
namespace filter {

/**
 * A class to encapsulate enum constants that specify changes of a Filter for
 * FilterObserver instances.
 *
 * @author swrede
 */
class RSB_EXPORT FilterAction {
public:
    /**
     * Possible actions with filters.
     *
     * @note There is not update action because filters would then need a more
     *       detailed observation model. Instead, the assumption is that a
     *       filter's settings will not change after it has been notified to a
     *       FilterObserver.
     * @author swrede
     */
    enum Types {
        /**
         * A filter shall be added to the FilterObserver.
         */
        ADD,
        /**
         * A filter shall be remove from the observer.
         */
        REMOVE
    };
};

}
}


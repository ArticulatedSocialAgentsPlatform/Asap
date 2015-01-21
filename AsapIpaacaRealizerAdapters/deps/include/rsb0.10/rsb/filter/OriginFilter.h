/* ============================================================
 *
 * This file is part of the RSB project.
 *
 * Copyright (C) 2011 Jan Moringen <jmoringe@techfak.uni-bielefeld.de>
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

#include <rsc/misc/UUID.h>

#include "Filter.h"
#include "rsb/rsbexports.h"

namespace rsb {
namespace filter {

/**
 * This filter matches events that originate from a particular
 * participant.
 *
 * @author jmoringe
 */
class RSB_EXPORT OriginFilter: public Filter {
public:
    /**
     * Creates a new origin filter that matches event originating from
     * @a origin.
     *
     * @param origin Id of the participant from which matching events
     *               have to originate.
     * @param invert If true, events match if they do @b not originate
     *               from @a origin.
     */
    OriginFilter(const rsc::misc::UUID& origin,
                 bool                   invert = false);

    rsc::misc::UUID getOrigin() const;

    bool isInverted() const;

    bool match(EventPtr e);

    void notifyObserver(FilterObserverPtr fo, FilterAction::Types at);
private:
    rsc::misc::UUID origin;
    bool            invert;
};

}
}

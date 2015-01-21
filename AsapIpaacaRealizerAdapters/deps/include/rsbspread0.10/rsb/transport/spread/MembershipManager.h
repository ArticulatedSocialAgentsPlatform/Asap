/* ============================================================
 *
 * This file is part of the rsb-spread project.
 *
 * Copyright (C) 2010 by Sebastian Wrede <swrede at techfak dot uni-bielefeld dot de>
 * Copyright (C) 2013 Jan Moringen <jmoringe@techfak.uni-bielefeld.de>
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

#include "SpreadGroup.h"

#include <map>
#include <string>

#include <boost/shared_ptr.hpp>
#include <boost/thread.hpp>

#include <rsc/logging/Logger.h>

#include "SpreadConnection.h"

#include "rsb/transport/spread/rsbspreadexports.h"

#pragma once

namespace rsb {
namespace transport {
namespace spread {

typedef std::map<std::string, std::pair<SpreadGroupPtr, int> > GroupMap;

/**
 * Reference counting class for Spread group memberships.
 *
 * @author swrede
 * @todo think about adding SpreadConnectionPtr as a member, offering
 *       methods that operate on this connection only
 */
class RSBSPREAD_EXPORT MembershipManager {
public:
    MembershipManager();
    virtual ~MembershipManager();

    /**
     * Joins the given Spread group if not previously done
     * and increments reference count for this group by one.
     *
     * @param group group name to join
     * @param s spread connection to join on
     */
    void join(std::string group, SpreadConnectionPtr s);

    /**
     * Decrements the reference count for the given Spread
     * group identifier. If reference count for this identifier
     * drops to zero, the corresponding Spread group is left.
     *
     * @param group group name to leave
     * @param s spread connection to leave on
     */
    void leave(std::string group, SpreadConnectionPtr s);

private:
    rsc::logging::LoggerPtr logger;
    boost::recursive_mutex groupsMutex;
    boost::shared_ptr<GroupMap> groups;
};

typedef boost::shared_ptr<MembershipManager> MembershipManagerPtr;

}
}
}

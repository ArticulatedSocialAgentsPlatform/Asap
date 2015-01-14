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

#pragma once

#include <string>

#include <boost/shared_ptr.hpp>

#include <rsc/logging/Logger.h>

#include "SpreadConnection.h"
#include "rsb/transport/spread/rsbspreadexports.h"

namespace rsb {
namespace transport {
namespace spread {

/**
 * @author swrede
 */
class RSBSPREAD_EXPORT SpreadGroup {
public:
    SpreadGroup(const std::string& n);
    virtual ~SpreadGroup();

    std::string getName() const;
    // MembershipList getMembers();

    /**
     * Joins it's group on the given connection.
     *
     * @param con the connection to join on
     * @throw CommException spread error joining
     */
    virtual void join(SpreadConnectionPtr con);
    /**
     * Leaves it's group on the given connection.
     *
     * @param con the connection to leave on
     * @throw CommException spread error leaving
     */
    virtual void leave(SpreadConnectionPtr con);

private:

    void handleRetCode(const int& code, const std::string& actionName);

    std::string name;
    rsc::logging::LoggerPtr logger;
    SpreadConnectionPtr con;
};

typedef boost::shared_ptr<SpreadGroup> SpreadGroupPtr;

}
}
}

/* ============================================================
 *
 * This file is part of the RSB project
 *
 * Copyright (C) 2011, 2012, 2013 Jan Moringen <jmoringe@techfak.uni-bielefeld.de>
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
#include <set>

#include "rsb/rsbexports.h"

namespace rsb {
namespace transport {

RSB_EXPORT void registerDefaultTransports();

/**
 * Transport directions. Can be combined via bitwise or.
 */
enum Directions {
    IN_PUSH = 0x01,
    IN_PULL = 0x02,
    OUT     = 0x04
};

/**
 * Returns the names of all available transports which support @a
 * requiredDirections.
 *
 * @param requiredDirections One or more of @ref Directions.
 * @return The set of names of the available transports.
 */
RSB_EXPORT std::set<std::string> getAvailableTransports(unsigned int requiredDirections);

/**
 * Returns @c true if @a transportName names a transport which is
 * available and supports @a requiredDirections .
 *
 * @param requiredDirections One or more of @ref Directions.
 * @return @c true if the requested transport is available, @c false
 *         otherwise.
 */
RSB_EXPORT bool isAvailable(const std::string& transportName,
                            unsigned int       requiredDirections);

/**
 * Returns @c true if @a transportName names a remote transport.
 *
 * @return @c true if the transport implements remote communication,
 *         @c false otherwise.
 *
 * @throw rsc::runtime::NoSuchObject If @a transportName does not name
 *                                   an available transport.
 */
RSB_EXPORT bool isRemote(const std::string& transportName);

}
}

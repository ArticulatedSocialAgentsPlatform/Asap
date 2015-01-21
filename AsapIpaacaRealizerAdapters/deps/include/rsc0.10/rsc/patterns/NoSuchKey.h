/* ============================================================
 *
 * This file is part of the RSC project
 *
 * Copyright (C) 2010, 2011 Jan Moringen
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

#include <boost/any.hpp>

#include "../runtime/NoSuchObject.h"
#include "rsc/rscexports.h"

namespace rsc {
namespace patterns {

/**
 * This exception is thrown if a specified key does not exist in an associative
 * container.
 *
 * @author Jan Moringen <jmoringe@techfak.uni-bielefeld.de>
 */
class RSC_EXPORT NoSuchKey: public runtime::NoSuchObject {
public:

    /**
     * Constructs a new no_such_key exception which indicates the that an
     * invalid key was used to query an associative container.
     *
     * @param message A string describing the invalid access.
     */
    NoSuchKey(const std::string& message);

};

}
}

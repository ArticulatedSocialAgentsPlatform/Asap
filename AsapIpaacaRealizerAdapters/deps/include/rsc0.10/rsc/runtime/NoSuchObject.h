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

#include <stdexcept>
#include <string>

#include "rsc/rscexports.h"

namespace rsc {
namespace runtime {

/**
 * This exception is thrown if a specified object does not exist.
 *
 * @author Jan Moringen <jmoringe@techfak.uni-bielefeld.de>
 * @todo base class could be std::logic_error
 */
class RSC_EXPORT NoSuchObject: public std::runtime_error {
public:
    /**
     * Constructs a new @a NoSuchObject exception which indicates the absence of
     * the object specified by @a object.
     *
     * @param object The object which was specified but did not exist.
     */
    NoSuchObject(const std::string& object);
};

}
}

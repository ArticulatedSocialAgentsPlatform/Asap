/* ============================================================
 *
 * This file is a part of the RSC project
 *
 * Copyright (C) 2011 by Johannes Wienke <jwienke at techfak dot uni-bielefeld dot de>
 * Copyright (C) 2012 Jan Moringen <jmoringe@techfak.uni-bielefeld.de>
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

#include "rsc/rscexports.h"

#define RSC_VERSION_MAJOR 0
#define RSC_VERSION_MINOR 10
#define RSC_VERSION_PATCH 0

// We have to resort to this trick to avoid invalid octal integer
// literals.
#define RSC_VERSION_NUMERIC (1001000 - 1000000)

#define RSC_ABI_VERSION 4

namespace rsc {

/**
 * Version information for the library.
 *
 * @author jwienke
 */
class RSC_EXPORT Version {
public:

    static unsigned int major();
    static unsigned int minor();
    static unsigned int patch();

    static unsigned int numeric();

    static std::string string();

    static unsigned int abi();

    static std::string buildId();
    static std::string buildDate();

    static std::string buildString();

private:
    Version();

};

}

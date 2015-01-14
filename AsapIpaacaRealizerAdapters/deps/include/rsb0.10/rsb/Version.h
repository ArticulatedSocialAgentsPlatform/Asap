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

#include <string>

#include <boost/filesystem/path.hpp>

#include "rsb/rsbexports.h"

#define RSB_VERSION_MAJOR 0
#define RSB_VERSION_MINOR 10
#define RSB_VERSION_PATCH 0

#define RSB_VERSION_NUMERIC (1001000 - 1000000)

#define RSB_ABI_VERSION 2

namespace rsb {

class RSB_EXPORT Version {
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

    static boost::filesystem::path installPrefix();
    static boost::filesystem::path libdir();
private:
    Version();

};

}

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
 * ============================================================ */

#pragma once

#include <stdexcept>
#include <string>
#include <iostream>

#include <boost/cstdint.hpp>
#include <boost/format.hpp>

namespace rsb {
namespace transport {
namespace socket {

enum Server {
    SERVER_NO   = 0,
    SERVER_YES  = 1,
    SERVER_AUTO = 2
};

template <typename Ch, typename Tr>
std::basic_istream<Ch, Tr>& operator>>(std::basic_istream<Ch, Tr>& stream,
                                       Server&                     value) {
    // Read one whitespace-delimited token.
    std::basic_string<Ch, Tr> string;
    stream >> string;

    // Interpret the value.
    if (string == "0") {
        value = SERVER_NO;
    } else if (string == "1") {
        value = SERVER_YES;
    } else if (string == "auto") {
        value = SERVER_AUTO;
    } else {
        throw std::invalid_argument(boost::str(boost::format("Invalid server/client specification: %1%")
                                               % string));
    }

    return stream;
}

template <typename Ch, typename Tr>
std::basic_ostream<Ch, Tr>& operator<<(std::basic_ostream<Ch, Tr>& stream,
                                       const Server&               value) {
    switch (value) {
    case SERVER_NO:
        stream << "0";
        break;
    case SERVER_YES:
        stream << "1";
        break;
    case SERVER_AUTO:
        stream << "auto";
        break;
    }

    return stream;
}

extern const std::string DEFAULT_HOST;

extern const boost::uint16_t DEFAULT_PORT;

}
}
}

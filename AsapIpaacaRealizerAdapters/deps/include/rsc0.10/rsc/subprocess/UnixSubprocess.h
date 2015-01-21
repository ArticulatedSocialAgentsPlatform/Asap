/* ============================================================
 *
 * This file is a part of RSC project
 *
 * Copyright (C) 2010 by Johannes Wienke <jwienke at techfak dot uni-bielefeld dot de>
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

#include "Subprocess.h"

#include <sys/types.h>
#include <unistd.h>

#include "../logging/Logger.h"

namespace rsc {
namespace subprocess {

/**
 * Unix subprocess implementation.
 *
 * @author jwienke
 * @todo somehow this crashes and does not give an exception if you try to
 *       start a binary that does not exist
 */
class UnixSubprocess: public Subprocess {
public:
    UnixSubprocess(const std::string& command,
            const std::vector<std::string>& args);
    virtual ~UnixSubprocess();

private:

    logging::LoggerPtr logger;

    std::string command;

    pid_t pid;

    char** args;
    size_t argLen;

};

}
}


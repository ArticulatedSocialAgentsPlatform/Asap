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

#include <string>
#include <vector>

#include <boost/noncopyable.hpp>
#include <boost/shared_ptr.hpp>

#include "rsc/rscexports.h"

namespace rsc {
namespace subprocess {

class Subprocess;
typedef boost::shared_ptr<Subprocess> SubprocessPtr;

/**
 * A wrapper to call a different command as a subprocess and control its
 * lifecycle. This class uses the RAII idiom to manage the subprocces. This
 * means that the called process will be terminated on destruction of an
 * instance of this class.
 *
 * This class is intended to provide a platform-independent way of controlling
 * a command. Hence, a factory method is used instead of a normal constructor
 * to automatically instantiate the platform-specific implementations.
 *
 * Subclasses ensure that the process is started in the constructor and
 * terminated in the destructor.
 *
 * @author jwienke
 */
class RSC_EXPORT Subprocess: private boost::noncopyable {
public:
    virtual ~Subprocess();

    /**
     * Creates a new subprocess for the given command with the specified
     * arguments.
     *
     * @param command command to call
     * @param args arguments for the command. The command itself must not be
     *             given. It is automatically passed by this class.
     * @return subprocess instance
     * @throw std::runtime_error error starting the command
     */
    static SubprocessPtr newInstance(const std::string& command,
            const std::vector<std::string>& args = std::vector<std::string>());

protected:
    Subprocess();

};

}
}


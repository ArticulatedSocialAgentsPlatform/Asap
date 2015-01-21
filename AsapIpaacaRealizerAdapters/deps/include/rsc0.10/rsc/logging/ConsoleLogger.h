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

#include <boost/thread/recursive_mutex.hpp>

#include "Logger.h"
#include "rsc/rscexports.h"

namespace rsc {
namespace logging {

/**
 * A simple logger that uses cout and cerr for logging.
 *
 * @author jwienke
 */
class RSC_EXPORT ConsoleLogger: public Logger {
public:

    /**
     * Creates a new logger with the given name and level INFO.
     *
     * @param name name of the logger
     */
    ConsoleLogger(const std::string& name);
    ConsoleLogger(const std::string& name, const Level& level);
    virtual ~ConsoleLogger();

    Level getLevel() const;
    void setLevel(const Level& level);
    std::string getName() const;
    void setName(const std::string& name);

    void log(const Level& level, const std::string& msg);

private:

    /**
     * Prints a generic header for this logger to the stream. Acquire the lock
     * before calling this method.
     *
     * @param stream stream to print on
     * @param level the level of the header to generate
     * @return the stream passed in
     */
    std::ostream& printHeader(std::ostream& stream, const Level& level);

    std::string name;
    Level level;

    mutable boost::recursive_mutex mutex;

};

}
}


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

#include "Logger.h"
#include "../misc/Registry.h"
#include "rsc/rscexports.h"

namespace rsc {
namespace logging {

/**
 * Represents a logging adapter available to the rsc logging system through the
 * configuration. Reentrancy is required.
 *
 * @author jwienke
 */
class RSC_EXPORT LoggingSystem {
public:

    virtual ~LoggingSystem();

    /**
     * Returns a unique key describing the name of the logging system.
     *
     * @return name of the logging system
     */
    virtual std::string getRegistryKey() const = 0;

    /**
     * Factory method to create a new system-specific logger. Given parameters
     * to this method are assumed to be set on the returned logger. The caller
     * is responsible to not request a logger with the same name multiple times.
     * Otherwise it is legal to return a second instance in this method.
     *
     * @param name name of the new logger
     * @return new logger instance
     */
    virtual LoggerPtr createLogger(const std::string& name) = 0;

};

RSC_EXPORT rsc::misc::Registry<LoggingSystem>* loggingSystemRegistry();

}
}


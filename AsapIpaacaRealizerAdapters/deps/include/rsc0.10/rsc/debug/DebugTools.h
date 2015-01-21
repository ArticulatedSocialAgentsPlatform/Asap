/* ============================================================
 *
 * This file is a part of the RSC project.
 *
 * Copyright (C) 2011 by Johannes Wienke <jwienke at techfak dot uni-bielefeld dot de>
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
#include <sstream>

#include <boost/shared_ptr.hpp>

#include "../runtime/TypeStringTools.h"
#include "rsc/rscexports.h"

namespace rsc {
namespace debug {

class DebugTools;
typedef boost::shared_ptr<DebugTools> DebugToolsPtr;

/**
 * A class providing platform-specific runtime debugging tools, e.g. to generate
 * a backtrace.
 */
class RSC_EXPORT DebugTools {
public:

    virtual ~DebugTools();

    static DebugToolsPtr newInstance();

    /**
     * Generates a string representation of the current thread's backtrace.
     *
     * @return backtrace string as vector of method calls
     */
    virtual std::vector<std::string> createBacktrace(
            const unsigned int& maxElements = 20) = 0;

    std::string formatBacktrace(const std::vector<std::string>& trace);

    /**
     * Generates a string giving verbose information about an exception in the
     * given context. Use this immediately after catching the exception.
     *
     * @param e the exception to analyze
     * @tparam ExceptionType type of the caught exception
     */
    template<class ExceptionType>
    std::string exceptionInfo(const ExceptionType& e) {

        std::stringstream s;
        s << "Exception of type: " << rsc::runtime::typeName(e) << std::endl;
        s << "Backtrace:" << std::endl;
        std::vector<std::string> trace = createBacktrace();
        s << formatBacktrace(trace);
        return s.str();

    }

protected:

    DebugTools();

};

}
}


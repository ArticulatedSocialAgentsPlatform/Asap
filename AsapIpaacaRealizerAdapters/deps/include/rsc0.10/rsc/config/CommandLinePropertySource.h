/* ============================================================
 *
 * This file is a part of the rsc project.
 *
 * Copyright (C) 2013 by Johannes Wienke <jwienke at techfak dot uni-bielefeld dot de>
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

#include "ConfigSource.h"
#include "../logging/Logger.h"
#include "rsc/rscexports.h"

namespace rsc {
namespace config {

/**
 * A source for options from the command line using a -D java properties-like
 * syntax. The option key can be specified and in the default configuration
 * unknown or ill-formatted options are ignored so that it is safe to use this
 * source in conjunction with other command line handling routines.
 *
 * @author jwienke
 */
class RSC_EXPORT CommandLinePropertySource: public ConfigSource {
public:

    /**
     * Creates a new source.
     *
     * @param argc Number of arguments given to the program
     * @param argv Argument vector given to the program
     * @param reportSyntaxErrors if @c true, syntax errors in the options will
     *                           raise exceptions in #provideOptions, otherwise
     *                           they will be ignored. Default: @c false
     * @param option the command line option key used to parse the properties,
     *               defaults to 'D'.
     */
    CommandLinePropertySource(int          argc,
                              const char** argv,
                              bool         reportSyntaxErrors = false,
                              char         option             = 'D');
    virtual ~CommandLinePropertySource();

    void provideOptions(OptionHandler& handler);

private:
    logging::LoggerPtr logger;

    int                argc;
    const char**       argv;
    bool               reportSyntaxErrors;
    char               option;

};

}
}

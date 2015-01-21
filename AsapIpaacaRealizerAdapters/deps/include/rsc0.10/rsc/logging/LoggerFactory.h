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

#include <map>
#include <vector>

#include <boost/noncopyable.hpp>
#include <boost/thread/recursive_mutex.hpp>
#include <boost/weak_ptr.hpp>
#include <boost/shared_ptr.hpp>

#include "Logger.h"
#include "LoggerTreeNode.h"
#include "LoggerProxy.h"
#include "LoggingSystem.h"
#include "../misc/Registry.h"
#include "../patterns/Singleton.h"
#include "../misc/langutils.h"
#include "rsc/rscexports.h"

namespace rsc {

/**
 * Provides a hierarchical logging system with the possibility to install
 * different backends, which are instances of LoggingSystem. The hierarchy is
 * completely maintained by LoggerFactory, hence logging systems only need to
 * provide Logger instances which are configured externally according to the
 * hierarchy.
 *
 * Conceptually, the hierarchy is based on log4cxx/log4j. We have a root logger
 * with empty string as name. All other loggers are (indirect) children of this
 * logger. Hierarchies are separated by a '.' in the logger name. At startup
 * only the root logger has an assigned level. All other loggers inherit this
 * level if not one of their parents also has a level assigned. Generally,
 * the effective level of a logger is the assigned level of the earliest parent
 * of the logger which actually has an assigned level.
 *
 * In detail, the logger names have the following conventions and rules:
 *  - name are handled case-insensitive
 *  - the part after the last . must not be called "system" or "level"
 *    (case-insensitive)
 *  - you should not include = characters and any kind of line breaks as they
 *    might interfere with configuration system
 *
 * To install new LoggingSystem instances, register them in #loggingSystemRegistry.
 * The selection of a logging system can be triggered through
 * LoggerFactory#reselectLoggingSystem using a string as a hint.
 *
 * As the default, a simple cout- / cerr-based LoggingSystem called
 * ConsoleLoggingSystem is provided.
 */
namespace logging {

/**
 * Factory to create logger instances. On singleton creation selects a logger
 * according to the heuristic mentioned for #reselectLoggingSystem.
 *
 * @author jwienke
 */
class RSC_EXPORT LoggerFactory: public patterns::Singleton<LoggerFactory> {
private:
    LoggerFactory();

public:

    friend class patterns::Singleton<LoggerFactory>;

    virtual ~LoggerFactory();

    /**
     * Get a logger for the given name. If a logger with this name already
     * exists, the existing instance is returned.
     *
     * Receiving a logger is a quite expensive operation if a logger with the
     * specified name did not exist before at runtime. Hence, do not use
     * constantly changing names for loggers with a short lifetime.
     *
     * @param name name of the logger, empty string means root logger
     * @return logger instance
     */
    LoggerPtr getLogger(const std::string& name = "");

    /**
     * Simple hack to reconfigure all known loggers and new instances with a
     * logging level. Only loggers which had a manually assigned level will
     * actually be assigned with the new level. All others use the inheritance
     * tree to get a level. Effectively this will result in all loggers
     * appearing with the same level but semantics for changing certain levels
     * in the logger tree are preserved.
     *
     * @param level new level for all loggers
     */
    void reconfigure(const Logger::Level& level);

    /**
     * Reconfigures the logging system from a configuration file.
     *
     * @param fileName name of the configuration file
     * @throw std::invalid_argument invalid file path
     */
    void reconfigureFromFile(const std::string& fileName);

    /**
     * Reselected the automatically chosen logging system to adapt to newly
     * available ones. A hint can be given on the name of the logging system to
     * select.
     *
     * If no hint is given the first found logging system which is not the
     * default will be selected. If no such system exists, the default is used.
     * The name hint overrides these settings if a logging system matching the
     * hint is found.
     *
     * This method will NOT change previously gathered loggers.
     *
     * @param nameHint hint for the name of the logging system to select. If the
     *                 string is empty, it will not be used. If it is
     *                 #DEFAULT_LOGGING_SYSTEM, the default logging system will
     *                 be selected even if other systems are available.
     */
    void reselectLoggingSystem(const std::string& nameHint = "");

    /**
     * Parameter to pass as name hint to #reselectLoggingSystem for selecting
     * the default system.
     */
    static const std::string DEFAULT_LOGGING_SYSTEM;

    /**
     * Default level when the system is used without prior initialization.
     */
    static const Logger::Level DEFAULT_LEVEL;

    /**
     * Returns the name of the currently selected logging system.
     *
     * @return logging system name
     */
    std::string getLoggingSystemName();

    /**
     * Do not use this, for testing only!
     */
    void clearKnownLoggers();

private:

    class ReconfigurationVisitor;
    class ReselectVisitor;

    LoggerProxyPtr createLogger(const LoggerTreeNode::NamePath& path,
            LoggerTreeNodePtr node);

    boost::shared_ptr<LoggingSystem> loggingSystem;

    boost::recursive_mutex mutex;
    LoggerTreeNodePtr loggerTree;

};

}
}


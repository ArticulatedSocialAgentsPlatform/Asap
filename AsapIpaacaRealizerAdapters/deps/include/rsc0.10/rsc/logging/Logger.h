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
#include <ostream>
#include <sstream>

#include <boost/noncopyable.hpp>
#include <boost/shared_ptr.hpp>

#include "rsc/rscexports.h"

namespace rsc {
namespace logging {

class Logger;
typedef boost::shared_ptr<Logger> LoggerPtr;

/**
 * Interface for logging adapters that can be used with RSC. The interface
 * mimics the log4cxx interface. Loggers must be <b>reentrant</b>!
 *
 * Even though loggers are hierarchical, this hierarchy is maintained
 * externally. Hence, implementations of this class do not need to interpret
 * the name they get assigned and each Logger always gets a level.
 *
 * @author jwienke
 */
class RSC_EXPORT Logger: public boost::noncopyable {
public:

    /**
     * Possible logging levels. The more messages shall be logged, the higher
     * is the int representation of the level.
     *
     * @author jwienke
     */
    enum Level {
        LEVEL_ALL = 0x11111111,
        LEVEL_TRACE = 60,
        LEVEL_DEBUG = 50,
        LEVEL_INFO = 40,
        LEVEL_WARN = 30,
        LEVEL_ERROR = 20,
        LEVEL_FATAL = 10,
        LEVEL_OFF = 0
    };

    virtual ~Logger();

    /**
     * Returns a logger for the given name. The initial log level of the logger
     * is defined by the global configuration.
     *
     * Receiving a logger is a quite expensive operation if a logger with the
     * specified name did not exist before at runtime. Hence, do not use
     * constantly changing names for loggers with a short lifetime.
     *
     * @param name name of the logger
     * @return logger
     */
    static LoggerPtr getLogger(const std::string& name);

    /**
     * @name logger configuration
     */
    //@{

    /**
     * Returns the currently defined level of the logger.
     *
     * @return current level of the logger
     */
    virtual Level getLevel() const = 0;

    /**
     * Defines the level of the logger
     *
     * @param level new level
     */
    virtual void setLevel(const Level& level) = 0;

    /**
     * Returns the hierarchical name of the logger.
     *
     * @return name of the logger
     */
    virtual std::string getName() const = 0;

    /**
     * Sets the name of the logger.
     *
     * @param name new name
     */
    virtual void setName(const std::string& name) = 0;

    //@}

    /**
     * @name level-based logging functions
     *
     * Default implementations of this method map to #log calls.
     */
    //@{

    /**
     * Logs a message with trace logging level if this level is enabled.
     *
     * @param msg message to log
     */
    virtual void trace(const std::string& msg);

    /**
     * Logs a message with debug logging level if this level is enabled.
     *
     * @param msg message to log
     */
    virtual void debug(const std::string& msg);

    /**
     * Logs a message with info logging level if this level is enabled.
     *
     * @param msg message to log
     */
    virtual void info(const std::string& msg);

    /**
     * Logs a message with warn logging level if this level is enabled.
     *
     * @param msg message to log
     */
    virtual void warn(const std::string& msg);

    /**
     * Logs a message with error logging level if this level is enabled.
     *
     * @param msg message to log
     */
    virtual void error(const std::string& msg);

    /**
     * Logs a message with fatal logging level if this level is enabled.
     *
     * @param msg message to log
     */
    virtual void fatal(const std::string& msg);

    //@}

    /**
     * Logs a message with the given level if it is enabled.
     *
     * @param level level for the message
     * @param msg message to log
     */
    virtual void log(const Level& level, const std::string& msg) = 0;

    /**
     * @name level checks
     *
     * Methods are mapped to calls on #getLevel.
     */
    //@{
    virtual bool isTraceEnabled() const;
    virtual bool isDebugEnabled() const;
    virtual bool isInfoEnabled() const;
    virtual bool isWarnEnabled() const;
    virtual bool isErrorEnabled() const;
    virtual bool isFatalEnabled() const;
    virtual bool isEnabledFor(const Level& level) const;
    //@}

};

RSC_EXPORT std::ostream& operator<<(std::ostream& stream, const Logger::Level& level);

}
}

/**
 * @name logging utility macros with stream semantics
 */
//@{

#define RSCTRACE(logger, msg) \
    if (logger->isTraceEnabled()) { \
        std::stringstream s; \
        s << msg; \
        logger->trace(s.str()); \
    }

#define RSCDEBUG(logger, msg) \
    if (logger->isDebugEnabled()) { \
        std::stringstream s; \
        s << msg; \
        logger->debug(s.str()); \
    }

#define RSCINFO(logger, msg) \
    if (logger->isInfoEnabled()) { \
        std::stringstream s; \
        s << msg; \
        logger->info(s.str()); \
    }

#define RSCWARN(logger, msg) \
    if (logger->isWarnEnabled()) { \
        std::stringstream s; \
        s << msg; \
        logger->warn(s.str()); \
    }

#define RSCERROR(logger, msg) \
    if (logger->isErrorEnabled()) { \
        std::stringstream s; \
        s << msg; \
        logger->error(s.str()); \
    }

#define RSCFATAL(logger, msg) \
    if (logger->isFatalEnabled()) { \
        std::stringstream s; \
        s << msg; \
        logger->fatal(s.str()); \
    }

//@}

/**
 * @name expectation logging utility macros with stream semantics
 *
 * The macros only log if the condition fails. However they do not let a program
 * terminate and if the logger is not active, nothing will happen.
 */
//@{

#define RSCTRACE_EXPECT(condition, logger, msg) \
    if (!(condition) && logger->isTraceEnabled()) { \
        std::stringstream s; \
        s << msg << "\nfailed condition: " << #condition; \
        logger->trace(s.str()); \
    }

#define RSCDEBUG_EXPECT(condition, logger, msg) \
    if (!(condition) && logger->isDebugEnabled()) { \
        std::stringstream s; \
        s << msg << "\nfailed condition: " << #condition; \
        logger->debug(s.str()); \
    }

#define RSCINFO_EXPECT(condition, logger, msg) \
    if (!(condition) && logger->isInfoEnabled()) { \
        std::stringstream s; \
        s << msg << "\nfailed condition: " << #condition; \
        logger->info(s.str()); \
    }

#define RSCWARN_EXPECT(condition, logger, msg) \
    if (!(condition) && logger->isWarnEnabled()) { \
        std::stringstream s; \
        s << msg << "\nfailed condition: " << #condition; \
        logger->warn(s.str()); \
    }

#define RSCERROR_EXPECT(condition, logger, msg) \
    if (!(condition) && logger->isErrorEnabled()) { \
        std::stringstream s; \
        s << msg << "\nfailed condition: " << #condition; \
        logger->error(s.str()); \
    }

#define RSCFATAL_EXPECT(condition, logger, msg) \
    if (!(condition) && logger->isFatalEnabled()) { \
        std::stringstream s; \
        s << msg << "\nfailed condition: " << #condition; \
        logger->fatal(s.str()); \
    }

//@}

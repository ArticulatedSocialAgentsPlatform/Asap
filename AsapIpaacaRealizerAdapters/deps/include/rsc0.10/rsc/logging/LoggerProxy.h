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

#include <boost/enable_shared_from_this.hpp>
#include <boost/shared_ptr.hpp>

#include "Logger.h"

namespace rsc {
namespace logging {

/**
 * A proxy to an instance of Logger, which provides the same interface but
 * allows to exchange the underlying logger at runtime.
 *
 * @author jwienke
 */
class LoggerProxy: public Logger, public boost::enable_shared_from_this<
        LoggerProxy> {
public:

    /**
     * Interface for callbacks which are invoked when someone calls setLevel on
     * this proxy. This callback also needs to ensure that the level of the
     * called logger is set. LoggerProxy completely delegates the task of
     * assigning levels to the callback.
     *
     * @author jwienke
     * @note Convert this to a real observer pattern when more feedback is
     *       required than only the level change
     */
    class SetLevelCallback {
    public:
        virtual ~SetLevelCallback();
        virtual void call(boost::shared_ptr<LoggerProxy> proxy,
                const Logger::Level& level) = 0;
    };
    typedef boost::shared_ptr<SetLevelCallback> SetLevelCallbackPtr;

    /**
     * Constructor.
     * @param logger the initial logger to hide behind this proxy
     * @param callback callback invoked when #setLevel is called
     */
    LoggerProxy(LoggerPtr logger, SetLevelCallbackPtr callback);
    virtual ~LoggerProxy();

    /**
     * @name logger interface
     */
    //@{

    virtual Logger::Level getLevel() const;
    virtual void setLevel(const Logger::Level& level);
    virtual std::string getName() const;
    virtual void setName(const std::string& name);
    virtual void log(const Logger::Level& level, const std::string& msg);

    //@}

    /**
     * Returns the hidden logger behind this proxy.
     * @return hidden logger instance
     */
    LoggerPtr getLogger() const;

    /**
     * (Re-)Sets the logger to be hidden behind this proxy. This may happen at
     * any time and from any thread.
     *
     * @param logger new logger to hide
     */
    void setLogger(LoggerPtr logger);

private:

    /**
     * The hidden logger. We rely on the locking of boost::shared_ptr for access
     * to this instance, even on reselection.
     */
    LoggerPtr logger;

    SetLevelCallbackPtr callback;

};

typedef boost::shared_ptr<LoggerProxy> LoggerProxyPtr;

}
}

/* ============================================================
 *
 * This file is part of the rsb-spread project.
 *
 * Copyright (C) 2011, 2012, 2013 Jan Moringen <jmoringe@techfak.uni-bielefeld.de>
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

#include <boost/cstdint.hpp>
#include <boost/shared_ptr.hpp>
#include <boost/thread/recursive_mutex.hpp>
#include <boost/date_time/posix_time/ptime.hpp>

#include <rsc/logging/Logger.h>
#include <rsc/threading/PeriodicTask.h>
#include <rsc/threading/ThreadedTaskExecutor.h>

#include <rsb/protocol/Notification.h>
#include <rsb/protocol/FragmentedNotification.h>

#include "rsb/transport/spread/rsbspreadexports.h"

namespace rsb {
namespace transport {
namespace spread {

/**
 * Instances of this class store fragments of partially received,
 * fragmented notifications for later assembly.
 *
 * @author swrede
 */
class RSBSPREAD_EXPORT Assembly {
public:

    Assembly(rsb::protocol::FragmentedNotificationPtr n);
    ~Assembly();

    /**
     * Returns the completed notification built from all fragments.
     *
     * @return complete notification with all data
     */
    rsb::protocol::NotificationPtr getCompleteNotification() const;

    /**
     * Adds a newly received fragment to this Assembly and tells whether this
     * completed the assembly.
     *
     * @param n fragment to add
     * @return @c true if the assembly is now completed, else @c false
     * @throw protocol::ProtocolException if there is already a fragment in this
     *                                    Assembly with the same fragment number
     */
    bool add(rsb::protocol::FragmentedNotificationPtr n);

    bool isComplete() const;

    /**
     * Age of the assembly as seconds. The age is the elapsed time since this
     * instance was created.
     *
     * @return age in seconds
     */
    unsigned int age() const;

private:
    rsc::logging::LoggerPtr logger;
    unsigned int receivedParts;
    std::vector<rsb::protocol::FragmentedNotificationPtr> store;
    boost::posix_time::ptime birthTime;

};

typedef boost::shared_ptr<Assembly> AssemblyPtr;

/**
 * Instances of this class maintain a pool of ongoing @ref Assembly
 * s. In addition to adding arriving notification fragments to these,
 * the ages of assemblies are monitor and old assemblies are pruned.
 *
 * @author jmoringe
 */
class RSBSPREAD_EXPORT AssemblyPool {
public:

    /**
     * Creates a new pool with specified settings. Pruning will not immediately
     * start with these settings. It has to be enabled explicitly using the
     * appropriate method calls.
     *
     * @param ageS defines the max. allowed age of pooled fragments before they
     *              are pruned (s) > 0
     * @param pruningIntervalMs the interval to use for checking the age (ms) > 0
     * @throw std::domain_error 0 given for ageMs or pruningIntervalMs
     */
    explicit AssemblyPool(const unsigned int& ageS = 20,
            const unsigned int& pruningIntervalMs = 4000);

    ~AssemblyPool();

    /**
     * Tells whether the pool is currently pruning fragments or not. This method
     * is thread-safe.
     *
     * @return @c true if the pool is currently pruning, else @c false
     */
    bool isPruning() const;

    /**
     * Changes the pruning settings (enables or disables pruning) and waits
     * until the new settings are applied. This method is thread-safe.
     *
     * @param prune if @c true, start pruning if it is not yet running, if
     *        @c false, disable pruning if active
     */
    void setPruning(const bool& prune);

    /**
     * Adds a new notification to the pool and tries to join it with already
     * pooled parts. If a complete event notification is available after this
     * message, the joined Notification is returned and the all parts are
     * removed from the pool.
     *
     * @param notification notification to add to the pool
     * @return if a joined message is ready, the notification is returned, else
     *         a 0 pointer
     * @throw protocol::ProtocolException if a fragment was received multiple
     *                                    times
     */
    rsb::protocol::NotificationPtr add(
            rsb::protocol::FragmentedNotificationPtr notification);

private:
    typedef std::map<std::string, boost::shared_ptr<Assembly> > Pool;

    class PruningTask: public rsc::threading::PeriodicTask {
    public:

        PruningTask(Pool& pool, boost::recursive_mutex& poolMutex,
                const unsigned int& ageS,
                const unsigned int& pruningIntervalMs);

        void execute();

    private:
        rsc::logging::LoggerPtr logger;
        Pool& pool;
        boost::recursive_mutex& poolMutex;
        unsigned int maxAge;
    };

    rsc::logging::LoggerPtr logger;
    Pool pool;
    boost::recursive_mutex poolMutex;

    const unsigned int pruningAgeS;
    const unsigned int pruningIntervalMs;

    rsc::threading::ThreadedTaskExecutor executor;
    mutable boost::recursive_mutex pruningMutex;
    rsc::threading::TaskPtr pruningTask;
};

typedef boost::shared_ptr<AssemblyPool> AssemblyPoolPtr;

}
}
}

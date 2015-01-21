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

#include <boost/thread/recursive_mutex.hpp>
#include <boost/thread/condition.hpp>

#include "Task.h"
#include "rsc/rscexports.h"

namespace rsc {
namespace threading {

/**
 * A Task subclass which maintains interruption through a volatile boolean flag
 * which should be processed by the user code.
 *
 * Implementations of #run must call #markDone whenever they stop working. This
 * is either because their job finished or they were canceled.
 *
 * @author jwienke
 */
class RSC_EXPORT SimpleTask: public Task {
public:

    SimpleTask();
    virtual ~SimpleTask();

    virtual void cancel();

    virtual bool isCancelRequested();

    virtual void waitDone();

    virtual bool isDone();

protected:
    void markDone();

private:

    mutable boost::recursive_mutex mutex;
    boost::condition condition;

    volatile bool canceled;
    volatile bool done;

};

}
}

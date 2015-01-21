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

#include <queue>

#include <boost/format.hpp>
#include <boost/function.hpp>
#include <boost/noncopyable.hpp>
#include <boost/thread/condition.hpp>
#include <boost/thread/recursive_mutex.hpp>

#include "InterruptedException.h"
#include "rsc/rscexports.h"

namespace rsc {
namespace threading {

/**
 * Indicates that a queue was empty while trying to pop an element from it.
 *
 * @author jwienke
 */
class RSC_EXPORT QueueEmptyException: public std::runtime_error {
public:
    QueueEmptyException();
    explicit QueueEmptyException(const std::string& message);
};

/**
 * A queue with synchronized access and interruption support. On #push
 * operations only one waiting thread is woken up.
 *
 * @author jwienke
 * @tparam M message type handled in this queue
 */
template<class M>
class SynchronizedQueue: public boost::noncopyable {
public:

    typedef boost::function<void(const M& drop)> dropHandlerType;

private:

    volatile bool interrupted;
    std::queue<M> queue;
    mutable boost::recursive_mutex mutex;
    boost::condition condition;
    unsigned int sizeLimit;
    dropHandlerType dropHandler;

public:

    /**
     * Creates a new queue.
     *
     * @param sizeLimit max allowed size of the queue. If the limit is reached
     *                  and another element is added to the queue, the oldest
     *                  element is removed. 0 means unlimited
     * @param
     */
    explicit SynchronizedQueue(const unsigned int& sizeLimit = 0,
                               dropHandlerType dropHandler = 0) :
        interrupted(false), sizeLimit(sizeLimit), dropHandler(dropHandler) {
    }

    virtual ~SynchronizedQueue() {
        // TODO what to do on destruction if still someone in waiting?
        // Simply calling interrupt does not help because threads still access
        // the queue variables at waking up from the interruption while this
        // class is being destructed and the condition variable gets destructed
    }

    /**
     * Pushes a new element on the queue and wakes up one waiting client if
     * there is one.
     *
     * @param message element to push on the queue
     */
    void push(const M& message) {
        {
            boost::recursive_mutex::scoped_lock lock(this->mutex);
            if (this->sizeLimit > 0) {
                while (this->queue.size() > this->sizeLimit - 1) {
                    if (this->dropHandler) {
                        this->dropHandler(this->queue.front());
                    }
                    this->queue.pop();
                }
            }
            this->queue.push(message);
        }
        this->condition.notify_one();
    }

    /**
     * Returns the element at the front of the queue without removing
     * it, waiting until there is such an element if necessary.
     *
     * @param timeoutMs maximum time spent waiting for a new element
     *                  in milliseconds. Zero indicates to wait
     *                  endlessly.
     * @return front element of the queue
     * @throw InterruptedException if #interrupt was called before a call to
     *                             this method or while waiting for an element
     * @throw QueueEmptyException thrown if @param timeoutMs was > 0
     *                            and no element was found on the
     *                            queue in the specified time
     */
    M peek(const boost::uint32_t& timeoutMs = 0) {

        boost::recursive_mutex::scoped_lock lock(this->mutex);

        while (!this->interrupted && this->queue.empty()) {
            if (timeoutMs == 0) {
                this->condition.wait(lock);
            } else {
#if BOOST_VERSION >= 105000
                if (!this->condition.timed_wait(lock, boost::posix_time::milliseconds(timeoutMs))) {
#else
                const boost::system_time timeout = boost::get_system_time()
                    + boost::posix_time::milliseconds(timeoutMs);
                if (!this->condition.timed_wait(lock, timeout)) {
#endif
                    throw QueueEmptyException(boost::str(boost::format("No element available on queue within %d ms.") % timeoutMs));
                }
            }
        }

        if (this->interrupted) {
            throw InterruptedException("Queue was interrupted");
        }

        return this->queue.front();
    }

    /**
     * Returns the next element form the queue and wait until there is such an
     * element. The returned element is removed from the queue immediately.
     *
     * @param timeoutMs maximum time spent waiting for a new element in
     *                  milliseconds. Zero indicates to wait endlessly.
     *                  Use #tryPop if you want to get a result without waiting
     * @return next element on the queue
     * @throw InterruptedException if #interrupt was called before a call to
     *                             this method or while waiting for an element
     * @throw QueueEmptyException thrown if @param timeoutMs was > 0 and no
     *                            element was found on the queue in the
     *                            specified time
     */
    M pop(const boost::uint32_t& timeoutMs = 0) {

        boost::recursive_mutex::scoped_lock lock(this->mutex);

        while (!this->interrupted && this->queue.empty()) {
            if (timeoutMs == 0) {
                this->condition.wait(lock);
            } else {
#if BOOST_VERSION >= 105000
                if (!this->condition.timed_wait(lock, boost::posix_time::milliseconds(timeoutMs))) {
#else
                const boost::system_time timeout = boost::get_system_time()
                        + boost::posix_time::milliseconds(timeoutMs);
                if (!this->condition.timed_wait(lock, timeout)) {
#endif
                    throw QueueEmptyException(boost::str(boost::format("No element available on queue within %d ms.") % timeoutMs));
                }
            }
        }

        if (this->interrupted) {
            throw InterruptedException("Queue was interrupted");
        }

        M message = this->queue.front();
        this->queue.pop();
        return message;

    }

    /**
     * Tries to pop an element from the queue but does not wait if there is no
     * element on the queue.
     *
     * @return element from the queue
     * @throw QueueEmptyException the queue was empty
     */
    M tryPop() {

        boost::recursive_mutex::scoped_lock lock(this->mutex);

        if (this->queue.empty()) {
            throw QueueEmptyException();
        }

        M message = this->queue.front();
        this->queue.pop();
        return message;

    }

    /**
     * Checks whether this queue is empty. Is not affected by the interruption
     * flag.
     *
     * @return @c true if empty, else @c false
     */
    bool empty() const {
        boost::recursive_mutex::scoped_lock lock(this->mutex);
        return this->queue.empty();
    }

    /**
     * Return element count of the queue.
     *
     * @return The number of elements currently stored in the queue.
     */
    std::size_t size() const {
        boost::recursive_mutex::scoped_lock lock(this->mutex);
        return this->queue.size();
    }

    /**
     * Remove all elements from the queue.
     */
    void clear() {
        boost::recursive_mutex::scoped_lock lock(this->mutex);
        while (!this->queue.empty()) {
            this->queue.pop();
        }
    }

    /**
     * Interrupts the processing on this queue for every current call
     * to pop and any later call. All of them will return with an
     * exception.
     */
    void interrupt() {
        {
            boost::recursive_mutex::scoped_lock lock(this->mutex);
            this->interrupted = true;
        }
        this->condition.notify_all();
    }

};

}
}

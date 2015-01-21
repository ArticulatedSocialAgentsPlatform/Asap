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

#include <boost/bind.hpp>
#include <boost/thread/condition.hpp>
#include <boost/function.hpp>
#include <boost/thread/mutex.hpp>
#include <boost/shared_ptr.hpp>
#include <boost/thread.hpp>

#include "../misc/IllegalStateException.h"
#include "SynchronizedQueue.h"

namespace rsc {
namespace threading {

/**
 * A thread pool that dispatches messages to a list of receivers. The number of
 * threads is usually smaller than the number of receivers and for each
 * receiver it is guaranteed that messages arrive in the order they were
 * published. No guarantees are given between different receivers.
 * All methods except #start and #stop are reentrant.
 *
 * The pool can be stopped and restarted at any time during the processing but
 * these calls must be single-threaded.
 *
 * Assumptions:
 *  * same subscriptions for multiple receivers unlikely, hence filtering done
 *    per receiver thread
 *
 * Filtering and delivery of message to receivers are performed by handlers.
 * These handlers should be stateless.
 *
 * @author jwienke
 *
 * @tparam M type of the messages dispatched by the pool
 * @tparam R type of the message receiver
 */
template<class M, class R>
class OrderedQueueDispatcherPool {
public:

    /**
     * A function that delivers a message to a receiver. Must be reentrant.
     */
    typedef boost::function<void(boost::shared_ptr<R>& receiver,
            const M& message)> deliverFunction;

    /**
     * A function that filters a message for a receiver. If @c true is returned,
     * the message is acceptable for the receiver, else it will not be
     * delivered. Must be reentrant.
     */
    typedef boost::function<bool(boost::shared_ptr<R>& receiver,
            const M& message)> filterFunction;

    /**
     * A handler that is called whenever a message is received from the pool
     * and should be passed to a receiver of the pool.
     *
     * @author jwienke
     * @note do not use state in this class and make it reentrant
     */
    class DeliveryHandler {
    public:

        virtual ~DeliveryHandler() {
        }

        /**
         * Requests this handler to deliver the message to the receiver.
         *
         * @param receiver receiver to pass message to
         * @param message message to pass to the receiver
         */
        virtual void
        deliver(boost::shared_ptr<R>& receiver, const M& message) = 0;

    };

    typedef boost::shared_ptr<DeliveryHandler> DeliveryHandlerPtr;

    /**
     * A handler that is used to filter messages for a certain receiver.
     *
     * @author jwienke
     * @note do not use state in this class and make it reentrant
     */
    class FilterHandler {
    public:

        virtual ~FilterHandler() {
        }

        /**
         * A function that filters a message for a receiver.
         *
         * @param receiver receiver to filter for
         * @param message message to filter
         * @return If @c true is returned, the message is acceptable for the
         *         receiver, else it will not be delivered.
         */
        virtual bool
        filter(boost::shared_ptr<R>& receiver, const M& message) = 0;

    };

    typedef boost::shared_ptr<FilterHandler> FilterHandlerPtr;

private:

    /**
     * A filter that accepts every message.
     *
     * @author jwienke
     */
    class TrueFilter: public FilterHandler {
    public:
        bool filter(boost::shared_ptr<R>& /*receiver*/, const M& /*message*/) {
            return true;
        }
    };

    /**
     * An adapter for function-based filter to the object-oriented interface.
     *
     * @author jwienke
     */
    class FilterFunctionAdapter: public FilterHandler {
    public:

        explicit FilterFunctionAdapter(filterFunction function) :
            function(function) {
        }

        bool filter(boost::shared_ptr<R>& receiver, const M& message) {
            return function(receiver, message);
        }

    private:
        filterFunction function;
    };

    /**
     * An adapter for function-based delivery handlers to the object-oriented
     * interface.
     *
     * @author jwienke
     */
    class DeliverFunctionAdapter: public DeliveryHandler {
    public:

        explicit DeliverFunctionAdapter(deliverFunction function) :
            function(function) {
        }

        void deliver(boost::shared_ptr<R>& receiver, const M& message) {
            function(receiver, message);
        }

    private:
        deliverFunction function;
    };

    /**
     * Represents on registered receiver of the pool.
     *
     * @author jwienke
     */
    class Receiver {
    public:

        Receiver(boost::shared_ptr<R> receiver) :
            receiver(receiver), processing(false) {
        }

        boost::shared_ptr<R> receiver;
        // TODO think about if this really requires a synchronized queue if
        // all message dispatching to worker threads is synchronized
        SynchronizedQueue<M> queue;

        boost::condition processingCondition;

        /**
         * Indicates whether a job for this worker is currently being processed
         * and this receiver hence cannot be addressed by another thread even
         * though there may be more messages to process.
         *
         * All changes to this flag are already locked by the global
         * recieversMutex.
         */
        volatile bool processing;

    };

    // TODO make this a set to only allow unique subscriptions?
    boost::mutex receiversMutex;
    std::vector<boost::shared_ptr<Receiver> > receivers;
    size_t currentPosition;

    volatile bool jobsAvailable;
    boost::condition jobsAvailableCondition;
    volatile bool interrupted;

    volatile bool started;

    /**
     * Returns the next job to process for worker threads and blocks if there
     * is no job.
     *
     * @param workerNum number of the worker requesting a new job
     * @param receiver out param with the receiver to work on
     */
    void nextJob(const unsigned int& /*workerNum*/,
            boost::shared_ptr<Receiver>& receiver) {

        boost::mutex::scoped_lock lock(receiversMutex);

        //        std::cout << "Worker " << workerNum << " requests a new job"
        //                << std::endl;

        // wait until a job is available
        bool gotJob = false;
        while (!gotJob) {

            while (!jobsAvailable && !interrupted) {
                //                std::cout << "Worker " << workerNum
                //                        << ": no jobs available, waiting" << std::endl;
                jobsAvailableCondition.wait(lock);
            }

            if (interrupted) {
                throw InterruptedException("Processing was interrupted");
            }

            // search for the next job
            for (size_t pos = 0; pos < receivers.size(); ++pos) {

                // TODO is this selection fair in any case?
                ++currentPosition;
                size_t realPos = currentPosition % receivers.size();

                // TODO maybe provide an atomic pop and tell if successful
                // operation in SynchronizedQueue
                if (!receivers[realPos]->processing
                        && !receivers[realPos]->queue.empty()) {

                    // found a job

                    receiver = receivers[realPos];
                    receiver->processing = true;
                    gotJob = true;
                    break;

                }

            }

            // did not find a job, hence there are no other jobs right now
            if (!gotJob) {
                jobsAvailable = false;
            }

        }

        // if I got a job there are certainly others interested in jobs
        //        std::cout << "Worker " << workerNum << ": got job for receiver"
        //                << receiver->receiver << ", notify_one" << std::endl;
        lock.unlock();
        jobsAvailableCondition.notify_one();

    }

    unsigned int threadPoolSize;

    void finishedWork(boost::shared_ptr<Receiver> receiver) {

        boost::mutex::scoped_lock lock(receiversMutex);
        // changing this flag must already be locked as it is read by the
        // globally synchronized nextJob method to determine if a job is
        // available
        receiver->processing = false;
        receiver->processingCondition.notify_all();
        // maybe avoid informing other threads about new jobs of the receiver
        // will be removed by adding a flag? Right now they will start to search
        // for a new job and may not find one.
        if (!receiver->queue.empty()) {
            jobsAvailable = true;
            lock.unlock();
            jobsAvailableCondition.notify_one();
        }

    }

    /**
     * Threaded worker method.
     */
    void worker(const unsigned int& workerNum) {

        try {
            while (true) {

                boost::shared_ptr<Receiver> receiver;
                nextJob(workerNum, receiver);
                M message = receiver->queue.pop();
                //                std::cout << "Worker " << workerNum << " got new job: "
                //                        << message << " for receiver " << *(receiver->receiver)
                //                        << std::endl;
                if (filterHandler->filter(receiver->receiver, message)) {
                    deliveryHandler->deliver(receiver->receiver, message);
                }
                finishedWork(receiver);

            }
        } catch (InterruptedException& e) {
            //            std::cout << "Worker " << workerNum << " was interrupted"
            //                    << std::endl;
        }

    }

    std::vector<boost::shared_ptr<boost::thread> > threadPool;

    DeliveryHandlerPtr deliveryHandler;
    FilterHandlerPtr filterHandler;

public:

    /**
     * Constructs a new pool.
     *
     * @param threadPoolSize number of threads for this pool
     * @param delFunc the strategy used to deliver messages of type M to
     *                receivers of type R. This will most likely be a simple
     *                delegate function mapping to a concrete method call. Must
     *                be reentrant.
     * @note the object-oriented interface should be preferred
     */
    OrderedQueueDispatcherPool(const unsigned int& threadPoolSize,
            deliverFunction delFunc) :
        currentPosition(0), jobsAvailable(false), interrupted(false), started(
                false), threadPoolSize(threadPoolSize), deliveryHandler(
                new DeliverFunctionAdapter(delFunc)), filterHandler(
                new TrueFilter()) {
    }

    /**
     * Constructs a new pool.
     *
     * @param threadPoolSize number of threads for this pool
     * @param delFunc the strategy used to deliver messages of type M to
     *                receivers of type R. This will most likely be a simple
     *                delegate function mapping to a concrete method call. Must
     *                be reentrant.
     * @param filterFunc Reentrant function used to filter messages per
     *                   receiver. Default accepts every message.
     * @note the object-oriented interface should be preferred
     */
    OrderedQueueDispatcherPool(const unsigned int& threadPoolSize,
            deliverFunction delFunc, filterFunction filterFunc) :
        currentPosition(0), jobsAvailable(false), interrupted(false), started(
                false), threadPoolSize(threadPoolSize), deliveryHandler(
                new DeliverFunctionAdapter(delFunc)), filterHandler(
                new FilterFunctionAdapter(filterFunc)) {
    }

    /**
     * Constructs a new pool using the object-oriented handler interface that
     * accepts every message.
     *
     * @param threadPoolSize number of threads for this pool
     * @param deliveryHandler handler to deliver messages to receivers
     */
    OrderedQueueDispatcherPool(const unsigned int& threadPoolSize,
            DeliveryHandlerPtr deliveryHandler) :
        currentPosition(0), jobsAvailable(false), interrupted(false), started(
                false), threadPoolSize(threadPoolSize), deliveryHandler(
                deliveryHandler), filterHandler(new TrueFilter) {
    }

    /**
     * Constructs a new pool using the object-oriented handler interface.
     *
     * @param threadPoolSize number of threads for this pool
     * @param deliveryHandler handler to deliver messages to receivers
     * @param filterHandler filter handler for messages
     */
    OrderedQueueDispatcherPool(const unsigned int& threadPoolSize,
            DeliveryHandlerPtr deliveryHandler, FilterHandlerPtr filterHandler) :
        currentPosition(0), jobsAvailable(false), interrupted(false), started(
                false), threadPoolSize(threadPoolSize), deliveryHandler(
                deliveryHandler), filterHandler(filterHandler) {
    }

    virtual ~OrderedQueueDispatcherPool() {
        stop();
    }

    /**
     * Registers a new receiver at the pool. Multiple registrations of the same
     * receiver are possible resulting in being called multiple times for the
     * same message (but effectively this destroys the guarantee about ordering
     * given above because multiple message queues are used for every
     * subscription).
     *
     * @param receiver new receiver
     */
    void registerReceiver(boost::shared_ptr<R> receiver) {
        boost::mutex::scoped_lock lock(receiversMutex);
        boost::shared_ptr<Receiver> rec(new Receiver(receiver));
        receivers.push_back(rec);
    }

    /**
     * Unregisters all registration of one receiver.
     *
     * @param receiver receiver to unregister
     * @return @c true if one or more receivers were unregistered, else @c false
     */
    bool unregisterReceiver(boost::shared_ptr<R> receiver) {

        boost::mutex::scoped_lock lock(receiversMutex);

        for (typename std::vector<boost::shared_ptr<Receiver> >::iterator it =
                receivers.begin(); it != receivers.end(); ++it) {
            boost::shared_ptr<Receiver> rec = *it;
            if (rec->receiver == receiver) {
                it = receivers.erase(it);
                while (rec->processing) {
                    rec ->processingCondition.wait(lock);
                }
                return true;
            }
        }
        return false;

    }

    /**
     * Non-blocking start.
     *
     * @throw IllegalStateException if the pool was already started and is
     *                              running
     */
    void start() {

        boost::mutex::scoped_lock lock(receiversMutex);
        if (started) {
            throw rsc::misc::IllegalStateException("Pool already running");
        }

        interrupted = false;

        for (unsigned int i = 0; i < threadPoolSize; ++i) {
            boost::function<void()> workerMethod = boost::bind(
                    &OrderedQueueDispatcherPool::worker, this, i);
            boost::shared_ptr<boost::thread> w(new boost::thread(workerMethod));
            threadPool.push_back(w);
        }

        started = true;

    }

    /**
     * Blocking until every thread has stopped working.
     */
    void stop() {

        {
            boost::mutex::scoped_lock lock(receiversMutex);
            interrupted = true;
        }
        jobsAvailableCondition.notify_all();

        for (unsigned int i = 0; i < threadPool.size(); ++i) {
            threadPool[i]->join();
        }
        threadPool.clear();

        started = false;

    }

    /**
     * Pushes a new message to be dispatched to all receivers in this pool.
     *
     * @param message message to dispatch
     */
    void push(const M& message) {

        //        std::cout << "new job " << message << std::endl;
        {
            boost::mutex::scoped_lock lock(receiversMutex);
            for (typename std::vector<boost::shared_ptr<Receiver> >::iterator
                    it = receivers.begin(); it != receivers.end(); ++it) {
                (*it)->queue.push(message);
            }
            jobsAvailable = true;
        }
        jobsAvailableCondition.notify_one();

    }

};

}
}


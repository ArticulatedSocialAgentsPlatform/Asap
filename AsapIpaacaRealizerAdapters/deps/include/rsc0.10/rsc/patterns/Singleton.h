/* ============================================================
 *
 * This file is part of the RSC project
 *
 * Copyright (C) 2010, 2011 Jan Moringen
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

#include <boost/noncopyable.hpp>
#include <boost/shared_ptr.hpp>
#include <boost/thread.hpp>
#include <boost/bind.hpp>

namespace rsc {
namespace patterns {

/**
 * This template class implements the singleton pattern.
 *
 * To add singleton behavior to a class @c T, add @c Singleton<T> to its list of
 * base classes.
 *
 * @note T has to contain a friend declaration for @c Singleton<T>.
 * @note This class is thread-safe and can be used in static initializations
 * @note For allowing the use inside static initialization code, the absurd
 *       amount of work for the mutex is necessary.
 *
 * @author Jan Moringen <jmoringe@techfak.uni-bielefeld.de>
 */
template<typename T>
class Singleton: private boost::noncopyable {
public:

    virtual ~Singleton();

    /**
     * Retrieve the singleton instance, creating it if necessary.
     *
     * @return A reference to the instance.
     */
    static T& getInstance();

    /**
     * This function can be used to make sure the instance is deleted at a
     * particular time.
     *
     * You may need this function to enforce a certain order of destruction.
     *
     * @note The instance will be destroyed in any case but the order of
     *       destruction is unspecified then.
     */
    static void killInstance();
private:
    static boost::shared_ptr<T>& getStorage();

    static boost::mutex& getInstanceMutex();

    static void createMutex(boost::mutex*& destination);

};

// Singleton implementation

template<typename T>
void Singleton<T>::createMutex(boost::mutex*& destination) {
    destination = new boost::mutex();
}

template<typename T>
boost::mutex& Singleton<T>::getInstanceMutex() {
    static boost::mutex* instanceMutex;
    static boost::once_flag instanceMutexOnceFlag = BOOST_ONCE_INIT;
    boost::call_once(instanceMutexOnceFlag,
            boost::bind(&Singleton::createMutex, boost::ref(instanceMutex)));
    return *instanceMutex;
}

template<typename T>
T& Singleton<T>::getInstance() {
    boost::mutex& instanceMutex = getInstanceMutex();
    boost::mutex::scoped_lock lock(instanceMutex);

    boost::shared_ptr<T>& instance = getStorage();

    if (!instance) {
        instance = boost::shared_ptr<T>(new T());
    }

    return *instance;
}

template<typename T>
Singleton<T>::~Singleton() {
}

template<typename T>
void Singleton<T>::killInstance() {
    boost::mutex& instanceMutex = getInstanceMutex();
    boost::mutex::scoped_lock lock(instanceMutex);

    boost::shared_ptr<T>& instance = getStorage();

    if (instance) {
        instance.reset();
    }
}

template<typename T>
boost::shared_ptr<T>& Singleton<T>::getStorage() {
    static boost::shared_ptr<T> instance = boost::shared_ptr<T>();

    return instance;
}

}
}

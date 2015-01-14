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

#include <iostream>
#include <map>
#include <set>
#include <stdexcept>
#include <string>

#include <boost/noncopyable.hpp>
#include <boost/shared_ptr.hpp>
#include <boost/static_assert.hpp>
#include <boost/thread/recursive_mutex.hpp>

#include "rsc/config.h"
#include "rsc/rscexports.h"

namespace rsc {
namespace misc {

/**
 * A templatized global registry.
 *
 * To create a registree, that will be registered on application startup before
 * main use the provided macros. This will work on every supported compiler.
 *
 * This class is reentrant.
 *
 * @author jwienke
 * @tparam R type to register called registree.
 */
template<class R>
class Registry: public boost::noncopyable {
public:

    Registry() {
    }

    /**
     * Registers a new registree in the Registry. This class takes over the
     * ownership of the pointer and manages deleting the pointer when needed.
     *
     * @param r new registree to register
     * @param errorDescription optional string to add to the message of the
     *                         exception which is raised when a registree with
     *                         a same key already exists.
     * @throw std::invalid_argument if a registree with the same key is already
     *                              registered. In this case @c r is not owned
     *                              by this registry
     */
    void addRegistree(R* r, const std::string& errorDescription = "") {

        boost::recursive_mutex::scoped_lock lock(mutex);

        if (registreesByName.count(r->getRegistryKey())) {
            throw std::invalid_argument(
                    "There already is a registree with key '"
                            + r->getRegistryKey() + "'. " + errorDescription);
        }

        registreesByName[r->getRegistryKey()] = boost::shared_ptr<R>(r);

    }

    /**
     * Returns the registree for the provided key.
     *
     * @param key key of the registree
     * @return registree
     * @throw std::invalid_argument if there is no registree with this name
     */
    boost::shared_ptr<R> getRegistree(const std::string& key) {

        boost::recursive_mutex::scoped_lock lock(mutex);

        typename std::map<std::string, boost::shared_ptr<R> >::const_iterator it =
                registreesByName.find(key);
        if (it == registreesByName.end()) {
            throw std::invalid_argument(
                    "There is no registree with key '" + key + "'.");
        }

        return it->second;

    }

    /**
     * Returns a set with all known registree keys.
     *
     * @return all known registree keys
     */
    std::set<std::string> getKnownRegistryKeys() {

        boost::recursive_mutex::scoped_lock lock(mutex);

        std::set<std::string> keys;
        typename std::map<std::string, boost::shared_ptr<R> >::const_iterator it;
        for (it = registreesByName.begin(); it != registreesByName.end();
                ++it) {
            keys.insert(it->first);
        }
        return keys;

    }

    /**
     * Removes a registree from this registry. Use this with caution as many
     * clients will assume that system configuration knowledge is static!
     *
     * @param name name of the registree to remove
     * @return @c true if a registree was removed, else @c false
     */
    bool removeRegistree(const std::string& name) {
        boost::recursive_mutex::scoped_lock lock(mutex);
        return registreesByName.erase(name) != 0;
    }

    /**
     * Returns a recursive mutex that can be used to make multiple operations
     * on this class atomic.
     *
     * @return mutex internally used by this class
     */
    boost::recursive_mutex* getMutex() {
        return &mutex;
    }

private:

    boost::recursive_mutex mutex;
    std::map<std::string, boost::shared_ptr<R> > registreesByName;

};

/**
 * Creates an object that globally registers in the Registry. This method only
 * works directly in binaries or shared libraries, no static libraries.
 *
 * Class names for this macro must be given without namespaces and templates.
 * Create typedefs as required to match these restrictions.
 *
 * @note be aware of the static initialization order fiasco and do not return
 *       static members for the registry key
 * @param registry registry to register in
 * @param registree registree to register
 * @param uniqueName a unique name to generate a register function
 */
#define CREATE_GLOBAL_REGISTREE(registry, registree, uniqueName) \
    class Starter##uniqueName { \
    public: \
        Starter##uniqueName() { \
        (registry)->addRegistree(registree); \
        } \
    }; \
    Starter##uniqueName uniqueName##Starter;

/**
 * Creates an object that globally registers in the Registry. This method only
 * works directly in binaries or shared libraries, no static libraries.
 *
 * Class names for this macro must be given without namespaces and templates.
 * Create typedefs as required to match these restrictions.
 *
 * @note be aware of the static initialization order fiasco and do not return
 *       static members for the registry key
 * @param registry registry to register in
 * @param registree registree to register
 * @param msg error message to display on registration failure
 * @param uniqueName a unique name to generate a register function
 */
#define CREATE_GLOBAL_REGISTREE_MSG(registry, registree, uniqueName, msg) \
    class Starter##uniqueName { \
    public: \
        Starter##uniqueName() { \
        (registry)->addRegistree(registree, msg); \
        } \
    }; \
    Starter##uniqueName uniqueName##Starter;

}
}


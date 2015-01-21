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

#include <stdexcept>
#include <utility>
#include <algorithm>
#include <map>

#include <boost/function.hpp>
#include <boost/format.hpp>

#include "../runtime/TypeStringTools.h"
#include "../runtime/Properties.h"
#include "Singleton.h"
#include "NoSuchImpl.h"
#include "ConstructError.h"
#include "AssociativeProxy.h"

namespace rsc {
namespace patterns {

/**
 * An interface-independent factory interface, mainly used as a base class for
 * more specific factories.
 *
 * A factory of this kind is basically a mapping of keys to creation
 * functions that create objects of some common base class.
 *
 * The factory has a list of implementations that can be retrieved
 * using the @a impls_base method.
 *
 * Runtime type information for the interface implemented by
 * constructed objects can be retrieved using the
 * @a GetInterfaceType method.
 *
 * Objects are constructed by calling @a create_base with a key
 * identifying the implementation and properties to be used as
 * arguments to the constructor.
 *
 * @author Jan Moringen <jmoringe@techfak.uni-bielefeld.de>
 */
template<typename Key>
class FactoryBase {
public:
    typedef Key KeyType;
    typedef boost::function1<void*, const runtime::Properties&> CreateFunction;
    typedef std::pair<const std::type_info*, void*> type_and_storage;

    typedef std::map<Key, CreateFunction> ImplMap;

    class ImplMapProxy: public AssociativeProxy<ImplMap> {
    protected:
        template<typename K, typename I>
        friend class Factory;

        typedef AssociativeProxy<ImplMap> base;

        ImplMapProxy(ImplMap& container) :
            base(container) {
        }
    };

    /**
     * Return the type information of the interface type of the factory.
     */
    virtual const std::type_info&
    GetInterfaceType() const throw () = 0;

    /**
     * Return a container-like object holding all registered implementations.
     *
     * @return A constant reference to the implementation list proxy.
     */
    virtual const ImplMapProxy&
    implsBase() const throw () = 0;

    /**
     * Create and return an instance of the implementation designated by @a key.
     * @a properties_ is passed to the create function.
     *
     * @param key The name of a registered implementation.
     * @param properties_ A set of properties. The interpretation is up the
     *                    selected create function.
     * @return A pair containing the type information of the created object and
     *         a void pointer pointing to it.
     * @throw NoSuchImpl If @a key does not name a registered implementation.
     * @throw ConstructError If the selected create function produced an
     *                       exception during execution.
     */
    virtual type_and_storage
    createBase(const Key& key, const runtime::Properties& properties_ =
            runtime::Properties()) = 0;
};

/**
 * Objects of this class manage a family of named implementations of a
 * particular interface.
 *
 * @see FactoryBase
 *
 * @author Jan Moringen <jmoringe@techfak.uni-bielefeld.de>
 */
template<typename Key, typename Interface>
class Factory: public FactoryBase<Key> {
public:
    typedef FactoryBase<Key> base;

    typedef typename base::KeyType KeyType;
    typedef Interface InterfaceType;

    typedef boost::function1<InterfaceType*, const runtime::Properties&>
            CreateFunction;

    typedef std::map<Key, CreateFunction> ImplMap;

    typedef typename base::ImplMap ImplMapBase;
    typedef typename base::ImplMapProxy ImplMapBaseProxy;

public:

    /**
     * This object presents the registered implementations in a form very
     * similar to a STL container.
     *
     * In addition, implementations can be registered or unregistered using
     * additional functions.
     */
    class ImplMapProxy: public AssociativeProxy<ImplMap> {
        friend class Factory<Key, Interface> ;
    public:
        /**
         * @throw std::invalid_argument
         */
        void register_(const KeyType& key,
                const CreateFunction& create_function_);

        /**
         * @throw NoSuchImpl
         */
        void unregister(const KeyType& key);
    private:
        typedef AssociativeProxy<ImplMap> base_type;

        Factory<Key, Interface>& owner;

        ImplMapProxy(Factory<Key, Interface>& owner);
    };

    friend class ImplMapProxy;

    Factory();

    virtual
    ~Factory();

    const std::type_info&
    GetInterfaceType() const throw ();

    const ImplMapBaseProxy&
    implsBase() const throw ();

    /**
     * Return a container-like object holding all registered implementations.
     */
    ImplMapProxy&
    impls() throw ();

    /**
     * Return a container-like object holding all registered implementations.
     */
    const ImplMapProxy&
    impls() const throw ();

    /**
     * @throw NoSuchImpl
     * @throw ConstructError
     */
    typename FactoryBase<Key>::type_and_storage // TODO we should inherit that
    createBase(const Key& key, const runtime::Properties& properties_ =
            runtime::Properties());

    /**
     * Create and return an instance of the implementation designated by @a key.
     * @a properties_ is passed to the create function.
     *
     * @param key The name of a registered implementation.
     * @param properties_ A set of properties. The interpretation is up the
     *        selected create function.
     * @return A pointer to a newly created instance of the implementation
     *         specified by @a key.
     * @throw NoSuchImpl If @a key does not name a registered implementation.
     * @throw ConstructError If the selected create function produced an
     *                       exception during execution.
     */
    Interface*
    createInst(const Key& key, const runtime::Properties& properties_ =
            runtime::Properties());
protected:
    ImplMapBase impl_map_base_;
    ImplMapBaseProxy impl_map_base_proxy_;

    ImplMap impl_map_;
    ImplMapProxy impl_map_proxy_;

    /**
     * @throw std::invalid_
     */
    virtual void register_(const Key& key,
            const CreateFunction& create_function_);

    /**
     * @throw NoSuchImpl
     */
    virtual void unregister(const Key& key);
};

/**
 * A factory of which at most one instance exists at any time.
 */
template<typename Key, typename Interface>
class SingletonFactory: public Singleton<SingletonFactory<Key, Interface> > ,
        public Factory<Key, Interface> {
    friend class Singleton<SingletonFactory<Key, Interface> > ;
protected:
    SingletonFactory();
};

// Factory::impl_list_proxy implementation

template<typename Key, typename Interface>
Factory<Key, Interface>::ImplMapProxy::ImplMapProxy(
        Factory<Key, Interface>& owner) :
    base_type(owner.impl_map_), owner(owner) {
}

template<typename Key, typename Interface>
void Factory<Key, Interface>::ImplMapProxy::register_(const KeyType& key,
        const CreateFunction& create_function_) {
    this->owner.register_(key, create_function_);
}

template<typename Key, typename Interface>
void Factory<Key, Interface>::ImplMapProxy::unregister(const KeyType& key) {
    this->owner.unregister(key);
}

// Factory implementation

template<typename Key, typename Interface>
Factory<Key, Interface>::Factory() :
    impl_map_base_proxy_(this->impl_map_base_), impl_map_proxy_(*this) {
}

template<typename Key, typename Interface>
Factory<Key, Interface>::~Factory() {
}

template<typename Key, typename Interface>
const std::type_info&
Factory<Key, Interface>::GetInterfaceType() const throw () {
    return typeid(Interface);
}

template<typename Key, typename Interface>
const typename Factory<Key, Interface>::ImplMapBaseProxy&
Factory<Key, Interface>::implsBase() const throw () {
    return this->impl_map_base_proxy_;
}

template<typename Key, typename Interface>
typename Factory<Key, Interface>::ImplMapProxy&
Factory<Key, Interface>::impls() throw () {
    return this->impl_map_proxy_;
}

template<typename Key, typename Interface>
const typename Factory<Key, Interface>::ImplMapProxy&
Factory<Key, Interface>::impls() const throw () {
    return this->impl_map_proxy_;
}

template<typename Key, typename Interface>
void Factory<Key, Interface>::register_(const Key& key,
        const CreateFunction& create_function_) {
    //
    if (this->impl_map_.find(key) != this->impl_map_.end()) {
        throw std::invalid_argument(runtime::typeString("duplicate key `%1%'",
                "duplicate key", key));
    }

    //
    this->impl_map_base_[key] = create_function_;
    this->impl_map_[key] = create_function_;
}

template<typename Key, typename Interface>
void Factory<Key, Interface>::unregister(const Key& key) {
    //
    typename ImplMap::iterator it;
    if ((it = this->impl_map_.find(key)) == this->impl_map_.end()) {
        throw NoSuchImpl(
                boost::str(
                        boost::format(
                                runtime::typeString(
                                        "no implementation of interface `%%1%%' found for specified key `%1%'",
                                        "no implementation of interface `%%1%%' found for specified key",
                                        key)) % runtime::typeName<Interface>()));
    }

    //
    this->impl_map_base_.erase(key);
    this->impl_map_.erase(it);
}

template<typename Key, typename Interface>
typename FactoryBase<Key>::type_and_storage Factory<Key, Interface>::createBase(
        const Key& key, const runtime::Properties& properties_) {
    Interface* instance = createInst(key, properties_);

    return std::make_pair(&typeid(*instance), instance);
}

template<typename Key, typename Interface>
Interface*
Factory<Key, Interface>::createInst(const Key& key,
        const runtime::Properties& properties_) {
    // Try to find the implementation specified by key.
    typename ImplMap::const_iterator it;
    if ((it = this->impl_map_.find(key)) == this->impl_map_.end()) {
        throw NoSuchImpl(
                boost::str(
                        boost::format(
                                runtime::typeString(
                                        "no implementation of interface `%%1%%' found for specified key `%1%'",
                                        "no implementation of interface `%%1%%' found for specified key",
                                        key)) % runtime::typeName<Interface>()));
    }

    // Try to create an instance of that implementation.
    Interface* instance = 0;
    try {
        instance = reinterpret_cast<Interface*> (it->second(properties_));
    } catch (const std::exception& exception_) {
        throw ConstructError(runtime::typeName(typeid(exception_)) + ": "
                + exception_.what());
        // TODO use boost exception stuff and rethrow
    } catch (...) {
        throw ConstructError(runtime::typeString(
                "could not construct implementation instance for key `%1%'",
                "could not construct implementation instance", key));
    }

    // Return the constructed instance.
    return instance;
}

// SingletonFactory implementation

template<typename Key, typename Interface>
SingletonFactory<Key, Interface>::SingletonFactory() {
}

// free function implementations

template<typename Ch, typename Tr, typename Key, typename Interface>
std::basic_ostream<Ch, Tr>&
operator<<(std::basic_ostream<Ch, Tr>& stream,
        const Factory<Key, Interface>& factory) {
    typedef Factory<Key, Interface> Factory_type;
    typedef typename Factory<Key, Interface>::ImplMapProxy impl_map_proxy_type;

    //
    stream << (boost::format("implementations of interface %1%:\n")
            % runtime::typeName(typeid(Interface)));

    //
    const impl_map_proxy_type& impls = factory.impls();

    for (typename impl_map_proxy_type::const_iterator it = impls.begin(); it
            != impls.end(); ++it) {
        stream << (boost::format("* %1%\n") % it->first);
    }

    // Don't forget to return the stream.
    return stream;
}

}
}

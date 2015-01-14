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

#include <boost/signals2.hpp>

#include "Factory.h"

namespace rsc {
namespace patterns {

/**
 * A specialized factory class objects of which emit signals when
 * implementations are registered or unregistered.
 *
 * @author Jan Moringen <jmoringe@techfak.uni-bielefeld.de>
 */
template<typename Key, typename Interface>
class ObservableFactory: public Factory<Key, Interface> {
protected:
    typedef Factory<Key, Interface> base;
public:
    typedef typename base::CreateFunction CreateFunction;

    typedef boost::signals2::signal2<void, const std::string&, const CreateFunction&>
            ImplAddedSignal;

    typedef boost::signals2::signal2<void, const std::string&, const CreateFunction&>
            ImplRemovedSignal;

    /**
     * Return the "implementation added" signal.
     */
    ImplAddedSignal&
    signalImplAdded() throw ();

    /**
     * Return the "implementation removed" signal.
     */
    ImplRemovedSignal&
    signalImplRemoved() throw ();
protected:
    typedef typename base::ImplMap ImplMap;

    ImplAddedSignal signal_impl_added_;
    ImplRemovedSignal signal_impl_removed_;

    /**
     * @throw std::invalid_argument
     */
    void register_(const Key& key, const CreateFunction& create_function_);

    /**
     * @throw NoSuchImpl
     */
    void unregister(const Key& key);
};

/**
 * An observable factory of which at most one instance exists at any time.
 */
template<typename Key, typename Interface>
class ObservableSingletonFactory: public Singleton<ObservableSingletonFactory<
        Key, Interface> > , public ObservableFactory<Key, Interface> {
    friend class Singleton<SingletonFactory<Key, Interface> > ;
private:
    ObservableSingletonFactory();
};

// ObservableFactory implementation

template<typename Key, typename Interface>
typename ObservableFactory<Key, Interface>::ImplAddedSignal&
ObservableFactory<Key, Interface>::signalImplAdded() throw () {
    return this->signal_impl_added_;
}

template<typename Key, typename Interface>
typename ObservableFactory<Key, Interface>::ImplRemovedSignal&
ObservableFactory<Key, Interface>::signalImplRemoved() throw () {
    return this->signal_impl_removed_;
}

template<typename Key, typename Interface>
void ObservableFactory<Key, Interface>::register_(const Key& key,
        const CreateFunction& create_function_) {
    base::register_(key, create_function_);

    //
    if (!this->signal_impl_added_.empty()) {
        this->signal_impl_added_(key, create_function_);
    }
}

template<typename Key, typename Interface>
void ObservableFactory<Key, Interface>::unregister(const Key& key) {
    //
    if (!this->signal_impl_removed_.empty()) {
        typename ImplMap::iterator it;
        if ((it = this->impl_map_.find(key)) != this->impl_map_.end()) {
            this->signal_impl_removed_(it->first, it->second);
	}
    }

    base::unregister(key);
}

// ObservableSingletonFactory implementation

template<typename Key, typename Interface>
ObservableSingletonFactory<Key, Interface>::ObservableSingletonFactory() {
}

}
}

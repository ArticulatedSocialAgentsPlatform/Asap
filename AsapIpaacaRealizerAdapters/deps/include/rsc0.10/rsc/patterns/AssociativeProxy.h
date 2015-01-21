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

#include <boost/type_traits/remove_reference.hpp>
#include <boost/noncopyable.hpp>

#include "../runtime/TypeStringTools.h"
#include "NoSuchKey.h"
#include "ContainerProxy.h"
#include "Accessors.h"
#include "detail/ForceConst.h"
#include "detail/PairWorkaround.h"

namespace rsc {
namespace patterns {

namespace detail {

template<typename Key, typename Mapped, typename Accessor>
struct pair_adapter {
    typedef detail::pair<Key, typename Accessor::result_type> result_type;

    result_type operator()(const std::pair<Key, Mapped>& pair) const throw () {
        return make_pair<Key, typename Accessor::result_type> (pair.first,
                accessor(pair.second));
    }

    Accessor accessor;
};

}

/**
 * @author Jan Moringen <jmoringe@techfak.uni-bielefeld.de>
 */
template<typename Container, typename Accessor = pass_through>
class AssociativeProxy: public ContainerProxy<Container,
        detail::pair_adapter<typename Container::key_type,
                typename Container::mapped_type, Accessor> > {
public:
    typedef typename Container::key_type key_type;
    typedef typename boost::remove_reference<typename Accessor::result_type>::type
            mapped_type;

    typedef detail::pair_adapter<typename Container::key_type,
            typename Container::mapped_type, Accessor> base_accessor_type;
    typedef ContainerProxy<Container, base_accessor_type> base_type;

    AssociativeProxy(Container& container);

    /**
     * @throw NoSuchKey
     */
    typename detail::force_const<typename Accessor::result_type>::type
    operator[](const key_type& key) const;

    /**
     * @throw NoSuchKey
     */
    typename Accessor::result_type
    operator[](const key_type& key);

    typename base_type::const_iterator
    find(const key_type& key) const throw () {
        return typename base_type::const_iterator(base_type::container.find(key),
                base_type::accessor);
    }

    typename base_type::iterator
    find(const key_type& key) throw () {
        return typename base_type::iterator(base_type::container.find(key),
                base_type::accessor);
    }

private:
    typedef Accessor accessor_type;

    accessor_type accessor;
};

template<typename Container>
class AssociativeProxy<Container, pass_through> : public ContainerProxy<
        Container, pass_through> {
public:
    typedef typename Container::key_type key_type;
    typedef typename Container::mapped_type mapped_type;

    typedef pass_through base_accessor_type;
    typedef ContainerProxy<Container, pass_through> base_type;

    AssociativeProxy(Container& container);

    /**
     * @throw NoSuchKey
     */
    typename detail::force_const<mapped_type>::type&
    operator[](const key_type& key) const;

    /**
     * @throw NoSuchKey
     */
    mapped_type&
    operator[](const key_type& key);

    typename ContainerProxy<Container, pass_through>::const_iterator
    find(const key_type& key) const throw ();

    typename ContainerProxy<Container, pass_through>::iterator
    find(const key_type& key) throw ();
private:
    typedef ContainerProxy<Container, pass_through> base;
};

// AssociativeProxy implementation

template<typename Container, typename Accessor>
AssociativeProxy<Container, Accessor>::AssociativeProxy(Container& container) :
    base_type(container) {
}

template<typename Container, typename Accessor>
typename detail::force_const<typename Accessor::result_type>::type AssociativeProxy<
        Container, Accessor>::operator[](const key_type& key) const {
    typename Container::const_iterator it;
    if ((it = base_type::container.find(key)) == base_type::container.end()) {
        throw no_such_key(type_string("no such key in container: `%1%'",
                "no such key in container", key));
    }

    return this->accessor(it->second);
}

template<typename Container, typename Accessor>
typename Accessor::result_type AssociativeProxy<Container, Accessor>::operator[](
        const key_type& key) {
    typename Container::iterator it;
    if ((it = base_type::container.find(key)) == base_type::container.end()) {
        throw no_such_key(type_string("no such key in container: `%1%'",
                "no such key in container", key));
    }

    return this->accessor(it->second);
}

// AssociativeProxy<Container, pass_through> implementation

template<typename Container>
AssociativeProxy<Container, pass_through>::AssociativeProxy(
        Container& container) :
    base(container) {
}

template<typename Container>
typename detail::force_const<
        typename AssociativeProxy<Container, pass_through>::mapped_type>::type&
AssociativeProxy<Container, pass_through>::operator[](const key_type& key) const {
    typename Container::const_iterator it;
    if ((it = base_type::container.find(key)) == base_type::container.end()) {
        throw no_such_key(type_string("no such key in container: `%1%'",
                "no such key in container", key));
    }

    return it->second;
}

template<typename Container>
typename AssociativeProxy<Container, pass_through>::mapped_type&
AssociativeProxy<Container, pass_through>::operator[](const key_type& key) {
    typename Container::iterator it;
    if ((it = base_type::container.find(key)) == base_type::container.end()) {
        throw no_such_key(type_string("no such key in container: `%1%'",
                "no such key in container", key));
    }

    return it->second;
}

template<typename Container>
typename ContainerProxy<Container, pass_through>::const_iterator AssociativeProxy<
        Container, pass_through>::find(const key_type& key) const throw () {
    return base_type::container.find(key);
}

template<typename Container>
typename ContainerProxy<Container, pass_through>::iterator AssociativeProxy<
        Container, pass_through>::find(const key_type& key) throw () {
    return base::container.find(key);
}

}
}

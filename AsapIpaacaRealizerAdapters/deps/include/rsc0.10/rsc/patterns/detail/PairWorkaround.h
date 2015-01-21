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

#include <boost/type_traits.hpp>

namespace rsc {
namespace patterns {
namespace detail {

/// pair holds two objects of arbitrary type.
template<class _T1, class _T2>
struct pair {
    typedef _T1 first_type; ///<  @c first_type is the first bound type
    typedef _T2 second_type; ///<  @c second_type is the second bound type

    _T1 first; ///< @c first is a copy of the first object
    _T2 second; ///< @c second is a copy of the second object

    // _GLIBCXX_RESOLVE_LIB_DEFECTS
    // 265.  std::pair::pair() effects overly restrictive
    /** The default constructor creates @c first and @c second using their
     *  respective default constructors.  */
    pair() :
        first(), second() {
    }

    /** Two objects may be passed to a @c pair constructor to be copied.  */
    pair(typename boost::remove_reference<_T1>::type& __a,
            typename boost::remove_reference<_T2>::type& __b) :
        first(__a), second(__b) {
    }

    /** There is also a templated copy ctor for the @c pair class itself.  */
    template<class _U1, class _U2>
    pair(const pair<_U1, _U2>& __p) :
        first(__p.first), second(__p.second) {
    }
};

/// Two pairs of the same type are equal iff their members are equal.
template<class _T1, class _T2>
inline bool operator==(const pair<_T1, _T2>& __x, const pair<_T1, _T2>& __y) {
    return __x.first == __y.first && __x.second == __y.second;
}

/// <http://gcc.gnu.org/onlinedocs/libstdc++/20_util/howto.html#pairlt>
template<class _T1, class _T2>
inline bool operator<(const pair<_T1, _T2>& __x, const pair<_T1, _T2>& __y) {
    return __x.first < __y.first || (!(__y.first < __x.first) && __x.second
            < __y.second);
}

/// Uses @c operator== to find the result.
template<class _T1, class _T2>
inline bool operator!=(const pair<_T1, _T2>& __x, const pair<_T1, _T2>& __y) {
    return !(__x == __y);
}

/// Uses @c operator< to find the result.
template<class _T1, class _T2>
inline bool operator>(const pair<_T1, _T2>& __x, const pair<_T1, _T2>& __y) {
    return __y < __x;
}

/// Uses @c operator< to find the result.
template<class _T1, class _T2>
inline bool operator<=(const pair<_T1, _T2>& __x, const pair<_T1, _T2>& __y) {
    return !(__y < __x);
}

/// Uses @c operator< to find the result.
template<class _T1, class _T2>
inline bool operator>=(const pair<_T1, _T2>& __x, const pair<_T1, _T2>& __y) {
    return !(__x < __y);
}

/**
 *  @brief A convenience wrapper for creating a pair from two objects.
 *  @param  __x  The first object.
 *  @param  __y  The second object.
 *  @return   A newly-constructed pair<> object of the appropriate type.
 *
 *  The standard requires that the objects be passed by reference-to-const,
 *  but LWG issue #181 says they should be passed by const value.  We follow
 *  the LWG by default.
 */
// _GLIBCXX_RESOLVE_LIB_DEFECTS
// 181.  make_pair() unintended behavior
template<class _T1, class _T2>
inline pair<_T1, _T2> make_pair(_T1 __x, _T2 __y) {
    return pair<_T1, _T2> (__x, __y);
}

}
}
}

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

#include <boost/mpl/if.hpp>
#include <boost/type_traits/add_const.hpp>
#include <boost/type_traits/reference_traits.hpp>

namespace rsc {
namespace patterns {

namespace detail {

template<typename T>
struct force_const {
    typedef typename boost::mpl::if_<typename boost::is_reference<T>::type,
            typename boost::add_reference<typename boost::add_const<
                    typename boost::remove_reference<T>::type>::type>::type,
            typename boost::add_const<T>::type>::type type;
};

}

}
}

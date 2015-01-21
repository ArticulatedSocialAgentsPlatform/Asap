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

#include <boost/shared_ptr.hpp>

#include "detail/ForceConst.h"

namespace rsc {
namespace patterns {

struct pass_through {
};

/**
 * @author Jan Moringen <jmoringe@techfak.uni-bielefeld.de>
 */
template<typename T>
struct dereferencer {
    typedef T& result_type;

    inline result_type operator()(T* t) const {
        return *t;
    }

    inline typename detail::force_const<result_type>::type
    operator()(const T* t) const {
        return *t;
    }
};

/**
 * @author Jan Moringen <jmoringe@techfak.uni-bielefeld.de>
 */
template<typename T>
struct shared_ptr_dereferencer {
    typedef T& result_type;

    inline result_type operator()(boost::shared_ptr<T> t) const {
        return *t;
    }

    inline typename detail::force_const<result_type>::type
    operator()(boost::shared_ptr<const T> t) const {
        return *t;
    }
};

}
}

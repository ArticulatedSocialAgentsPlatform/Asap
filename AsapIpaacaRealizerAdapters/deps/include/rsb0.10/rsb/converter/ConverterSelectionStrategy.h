/* ============================================================
 *
 * This file is part of the RSB project
 *
 * Copyright (C) 2011 Jan Moringen <jmoringe@techfak.uni-bielefeld.de>
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

#include <string>

#include <boost/shared_ptr.hpp>

#include <rsc/runtime/Printable.h>

#include "Converter.h"

namespace rsb {
namespace converter {

/**
 * Implementation of this interface perform mappings of one of the
 * followings forms:
 * - wire-schema -> @ref Converter
 * - data-type -> @ref Converter
 *
 * @author jmoringe
 */
template <typename WireType>
class ConverterSelectionStrategy: public rsc::runtime::Printable {
public:
    typedef typename Converter<WireType>::Ptr ConverterPtr;

    typedef boost::shared_ptr< ConverterSelectionStrategy<WireType> > Ptr;

    /**
     * Tries to look up the converter designator by @a key.
     *
     * @param key A wire-schema or data-type desinated the desired
     *            converter.
     * @return A boost::shared_ptr holding the converter.
     * @throw rsc::runtime::NoSuchObject If there is no converter fo @a key.
     */
    virtual ConverterPtr getConverter(const std::string& key) const = 0;
};

}
}

/* ============================================================
 *
 * This file is part of the RSB project.
 *
 * Copyright (C) 2012 Jan Moringen <jmoringe@techfak.uni-bielefeld.de>
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
 * ============================================================  */

#pragma once

#include <boost/cstdint.hpp>
#include <boost/format.hpp>

#include <rosetta/api.h>

#include "Converter.h"
#include "rsb/rsbexports.h"

namespace rsb {
namespace converter {

/**
 *
 * @tparam Mechanism
 * @tparam DataType
 * @tparam WireSchema
 *
 * @author jmoringe
 */
template <typename Mechanism,
          typename DataType,
          typename WireSchema>
class RosettaConverter : public Converter<std::string> {
public:
    RosettaConverter();
    virtual ~RosettaConverter();

    std::string serialize(const AnnotatedData& data, std::string& wire);

    AnnotatedData deserialize(const std::string& wireType, const std::string& wire);
};

// Implementation

template <typename Mechanism,
          typename DataType,
          typename WireSchema>
RosettaConverter<Mechanism, DataType, WireSchema>::RosettaConverter() :
    Converter<std::string>(rsc::runtime::typeName<DataType>(),
                           boost::str(boost::format("rosetta<%1%,%2%>")
                                      % Mechanism::name()
                                      % WireSchema::name())) {
}

template <typename Mechanism,
          typename DataType,
          typename WireSchema>
RosettaConverter<Mechanism, DataType, WireSchema>::~RosettaConverter() {
}

template <typename Mechanism,
          typename DataType,
          typename WireSchema>
std::string RosettaConverter<Mechanism, DataType, WireSchema>::serialize(const AnnotatedData& data,
                                                                         std::string&         wireData) {
    assert(data.first == getDataType());

    boost::shared_ptr<DataType> object
        = boost::static_pointer_cast<DataType>(data.second);
    boost::uint64_t size = rosetta::packedSize<Mechanism, WireSchema>(*object);
    wireData.resize(size);

    std::vector<unsigned char> temp(size); /* TODO(jmoringe, 2012-04-25): temp */
    rosetta::pack<Mechanism, WireSchema>(*object, temp, 0, size);
    memcpy((void*) &wireData[0], (void*) &temp[0], size);

    return getWireSchema();
}

template <typename Mechanism,
          typename DataType,
          typename WireSchema>
AnnotatedData
RosettaConverter<Mechanism, DataType, WireSchema>::deserialize(const std::string& wireSchema,
                                                               const std::string& wireData) {
    assert(wireSchema == getWireSchema());

    boost::shared_ptr<DataType> result(new DataType());

    std::vector<unsigned char> data(wireData.size()); /* TODO(jmoringe, 2012-04-25): temp */
    memcpy((void*) &data[0], (void*) &wireData[0], wireData.size());
    rosetta::unpack<Mechanism, WireSchema>(data, *result, 0, wireData.size());

    return std::make_pair(getDataType(), result);
}

}
}

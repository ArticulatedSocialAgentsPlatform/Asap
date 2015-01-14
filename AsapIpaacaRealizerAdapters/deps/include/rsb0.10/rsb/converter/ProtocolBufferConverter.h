/* ============================================================
 *
 * This file is part of RSB.
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

#include <rsc/runtime/TypeStringTools.h>

#include "Converter.h"
#include "rsb/rsbexports.h"

namespace rsb {
namespace converter {

/**
 * A generic converter for data types based on Protocol Buffer messages.
 *
 * @author jmoringe
 * @tparam ProtocolBuffer type of the protobuf message to be converted
 */
template<typename ProtocolBuffer>
class ProtocolBufferConverter: public Converter<std::string> {
public:
    ProtocolBufferConverter();
    virtual
    ~ProtocolBufferConverter();

    std::string
    serialize(const AnnotatedData& data, std::string& wire);

    AnnotatedData
    deserialize(const std::string& wireType, const std::string& wire);

private:

    std::string typeNameToProtoName(const std::string& type_name) {
        bool skip = false;
    #ifdef WIN32
        if (type_name.size() >= 6 && type_name.substr(0, 6) == "class ") {
            skip = true;
        }
    #endif
        std::string result = ".";
        bool colon = false;
        for (std::string::const_iterator it
                 = type_name.begin() + (skip ? 6 : 0);
                 it != type_name.end(); ++it) {
            // Consume two (hopefully adjacent) ':', emit one '.'
            if (*it == ':') {
                if (colon) {
                    colon = false;
                } else {
                    result.push_back('.');
                    colon = true;
                }
            } else {
                result.push_back(*it);
            }
        }
        return result;
    }

    std::string typeNameToWireSchema(const std::string& type_name) {
        return typeNameToProtoName(type_name);
    }

};

// Implementation

template<typename ProtocolBuffer>
ProtocolBufferConverter<ProtocolBuffer>::ProtocolBufferConverter() :
    Converter<std::string> (rsc::runtime::typeName<ProtocolBuffer>(),
                            typeNameToWireSchema(rsc::runtime::typeName<
                                                         ProtocolBuffer>())) {
}

template<typename ProtocolBuffer>
ProtocolBufferConverter<ProtocolBuffer>::~ProtocolBufferConverter() {
}

template<typename ProtocolBuffer>
std::string ProtocolBufferConverter<ProtocolBuffer>::serialize(
    const AnnotatedData& data, std::string& wireData) {
    assert(data.first == getDataType());

    boost::shared_ptr<ProtocolBuffer> s = boost::static_pointer_cast<
        ProtocolBuffer>(data.second);
    s->SerializeToString(&wireData);
    return getWireSchema();
}

template<typename ProtocolBuffer>
AnnotatedData ProtocolBufferConverter<ProtocolBuffer>::deserialize(
    const std::string& wireSchema, const std::string& wireData) {
    assert(wireSchema == getWireSchema());

    boost::shared_ptr<ProtocolBuffer> result(new ProtocolBuffer());
    result->ParseFromString(wireData);
    return std::make_pair(getDataType(), result);
}

}
}

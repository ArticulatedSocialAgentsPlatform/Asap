/* ============================================================
 *
 * This file is a part of the RSB project
 *
 * Copyright (C) 2010 by Sebastian Wrede <swrede at techfak dot uni-bielefeld dot de>
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

#include <set>
#include <string>
#include <utility>

#include <boost/shared_ptr.hpp>

#include <rsc/runtime/Printable.h>
#include <rsc/runtime/TypeStringTools.h>

#include "../Event.h"

#include "rsb/rsbexports.h"

namespace rsb {
namespace converter {

#define RSB_TYPE_TAG(T) (reinterpret_cast<T*> (0))

/**
 * @author swrede
 * @author jwienke
 * @tparam WireType is the serialization format, uchar, string, binary, ...
 */
template<class WireType>
class Converter: public rsc::runtime::Printable {
public:

    virtual ~Converter() {
    }

    /**
     * Serialized the given domain object to the wire.
     *
     * @param data data to serialize
     * @param wire the wire to serialize on
     * @return the wire schema the data is encoded with
     * @throw SerializationException if the serialization failed
     */
    virtual std::string
    serialize(const AnnotatedData& data, WireType& wire) = 0;

    /**
     * Deserializes a domain object from a wire type.
     *
     * @param wireSchema type of the wire message
     * @param wire the wire containing the data
     * @return the deserialized domain object annotated with its data type name
     * @throw SerializationException if deserializing the message fails
     */
    virtual AnnotatedData deserialize(const std::string& wireSchema,
            const WireType& wire) = 0;

    /**
     * Returns the name of the data type this converter is applicable for.
     *
     * @return name of the data type this converter can be used for
     */
    virtual std::string getDataType() const {
        return dataType;
    }

    /**
     * Returns the name of the wire schema this converter can (de)serialize
     * from/to.
     *
     * @return name of the wire schema from/to this converter can
     *         (de)serialize
     */
    virtual std::string getWireSchema() const {
        return wireSchema;
    }

    typedef boost::shared_ptr<Converter<WireType> > Ptr;

protected:

    /**
     * Creates a new instance of this class with automatic handling for types.
     *
     * @param dataType data type this converter can serialize
     * @param wireSchema wire schema this converter can deserialize
     * @param dummy This parameter is used to disambiguate constructor
     *              signatures when WireType is std::string .
     */
    Converter(const std::string& dataType, const std::string& wireSchema, bool dummy = true) :
        dataType(dataType), wireSchema(wireSchema) {
        ((void) dummy);
    }

    /**
     * Creates a new instance of this class with a data type
     * string that is inferred based on the template parameter
     * @a DataType
     *
     * @tparam DataType type of the objects that the converter
     *                  (de)serializes. Use the RSB_TYPE_TAG macro with this.
     * @param wireSchema wire schema from/to this converter can
     *                   (de)serialize.
     */
    template<typename DataType>
    Converter(const std::string& wireSchema, const DataType* /*unused*/= 0) :
        dataType(rsc::runtime::typeName<DataType>()), wireSchema(wireSchema) {
    }

private:

    std::string dataType;
    std::string wireSchema;

    std::string getClassName() const {
        return rsc::runtime::typeName(*this);
    }

    void printContents(std::ostream& stream) const {
        stream << "wireType = " << rsc::runtime::typeName<WireType>()
               << ", wireSchema = " << getWireSchema()
               << ", dataType = " << getDataType();
    }
};

#if defined(_WIN32)
RSB_EXPIMP template class RSB_EXPORT Converter<std::string>;
#endif

}
}

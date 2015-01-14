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

#include <typeinfo>
#include <stdexcept>
#include <string>
#include <ostream>

#include <boost/type_traits.hpp>

#include <boost/format.hpp>

#include "Demangle.h"
#include "ContainerIO.h"
#include "rsc/rscexports.h"

namespace boost {

template<typename T>
struct has_stream_output: public false_type {
};

template<>
struct has_stream_output<bool> : public true_type {
};

template<>
struct has_stream_output<char> : public true_type {
};

template<>
struct has_stream_output<unsigned char> : public true_type {
};

template<>
struct has_stream_output<short> : public true_type {
};

template<>
struct has_stream_output<unsigned short> : public true_type {
};

template<>
struct has_stream_output<int> : public true_type {
};

template<>
struct has_stream_output<unsigned int> : public true_type {
};

template<>
struct has_stream_output<long> : public true_type {
};

template<>
struct has_stream_output<unsigned long> : public true_type {
};

template<>
struct has_stream_output<float> : public true_type {
};

template<>
struct has_stream_output<double> : public true_type {
};

template<>
struct has_stream_output<char*> : public true_type {
};

template<>
struct has_stream_output<std::string> : public true_type {
};

template<typename T>
struct has_stream_output<std::vector<T> > : public has_stream_output<T>::type {
};

template<>
struct has_stream_output<std::type_info> : public true_type {
};

}

namespace rsc {
namespace runtime {

/**
 * Returns a (demangled) string representation of @a type.
 *
 * @param type The type that's name should be returned.
 *
 * @return Demangled type name of @a type.
 * @throw runtime_error If demangling the type's name fails.
 *
 * @author Jan Moringen <jmoringe@techfak.uni-bielefeld.de>
 */
RSC_EXPORT std::string typeName(const std::type_info& type);

/**
 * Returns a (demangled) string representation of the type of the template
 * parameter.
 *
 * @return Demangled type name of the type of the template parameter.
 * @throw runtime_error If demangling the type's name fails.
 *
 * @author Jan Moringen <jmoringe@techfak.uni-bielefeld.de>
 */
template<typename T>
std::string typeName();

/**
 * Returns a (demangled) string representation of the type of @a object.
 *
 * @param object The object, the stringified type of which should be returned.
 * @return Demangled type name of the type of @a object.
 * @throw runtime_error If demangling the type's name fails.
 *
 * @author Jan Moringen <jmoringe@techfak.uni-bielefeld.de>
 */
template<typename T>
std::string typeName(const T& object);

/**
 * Returns one of two to strings depending on whether type @a T is known to be
 * able to support stream output (using operator<<).
 *
 * @param known_type_string The string to be used if type @a T supports stream
 *                          output. This string may contain @c %1% substrings
 *                          that will be replaced by the result of writing @a
 *                          value to a stream.
 * @param unknown_type_string The string to be used if type @a T does not
 *                            support stream output. This string will be
 *                            returned unmodified.
 * @param value The value that is to be embedded in @a known_type_string if that
 *              is possible.
 * @return- @a known_type_string   - If type @a T supports stream output.
 *        - @a unknown_type_string - otherwise.
 * @throw format_error If the format specified in @a known_type_string is
 *                     invalid.
 *
 * @note As it cannot be deduced automatically whether a type T has
 *       @c operator<<(ostream,T) defined or not, work has to be done to support
 *       user-defined types here.
 *
 * @author Jan Moringen <jmoringe@techfak.uni-bielefeld.de>
 */
template<typename T>
std::string typeString(const std::string& known_type_string,
        const std::string& unknown_type_string, const T& value);

}
}

namespace std {

template<typename Ch, typename Tr>
basic_ostream<Ch, Tr>&
operator<<(basic_ostream<Ch, Tr>& stream, const type_info& type_info_);

}

// free function implementations

namespace rsc {
namespace runtime {

template<typename T>
std::string typeName() {
    return demangle(typeid(T).name());
}

template<typename T>
std::string typeName(const T& object) {
    return demangle(typeid(object).name());
}

template<typename T>
std::string doTypeString(const std::string& known_type_string,
        const std::string&, const T& value, boost::true_type) {
    return (boost::format(known_type_string) % value).str();
}

template<typename T>
std::string doTypeString(const std::string&,
			 const std::string& unknown_type_string, const T&, boost::false_type) {
    return unknown_type_string;
}

template<typename T>
std::string typeString(const std::string& known_type_string,
        const std::string& unknown_type_string, const T& value) {
    return doTypeString(known_type_string, unknown_type_string, value,
            boost::has_stream_output<T>());
}

}
}

namespace std {

template<typename Ch, typename Tr>
basic_ostream<Ch, Tr>&
operator<<(basic_ostream<Ch, Tr>& stream, const type_info& type_info_) {
    stream << rsc::runtime::typeName(type_info_);

    return stream;
}

}

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

#include <string>
#include <map>
#include <utility>
#include <iostream>

#include <boost/any.hpp>
#include <boost/lexical_cast.hpp>
#include <boost/format.hpp>
#include <boost/operators.hpp>

#include "rsc/rscexports.h"

#include "NoSuchObject.h"
#include "TypeStringTools.h"

namespace rsc {
namespace runtime {

/**
 * @a Properties objects are basically glorified @c map<string, boost::any>
 * objects.
 *
 * In addition to the implied semantics, Properties objects support basic stream
 * output and merging and provide some convenience functions for setting and
 * retrieving values.
 *
 * @author Jan Moringen <jmoringe@techfak.uni-bielefeld.de>
 */
class RSC_EXPORT Properties: public std::map<std::string, boost::any>,
                             public boost::equality_comparable<Properties> { // TODO left_shiftable from Boost.Operators
    friend RSC_EXPORT Properties
    operator<<(const Properties& properties1, const Properties& properties2);
    template<typename Ch, typename Tr>
    friend std::basic_ostream<Ch, Tr>
            &
            operator<<(std::basic_ostream<Ch, Tr>& stream,
                    const Properties& properties);
public:

    /**
     * Merge with @a other. Values from @a other replace values with
     * identical keys.
     *
     * @param other New Properties which should take precedence over
     * already present values.
     * @return The modified Properties object.
     */
    Properties&
    operator<<=(const Properties& other); // TODO boost::left_shiftable

    /**
     * Compare to @a other. All keys and values are checked for
     * equality. Values can only be checked if they are of type
     * std::string, bool, int, unsigned int or double.
     *
     * @param other The Properties object to which this one should be
     * compared.
     *
     * @throw std::runtime_error If any value is of a type for which
     * comparison has not been explicitly implemented.
     */
    bool operator==(const Properties& other) const;

    bool
    has(const std::string& name) const throw (); // TODO check type

    /**
     * @throw NoSuchObject
     * @throw boost::bad_any_cast
     */
    template<typename T>
    T
    get(const std::string& name) const;

    /**
     * @throw boost::bad_any_cast
     */
    template<typename T>
    T
    get(const std::string& name, const T& default_) const;

    /** Parse the value of the property @a name as type @a T and
     * return the parsed value.
     *
     * This assumes that the stored value is of type @ref std::string.
     *
     * @tparam T Desired target type of the conversion.
     * @param name Name of the property.
     * @throw NoSuchObject If there is no propery named @a name.
     * @throw std::bad_cast If the string value of the property cannot
     * be converted to the desired target type.
     */
    template<typename T>
    T getAs(const std::string& name) const;

    /** Parse the value of the property @a name as type @a T and
     * return the parsed value.
     *
     * This assumes that the stored value is of type @ref std::string.
     *
     * @tparam T Desired target type of the conversion.
     * @param name Name of the property.
     * @param default_ A fallback value which is returned when there
     * is no property named @a name.
     * @throw std::bad_cast If the string value of the property cannot
     * be converted to the desired target type.
     */
    template<typename T>
    T getAs(const std::string& name, const T& default_) const;

    /**
     * Sets a new property in the map. If a property with this name exits, the
     * new one will not be inserted. The old property has to be removed first.
     *
     * @param name name of the property to set
     * @param value value to set
     * @return @c true of the property was inserted, @c false if a property with
     *         the given name existed and nothing was changed
     */
    template<typename Target, typename T>
    bool
    set(const std::string& name, const T& value) throw ();
};

/**
 * Merge @a left and @a right. Values in @a right take precedence over
 * values with identical keys in @a left.
 *
 * @param left Properties with lower precedence.
 * @param right Properties with higher precedence.
 *
 * @return A new Properties object which contains the result of the merge.
 */
RSC_EXPORT Properties
operator<<(const Properties& left, const Properties& right);

template<typename Ch, typename Tr>
std::basic_ostream<Ch, Tr>&
operator<<(std::basic_ostream<Ch, Tr>& stream, const std::pair<std::string,
        boost::any> property);

template<typename Ch, typename Tr>
std::basic_ostream<Ch, Tr>&
operator<<(std::basic_ostream<Ch, Tr>& stream, const Properties& properties);

// Properties implementation

template<typename T>
T Properties::get(const std::string& name) const {
    const_iterator it;
    if ((it = find(name)) == end()) {
        throw NoSuchObject(
                (boost::format("no such property `%1%'") % name).str());
    }

    try {
        return boost::any_cast<T>(it->second);
    } catch (const boost::bad_any_cast&) {
        std::cerr
                << (boost::format(
                        "properties: type mismatch in get for `%1%': requested: %2%; actual: %3%")
                        % name % typeName<T> () % typeName(it->second.type()))
                << std::endl;
        throw;
    }
}

template<typename T>
T Properties::get(const std::string& name, const T& default_) const {
    const_iterator it;
    if ((it = find(name)) == end()) {
        return default_;
    }

    try {
        return boost::any_cast<T>(it->second);
    } catch (const boost::bad_any_cast&) {
        std::cerr << (boost::format(
                "properties type mismatch in get for `%1%': requested: %2%; actual: %3%")
                 % name % typeName<T> () % typeName(it->second.type())) << std::endl;
        throw;
    }
}

template<typename T>
T Properties::getAs(const std::string& name) const {
    std::string value = get<std::string>(name);
    try {
        return boost::lexical_cast<T>(value);
    } catch (const std::bad_cast&) {
        std::cerr
            << (boost::format(
                    "properties: type conversion failure in getAs for `%1%': requested: %2%; value: \"%3%\"")
                % name % typeName<T> () % value)
            << std::endl;
        throw;
    }
}

template<typename T>
T Properties::getAs(const std::string& name, const T& default_) const {
    if (has(name)) {
        return getAs<T>(name);
    } else {
        return default_;
    }
}

template<typename Target, typename T>
bool Properties::set(const std::string& name, const T& value) throw () {
    erase(name);
    return insert(std::make_pair(name, static_cast<Target> (value))).second;
}

// free function implementations

template<typename Ch, typename Tr>
std::basic_ostream<Ch, Tr>&
operator<<(std::basic_ostream<Ch, Tr>& stream, const Properties& properties) {
    stream << "p{ ";

    for (Properties::const_iterator it = properties.begin(); it
            != properties.end();) {
        stream << it->first << ": ";

        if (it->second.type() == typeid(std::string)) {
            stream << "\"" << boost::any_cast<std::string>(it->second) << "\"";
	} else if (it->second.type() == typeid(bool)) {
            stream << boost::any_cast<bool>(it->second);
	} else if (it->second.type() == typeid(int)) {
            stream << boost::any_cast<int>(it->second);
	} else if (it->second.type() == typeid(unsigned int)) {
	  stream << boost::any_cast<unsigned int>(it->second);
	} else if (it->second.type() == typeid(double)) {
            stream << boost::any_cast<double>(it->second);
	} else {
            stream << "<" + typeName(it->second.type()) + ">"; // TODO this sucks
	}

        stream << ((++it) != properties.end() ? ", " : "");
    }

    stream << " }";

    return stream;
}

}
}

/* ============================================================
 *
 * This file is a part of the RSC project.
 *
 * Copyright (C) 2010 by Johannes Wienke <jwienke at techfak dot uni-bielefeld dot de>
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

#include <exception>
#include <map>
#include <ostream>
#include <set>
#include <vector>

#include <boost/shared_ptr.hpp>
#include <boost/weak_ptr.hpp>

#include "rsc/rscexports.h"

namespace rsc {
namespace runtime {

/**
 * A base class that defines utility functions for printing objects to streams.
 *
 * The default output format is <code>className[contents]</code> where
 * @c className is returned by #getClassName and contents is printed by
 * #printContents. Moreover, if a pointer is printed, @c * is prepended to the
 * name and <code>at 0xXYZ</code> is appended after the contents with the
 * object's memory location.
 *
 * In general it should be sufficient to override #printContents but if you want
 * to specify the whole output format, overriding print is needed.
 *
 * It is advisable to always use virtual inheritance with this class to avoid
 * problems with diamond-shaped inheritance later on.
 *
 * @author jwienke
 */
class RSC_EXPORT Printable {
public:

    virtual ~Printable();

    /**
     * Implement this method and return a human-readable class name. This method
     * should always return the same class name. Its existence is an admission
     * to missing RTTI functionalities in C++.
     *
     * @return human-readable name of the class.
     */
    virtual std::string getClassName() const;

    /**
     * This method does the actual printing of class debug information and
     * should be overridden in subclasses.
     *
     * @param stream stream to print on
     */
    virtual void printContents(std::ostream& stream) const;

    /**
     * Constructs and prints the final debug output.
     *
     * @param stream stream to print on
     */
    virtual void print(std::ostream& stream) const;

};

/**
 * Output operator on std::ostream for reference Printables.
 *
 * @param stream stream to print on
 * @param printable Printable to print
 * @return the \c stream
 */
RSC_EXPORT std::ostream& operator<<(std::ostream& stream,
        const Printable& printable);

/**
 * Output operator on std::ostream for pointer Printables.
 *
 * @param stream stream to print on
 * @param printable Printable to print
 * @return the \c stream
 */
RSC_EXPORT std::ostream& operator<<(std::ostream& stream,
        const Printable* printable);

/**
 * It seems boost::weak_ptr's do not have a stream operator. So provide one.
 *
 * @param stream stream to print on
 * @param p pointer to print
 * @return the stream that was printed on
 * @tparam Y pointee type
 */
template<class Y>
std::ostream&  operator<<(std::ostream&  stream, boost::weak_ptr<Y> const & p) {
    return stream << p.lock().get();
}

}
}

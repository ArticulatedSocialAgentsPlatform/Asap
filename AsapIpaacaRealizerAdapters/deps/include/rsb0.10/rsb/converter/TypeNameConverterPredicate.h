/* ============================================================
 *
 * This file is part of the RSB project
 *
 * Copyright (C) 2012 Johannes Wienke <jwienke at techfak dot uni-bielefeld dot de>
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

#include "PredicateConverterList.h"
#include "rsb/rsbexports.h"

namespace rsb {
namespace converter {

/** 
 * Objects of this class select @ref Converter s by matching the query
 * wire-schema or data-type against a stored string. The functionality of this
 * class is a subset of RegexConverterPredicate. Using this class you do not
 * need to care to replace special characters for regular expressions and the
 * implementation is probably much faster.
 *
 * @author jwienke
 */
class RSB_EXPORT TypeNameConverterPredicate: public ConverterPredicate {
public:

    /** 
     * Construct a new TypeNameConverterPredicate that matches queries
     * against the wanted type name @a typeName.
     * 
     * @param regex The regular expression the new predicate should
     * use.
     */
    TypeNameConverterPredicate(const std::string& typeName);

    bool match(const std::string& key) const;
private:
    std::string typeName;

    std::string getClassName() const;

    void printContents(std::ostream& /*stream*/) const;
};

}
}

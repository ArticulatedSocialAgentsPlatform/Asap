/* ============================================================
 *
 * This file is a part of the RSB project.
 *
 * Copyright (C) 2011 by Johannes Wienke <jwienke at techfak dot uni-bielefeld dot de>
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
#include <vector>
#include <ostream>

#include <boost/shared_ptr.hpp>
#include <boost/operators.hpp>

#include "rsb/rsbexports.h"

namespace rsb {

/**
 * Scope is a descriptor for a hierarchical channel of the unified bus. It can
 * be described through a syntax like "/a/parent/scope/".
 *
 * @author jwienke
 */
class RSB_EXPORT Scope: boost::totally_ordered<Scope> {
public:

    /**
     * Constructs scope from a string syntax.
     *
     * @param scope string representation of the desired scope
     * @throw std::invalid_argument invalid syntax
     */
    Scope(const std::string& scope);

    /**
     * Constructs scope from a string syntax.
     *
     * @param scope string representation of the desired scope
     * @throw std::invalid_argument invalid syntax
     */
    Scope(const char* scope);

    /**
     * Creates a scope representing "/". Use this wisely!
     */
    Scope();

    /**
     * Destructor.
     */
    virtual ~Scope();

    /**
     * Returns all components of the scope as an ordered list. Components are
     * the names between the separator character '/'. The first entry in the
     * list is the highest level of hierarchy. The scope '/' returns an empty
     * list.
     *
     * @return components of the represented scope as ordered list with highest
     *         level as first entry
     */
    const std::vector<std::string>& getComponents() const;

    /**
     * Reconstructs a fully formal string representation of the scope with
     * leading an trailing slashes.
     *
     * @return string representation of the scope
     */
    const std::string& toString() const;

    /**
     * Creates a new scope that is a sub-scope of this one with the subordinated
     * scope described by the given argument. E.g. "/this/is/".concat("/a/test/")
     * results in "/this/is/a/test".
     *
     * @param childScope child to concatenate to the current scope for forming a
     *                   sub-scope
     * @return new scope instance representing the created sub-scope
     */
    Scope concat(const Scope& childScope) const;

    /**
     * Tests whether this scope is a sub-scope of the given other scope, which
     * means that the other scope is a prefix of this scope. E.g. "/a/b/" is a
     * sub-scope of "/a/".
     *
     * @param other other scope to test
     * @return @c true if this is a sub-scope of the other scope, equality gives
     *         @c false, too
     */
    bool isSubScopeOf(const Scope& other) const;

    /**
     * Inverse operation of #isSubScopeOf.
     *
     * @param other other scope to test
     * @return @c true if this scope is a strict super scope of the other scope.
     *         equality also gives @c false.
     */
    bool isSuperScopeOf(const Scope& other) const;

    /**
     * Generates all super scopes of this scope including the root scope "/".
     * The returned list of scopes is ordered by hierarchy with "/" being the
     * first entry.
     *
     * @param includeSelf if set to @c true, this scope is also included as last
     *                    element of the returned list
     * @return list of all super scopes ordered by hierarchy, "/" being first
     */
    std::vector<Scope> superScopes(const bool& includeSelf = false) const;

    bool operator==(const Scope& other) const;
    bool operator<(const Scope& other) const;

    static const char COMPONENT_SEPARATOR;
private:

    /**
     * Updates the contents of @c scopestring based on @c components. Calling
     * this method is necessary if @c components is modified directly without
     * using one of the constructors.
     */
    void updateStringCache();

    std::string scopestring;
    std::vector<std::string> components;

};

typedef boost::shared_ptr<Scope> ScopePtr;

RSB_EXPORT std::ostream& operator<<(std::ostream& stream, const Scope& scope);

}

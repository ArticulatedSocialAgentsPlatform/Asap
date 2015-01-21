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

#include "Filter.h"
#include "../Scope.h"
#include "rsb/rsbexports.h"

namespace rsb {
namespace filter {

/**
 * @author swrede
 */
class RSB_EXPORT ScopeFilter: public Filter {
public:
    ScopeFilter(const Scope& scope);
    virtual ~ScopeFilter();

    std::string getClassName() const;
    void printContents(std::ostream& stream) const;

    bool match(EventPtr e);

    Scope getScope();

    void notifyObserver(FilterObserverPtr fo, FilterAction::Types at);

private:
    Scope scope;
};

}

}


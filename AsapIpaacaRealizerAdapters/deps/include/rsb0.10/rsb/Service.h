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

#include <ostream>
#include <set>

#include <boost/shared_ptr.hpp>

#include <rsc/runtime/Printable.h>

#include "Participant.h"
#include "Scope.h"
#include "rsb/rsbexports.h"

namespace rsb {

class Service;
typedef boost::shared_ptr<Service> ServicePtr;

/**
 * A service provides a hierarchical structure for organizing participants.
 * A service forms a parent scope for subordinated participants and
 * sub-services.
 *
 * Services need to be thread-safe.
 *
 * @author jwienke
 */
class RSB_EXPORT Service: public Participant {
public:

    virtual ~Service();

    void printContents(std::ostream& stream) const;

    /**
     * Returns all participants that reside under this service
     *
     * @return set of participants of this service
     */
    virtual std::set<ParticipantPtr> getParticipants() const = 0;

    /**
     * Registers a new participant in this service.
     *
     * @param participant participant to register
     * @throw std::invalid_argument participant is not in a sub-scope of the
     *                              service's scope
     */
    virtual void addParticipant(ParticipantPtr participant) = 0;

    /**
     * Removes a previously registered participant from this service.
     *
     * @param participant participant to remove
     */
    virtual void removeParticipant(ParticipantPtr participant) = 0;

protected:

    /**
     * Constructs a new service with the given scope.
     *
     * @param scope scope of the service
     */
    Service(const Scope& scope);

private:

    Scope scope;

};

}


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

#include <boost/cstdint.hpp>
#include <boost/operators.hpp>
#include <boost/thread.hpp>
#include <boost/shared_ptr.hpp>

#include <rsc/misc/UUID.h>
#include <rsc/runtime/Printable.h>

#include "rsb/rsbexports.h"

namespace rsb {

/**
 * A unique ID for events in RSB. Ids are composed of the sending
 * participant's ID and a sequence number and can optionally be converted to
 * UUIDs.
 *
 * @author jwienke
 */
class RSB_EXPORT EventId: boost::totally_ordered<EventId>,
        public rsc::runtime::Printable {
public:

    EventId(const rsc::misc::UUID& participantId,
            const boost::uint32_t& sequenceNumber);
    virtual ~EventId();

    rsc::misc::UUID getParticipantId() const;
    boost::uint32_t getSequenceNumber() const;

    rsc::misc::UUID getAsUUID() const;

    bool operator==(const EventId& other) const;
    bool operator<(const EventId& other) const;

    std::string getClassName() const;
    void printContents(std::ostream& stream) const;

private:

    /**
     * The id of the sending participant.
     */
    rsc::misc::UUID participantId;
    boost::uint32_t sequenceNumber;

    /**
     * A cache for the generated uuid.
     */
    mutable rsc::misc::UUIDPtr id;

};

typedef boost::shared_ptr<EventId> EventIdPtr;

}

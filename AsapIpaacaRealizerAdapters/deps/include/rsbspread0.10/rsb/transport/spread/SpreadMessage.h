/* ============================================================
 *
 * This file is part of the rsb-spread project.
 *
 * Copyright (C) 2010 by Sebastian Wrede <swrede at techfak dot uni-bielefeld dot de>
 * Copyright (C) 2013 Jan Moringen <jmoringe@techfak.uni-bielefeld.de>
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
#include <list>

#include <boost/shared_ptr.hpp>

#include "rsb/transport/spread/rsbspreadexports.h"

namespace rsb {
namespace transport {
namespace spread {

/**
 * Default message QOS for sending is RELIABLE.
 *
 * @author swrede
 */
class RSBSPREAD_EXPORT SpreadMessage {
public:

    enum Type {
        REGULAR = 0x0001, MEMBERSHIP = 0x0002, OTHER = 0xFFFF,
    };

    /**
     * Message reliability and QoS types. For some strange reasons the int
     * values directly resemble the sp.h defines. ;)
     *
     * @author jwienke
     */
    enum QOS {
        UNRELIABLE = 0x00000001,
        RELIABLE = 0x00000002,
        FIFO = 0x00000004,
        CASUAL = 0x00000008,
        AGREED = 0x00000010,
        SAFE = 0x00000020
    };

    /**
     * Creates a new empty message with undefined type #OTHER and QoS
     * #UNRELIABLE.
     */
    SpreadMessage();

    /**
     * Creates a new message with the specified type and QoS #UNRELIABLE.
     *
     * @param mt message type
     */
    SpreadMessage(const Type& mt);

    /**
     * Creates a message with the specified data and message type #OTHER and QoS
     * #UNRELIABLE.
     *
     * @param d data to set
     */
    SpreadMessage(const std::string& d);

    /**
     * Creates a message with the specified data and message type #OTHER and QoS
     * #UNRELIABLE.
     *
     * @param d data to set
     */
    SpreadMessage(const char* d);

    virtual ~SpreadMessage();

    void setData(const std::string& doc);
    void setData(const char* d);
    std::string getDataAsString() const;
    const char* getData() const;
    int getSize() const;
    SpreadMessage::Type getType() const;
    void setType(Type mt);
    QOS getQOS() const;
    void setQOS(const QOS& qos);

    void addGroup(const std::string& name);
    unsigned int getGroupCount() const;
    std::list<std::string>::const_iterator getGroupsBegin() const;
    std::list<std::string>::const_iterator getGroupsEnd() const;

    /**
     * Resets this message to a message of type #OTHER with no contents and
     * groups.
     */
    void reset();

private:
    std::string data;
    std::list<std::string> groups;
    Type type;
    QOS qos;
};

typedef boost::shared_ptr<SpreadMessage> SpreadMessagePtr;

}
}
}

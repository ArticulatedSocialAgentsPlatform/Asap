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

#include <map>
#include <set>
#include <string>

#include <boost/cstdint.hpp>
#include <boost/operators.hpp>
#include <boost/date_time.hpp>

#include <rsc/runtime/Printable.h>
#include <rsc/misc/UUID.h>

#include "rsb/rsbexports.h"

namespace rsb {

/**
 * Framework-supplied meta data attached to each event that give
 * information e.g. about timing issues.
 *
 * For all timestamps UTC unix timestamps are assumed. For the conversion from
 * boost::posix_time::ptime the client has to ensure that the ptim is given in
 * UTC (e.g. using universal_time).
 *
 * @author jwienke
 */
class RSB_EXPORT MetaData: public virtual rsc::runtime::Printable,
        boost::equality_comparable<MetaData> {
public:

    MetaData();
    virtual ~MetaData();

    std::string getClassName() const;
    void printContents(std::ostream& stream) const;

    /**
     * Returns the ID (a UUID) of the sending participant.
     *
     * @return A copy of the sender id UUID object.
     */
    rsc::misc::UUID getSenderId() const;

    /**
     * Sets the ID (a UUID) of the sending participant.
     *
     * @param senderId id of the sending participant
     */
    void setSenderId(const rsc::misc::UUID& senderId);

    /**
     * @name framework timestamps
     *
     * Timestamps supplied by the framework itself.
     */
    //@{
    /**
     * Returns a time stamp that is automatically filled with the time the event
     * instance was created by the language binding. This should usually reflect
     * the time at which the notified condition most likely occurred in the
     * sender. If event instances are reused, it has to be reset manually by the
     * client.
     *
     * This timestamp is initially set to the creating time stamp of this
     * instance.
     *
     * @return timestamp in microseconds
     */
    boost::uint64_t getCreateTime() const;
    /**
     * Sets the time stamp that is automatically filled with the time the event
     * instance was created by the language binding. This should usually reflect
     * the time at which the notified condition most likely occurred in the
     * sender. If event instances are reused, it has to be reset manually by the
     * client.
     *
     * @param time timestamp in microseconds or 0 to use current system time
     */
    void setCreateTime(const boost::uint64_t& time = 0);
    /**
     * @param time time in seconds since unix epoche.
     */
    void setCreateTime(const double& time);
    void setCreateTime(const boost::posix_time::ptime& time);

    /**
     * Returns the time at which the generated notification for an event was
     * sent on the bus (after serialization).
     *
     * @return timestamp in microseconds
     */
    boost::uint64_t getSendTime() const;
    /**
     * Sets the time at which the generated notification for an event was
     * sent on the bus (after serialization).
     *
     * @param time timestamp in microseconds or 0 to use current system time
     */
    void setSendTime(const boost::uint64_t& time = 0);
    void setSendTime(const double& time);
    void setSendTime(const boost::posix_time::ptime& time);

    /**
     * Returns the time at which an event is received by listener in its encoded
     * form.
     *
     * @return timestamp in microseconds
     */
    boost::uint64_t getReceiveTime() const;
    /**
     * Sets the time at which an event is received by listener in its encoded
     * form.
     *
     * @param time timestamp in microseconds or 0 to use current system time
     */
    void setReceiveTime(const boost::uint64_t& time = 0);
    void setReceiveTime(const double& time);
    void setReceiveTime(const boost::posix_time::ptime& time);

    /**
     * Returns the time at which an event was decoded and will be dispatched to
     * the client as soon as possible (set directly before passing it to the
     * client handler).
     *
     * @return timestamp in microseconds
     */
    boost::uint64_t getDeliverTime() const;
    /**
     * Sets the time at which an event was decoded and will be dispatched to
     * the client as soon as possible (set directly before passing it to the
     * client handler).
     *
     * @param time timestamp in microseconds or 0 to use current system time
     */
    void setDeliverTime(const boost::uint64_t& time = 0);
    void setDeliverTime(const double& time);
    void setDeliverTime(const boost::posix_time::ptime& time);
    //@}

    /**
     * @name user timestamps
     *
     * Additional timestamps that can be filled by the framework client. Keys
     * are unique.
     */
    //@{
    /**
     * Returns the keys of all available user times.
     *
     * @return set of all keys
     */
    std::set<std::string> userTimeKeys() const;
    /**
     * Checks whether a user-provided timestamp with the given key exists
     *
     * @param key the key to check
     * @return @c true if a timestamp for the given key exists, else @c false
     */
    bool hasUserTime(const std::string& key) const;
    /**
     * Returns the user timestamp stored under the provided key.
     *
     * @param key key of the user-provided timestamp
     * @return timestamp
     * @throw std::invalid_argument no timestamp stored und the provided key
     */
    boost::uint64_t getUserTime(const std::string& key) const;
    /**
     * Sets a user timestamp and replaces existing entries.
     *
     * @param key the key for the timestamp
     * @param time time in microseconds or 0 to use current system time
     */
    void setUserTime(const std::string& key, const boost::uint64_t& time = 0);
    void setUserTime(const std::string& key, const double& time);
    void setUserTime(const std::string& key, const boost::posix_time::ptime& time);

    std::map<std::string, boost::uint64_t>::const_iterator userTimesBegin() const;
    std::map<std::string, boost::uint64_t>::const_iterator userTimesEnd() const;
    //@}

    /**
     * @name user infos
     *
     * A set of key-value style string infos that can be used by the client.
     * Keys are unique.
     */
    //@{
    /**
     * Returns all keys of user-defined infos.
     *
     * @return set of all defined keys
     */
    std::set<std::string> userInfoKeys() const;
    /**
     * Checks whether a user info exists under the provided key.
     *
     * @param key key to check
     * @return @c true if an info for the key is defined, else @c false
     */
    bool hasUserInfo(const std::string& key) const;
    /**
     * Returns the user-defined string for the given key.
     *
     * @param key key to look up
     * @return user info given for this key
     * @throw std::invalid_argument no info set for the specified key
     */
    std::string getUserInfo(const std::string& key) const;
    /**
     * Sets a user info with the specified key and value or replaces and already
     * existing one
     *
     * @param key the key to set
     * @param value the user value
     */
    void setUserInfo(const std::string& key, const std::string& value);
    std::map<std::string, std::string>::const_iterator userInfosBegin() const;
    std::map<std::string, std::string>::const_iterator userInfosEnd() const;
    //@}

    bool operator==(const MetaData& other) const;

private:
    rsc::misc::UUID senderId;

    void checkedTimeStampSet(boost::uint64_t& timestamp, const boost::uint64_t& proposedValue);
    void checkedTimeStampSet(boost::uint64_t& timestamp, const double& proposedValue);
    void checkedTimeStampSet(boost::uint64_t& timestamp, const boost::posix_time::ptime& proposedValue);

    static const boost::posix_time::ptime UNIX_EPOCH;

    boost::uint64_t createTime;
    boost::uint64_t sendTime;
    boost::uint64_t receiveTime;
    boost::uint64_t deliverTime;

    std::map<std::string, boost::uint64_t> userTimes;
    std::map<std::string, std::string> userInfos;

};

RSB_EXPORT std::ostream& operator<<(std::ostream& stream, const MetaData& meta);

}

/* ============================================================
 *
 * This file is a part of the RSC project
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

#include <ostream>
#include <string>

#include <boost/uuid/uuid.hpp>
#include <boost/uuid/uuid_generators.hpp>
#include <boost/shared_ptr.hpp>

#include "rsc/rscexports.h"

namespace rsc {
namespace misc {

/**
 * Encapsulates the generation and handling of UUIDs.
 *
 * @author swrede
 */
class RSC_EXPORT UUID {
public:

    /**
     * Creates a new UUID object that is either random or the nil
     * UUID.
     *
     * @param random If @c true, a random UUID is created. Otherwise
     * the nil UUID is created.
     */
    explicit UUID(const bool& random = true);

    /**
     * Parses a UUID from a string. Various default formats are
     * accepted.
     *
     * @param uuid A string representation of the desired UUID.
     * @throw std::runtime_error given string is not acceptable as a
     * UUID
     */
    explicit UUID(const std::string& uuid);

    /**
     * Parses a UUID from a string. Various default formats are
     * accepted.
     *
     * @param uuid A string representation of the desired UUID.
     * @throw std::runtime_error given string is not acceptable as a
     * UUID
     */
    explicit UUID(const char* uuid);

    /**
     * Generates a uuid from the given 16 byte representation.
     *
     * @param data 16 byte representation of a uuid
     */
    explicit UUID(boost::uint8_t* data);

    /**
     * Generates a uuid for @a name in namespace @a ns.
     *
     * @param ns Namespace in which @a name should be placed.
     * @param name A unique name within namespace @a ns.
     */
    UUID(const UUID& ns, const std::string& name);

    virtual ~UUID();

    /**
     * Returns the contained UUID on boost format.
     *
     * @return uuid in boost format.
     */
    boost::uuids::uuid getId() const;

    /**
     * Returns a string representing the UUID.
     *
     * @return string representation of the UUID
     */
    std::string getIdAsString() const;

    bool operator==(const UUID& other) const;
    bool operator!=(const UUID& other) const;
    bool operator<(const UUID& other) const;

    friend RSC_EXPORT std::ostream& operator<<(std::ostream& stream, const UUID& id);

private:

    boost::uuids::uuid id;
    // TODO refactor to singleton
    static boost::uuids::nil_generator nilGen;
    static boost::uuids::basic_random_generator<boost::mt19937> randomGen;

};

typedef boost::shared_ptr<UUID> UUIDPtr;

RSC_EXPORT std::ostream& operator<<(std::ostream& stream, const UUID& id);

}
}

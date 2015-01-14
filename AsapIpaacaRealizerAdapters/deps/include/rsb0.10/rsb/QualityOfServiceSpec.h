/* ============================================================
 *
 * This file is a part of RSB project
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

#include <ostream>

#include <boost/operators.hpp>

#include "rsb/rsbexports.h"

namespace rsb {

/**
 * Specification of desired quality of service settings for sending and
 * receiving events. Specification given here are required "at least". This
 * means concrete port instances can implement "better" QoS specs without any
 * notification to the clients. Better is decided by the integer value of the
 * specification enums. Higher values mean better services.
 *
 * @author jwienke
 */
class RSB_EXPORT QualityOfServiceSpec: boost::equality_comparable<
        QualityOfServiceSpec> {
    friend class ParticipantConfig;
public:

    /**
     * Specifies the ordering of events for listeners on a informer.
     *
     * @author jwienke
     */
    enum Ordering {
        /**
         * The events are delivered in arbitrary order.
         */
        UNORDERED = 10,
        /**
         * Every listener receives the events of one informer in the order
         * the informer sent the events. No guarantees are given for events of
         * multiple informers.
         */
        ORDERED = 20
    };

    /**
     * Specifies the reliability of messages.
     *
     * @author jwienke
     */
    enum Reliability {
        /**
         * Events may be dropped and not be visible to a listener.
         */
        UNRELIABLE = 10,
        /**
         * Messages are guaranteed to be delivered. Otherwise an error is
         * raised.
         */
        RELIABLE = 20
    };

    /**
     * Constructs the default QoS specs for every informers. Messages are
     * unordered but reliably.
     */
    QualityOfServiceSpec();

    /**
     * Constructs a new QoS specification with desired details.
     *
     * @param ordering desired ordering type
     * @param reliability desired reliability type
     */
    QualityOfServiceSpec(Ordering ordering, Reliability reliability);

    /**
     * Destructor.
     */
    virtual ~QualityOfServiceSpec();

    /**
     * Returns the desired ordering settings.
     *
     * @return ordering requirements
     */
    Ordering getOrdering() const;

    /**
     * Returns the desired reliability settings.
     *
     * @return reliability requirements
     */
    Reliability getReliability() const;

    friend RSB_EXPORT std::ostream& operator<<(std::ostream& stream,
            const QualityOfServiceSpec& spec);

    bool operator==(const QualityOfServiceSpec& other) const;

private:

    Ordering ordering;
    Reliability reliability;

};

RSB_EXPORT std::ostream& operator<<(std::ostream& stream,
        const QualityOfServiceSpec& spec);

}

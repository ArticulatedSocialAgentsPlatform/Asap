/* ============================================================
 *
 * This file is part of the RSB project
 *
 * Copyright (C) 2011 Jan Moringen <jmoringe@techfak.uni-bielefeld.de>
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

#include <vector>

#include <boost/shared_ptr.hpp>

#include <rsc/logging/Logger.h>

#include "Scope.h"
#include "Event.h"
#include "Participant.h"
#include "eventprocessing/InRouteConfigurator.h"
#include "transport/InPullConnector.h"

namespace rsb {

/**
 * A Reader receives events published by a informers by participating
 * in a channel with a suitable scope. In contrast to the @ref
 * Listener participant, a Reader receives events using a pull-style
 * interface: the read method has to be explicitly in order to receive
 * events. This method may then block (depending on the @a block
 * parameter) until an event becomes available. The received event, if
 * any, is returned to the caller. There are no context swichtes or
 * queuing involved.
 *
 * Usage example:
 * @code
 * ReaderPtr reader = getFactory().createReader(Scope("/example/informer"));
 * reader->read();
 * @endcode
 *
 * @author jmoringe
 */
class RSB_EXPORT Reader: public Participant {
public:
    /**
     * Constructs a new reader object assigned to @a scope. The reader
     * connects to the bus using the supplied @a connectors.
     *
     * @param connectors a list of connectors that the reader should
     *                   use to communicate with the bus.
     * @param scope the scope where the data is received from.
     * @param config the configuration that was used to setup this listener
     *
     * @note This constructor is exposed for unit tests and such. Use
     * @ref Factory::createReader instead of calling this directly.
     */
    Reader(const std::vector<transport::InPullConnectorPtr>& connectors,
           const Scope&                                  scope,
           const ParticipantConfig&                      config);

    /**
     * Reads one event from the bus. If @a block is true, blocks until
     * an event is available. If @a block is false and no event is
     * available, an empty @ref EventPtr is returned.
     *
     * @param block Controls whether the call should block and wait
     * for an event if no event is immediately available.
     * @return A @ref boost::shared_ptr to the received event or an
     * empty @ref boost::shared_ptr if no event was available and @a
     * block was false.
     * @throw RSBError when a communication error occurs while
     * receiving or decoding the event.
     */
    EventPtr read(bool block = true);
private:
    rsc::logging::LoggerPtr logger;

    eventprocessing::InRouteConfiguratorPtr configurator;

    std::string getClassName() const;
};

typedef boost::shared_ptr<Reader> ReaderPtr;

}

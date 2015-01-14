/* ============================================================
 *
 * This file is part of the RSB project.
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

#include "../../Event.h"
#include "../../protocol/Notification.h"

namespace rsb {
namespace transport {
namespace socket {
/**
 * Converts @a notification into an @ref Event. The event payload will
 * be copied from @a notification into the event unmodified to allow
 * application of arbitrary converters.
 *
 * @param notification The @ref protocol::Notification from which the
 *                     event should be constructed.
 * @param exposeWireSchema Controls whether the wire-schema stored in
 *                         @a notification should be exposed in a meta
 *                         data item of the created event.
 * @return A shared pointer to a newly allocated @ref rsb::Event.
 */
EventPtr notificationToEvent(protocol::Notification& notification,
                             bool                    exposeWireSchema = false);
/**
 * Converts the @ref Event @a event into a @ref
 * protocol::Notification, storing the result in @a notification.
 *
 * @param notification The @ref protocol::Notification object into
 *                     which the conversion should be performed.
 * @param event event The @ref Event object that should be serialized.
 * @param wireSchema The wire-Schema that should be stored in @a
 *                   notification.
 * @param data The payload that should be stored in @a notification.
 */
void eventToNotification(protocol::Notification& notification,
                         const EventPtr&         event,
                         const std::string&      wireSchema,
                         const std::string&      data);

}
}
}

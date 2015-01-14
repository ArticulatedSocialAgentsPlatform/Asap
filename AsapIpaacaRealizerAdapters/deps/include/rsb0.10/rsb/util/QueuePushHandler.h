/* ============================================================
 *
 * This file is a part of RSB project
 *
 * Copyright (C) 2010-2013 by Johannes Wienke <jwienke at techfak dot uni-bielefeld dot de>
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

#include <boost/shared_ptr.hpp>

#include <rsc/threading/SynchronizedQueue.h>

#include "../Handler.h"

namespace rsb {
namespace util {

/**
 * A @ref rsb::Handler for @ref rsb::Listener s that pushes all
 * received data on a rsc::SynchronizedQueue. This queue must
 * handle shared pointers of the data type.
 *
 * @author jwienke
 * @tparam T data type received by the handler. All data is handeled as a shared
 *           pointer of this type
 */
template<class T>
class QueuePushHandler: public Handler {
private:
    boost::shared_ptr<rsc::threading::SynchronizedQueue<boost::shared_ptr<T> > >
            queue;
public:

    /**
     * Constructs a new instance.
     *
     * @param queue the queue to push received data on
     * @param method method of this handler to react on, empty means all events
     */
    QueuePushHandler(
            boost::shared_ptr<rsc::threading::SynchronizedQueue<
                    boost::shared_ptr<T> > > queue,
            const std::string& method = "") :
        Handler(method), queue(queue) {
    }

    std::string getClassName() const {
        return "QueuePushHandler";
    }

    void printContents(std::ostream& stream) const {
        stream << "queue = " << queue;
    }

    void handle(EventPtr event) {
        queue->push(boost::static_pointer_cast<T>(event->getData()));
    }
};

}
}

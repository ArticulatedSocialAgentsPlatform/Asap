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

#include <set>

#include <boost/shared_ptr.hpp>
#include <boost/function.hpp>

#include <rsc/runtime/TypeStringTools.h>

#include "Event.h"

#include "eventprocessing/Handler.h"

#include "filter/Filter.h"

#include "rsb/rsbexports.h"

namespace rsb {

/**
 * Asynchronously called handler interface on the client level. Handler may have
 * associated methods and are only called for events that match their desired
 * method.
 *
 * @author jwienke
 */
class RSB_EXPORT Handler: public eventprocessing::Handler {
public:

    /**
     * Returns the desired set of methods this handler can handle. An empty set
     * means no restrictions on methods are placed.
     *
     * @return set of desired methods or empty for all methods
     */
    std::set<std::string> getMethods() const;
    bool acceptsMethod(const std::string& method) const;

protected:

    /**
     * Creates a new handler that only accepts events for the specified method.
     *
     * @param method the accepted method of this handler or empty string for
     *               all methods
     */
    explicit                                  Handler(const std::string& method = "");

    /**
     * Creates a new handler that only accepts events with the specified
     * methods.
     *
     * @param methods set of desired methods, empty for all methods
     */
    explicit                                  Handler(const std::set<std::string>& methods);

    virtual ~Handler();

    std::string getClassName() const {
        return rsc::runtime::typeName(typeid(*this));
    }
private:
    std::set<std::string>                     methods;
};

typedef boost::shared_ptr<Handler>            HandlerPtr;

typedef boost::function<void(EventPtr)> EventFunction;

/**
 * A utility class to simplify event handling in ordinary functions or
 * member functions. This class is primarily intended for use with
 * Boost.Bind.
 */
class RSB_EXPORT EventFunctionHandler: public Handler {
public:
    EventFunctionHandler(const EventFunction& function,
                         const std::string&   method      = "");

    std::string getClassName() const;
    void printContents(std::ostream& stream) const;

    void                                      handle(EventPtr event);
protected:
    EventFunction                             function;
};

/**
 * A utility class to simplify data handling by automatically passing
 * the data of the desired type to a function. This class is primarily
 * intended for use with Boost.Bind.
 *
 * @tparam T desired data type to retrieve from the listeners
 *
 * @author swrede
 */
template<typename T>
class DataFunctionHandler: public Handler {
public:
    typedef boost::shared_ptr<T>              DataPtr;
    typedef boost::function<void(DataPtr)> DataFunction;

    std::string getClassName() const {
        return "DataFunctionHandler";
    }

    void printContents(std::ostream& stream) const {
        stream << "DataType = " << rsc::runtime::typeName<T>()
               << ", function = " << function;
    }

    DataFunctionHandler(const DataFunction& function) :
        function(function) {
    }

    void handle(EventPtr event) {
        this->function(boost::static_pointer_cast<T>(event->getData()));
    }
protected:
    DataFunction                              function;
};

/**
 * A Handler that automatically performs the desired type casting.
 *
 * @author jwienke
 * @tparam DataType type of the data received by this handler, omitting the
 *                  shared pointer
 */
template<class DataType>
class DataHandler: public Handler {
public:
    virtual ~DataHandler() {
    }

    void handle(EventPtr event) {
        notify(boost::static_pointer_cast<DataType>(event->getData()));
    }

    virtual void notify(boost::shared_ptr<DataType> data) = 0;

};

/**
 * A utility class that forwards events to another @ref rsb::Handler
 * object if they match a given @ref rsb::filter::Filter.
 *
 * @author jmoringe
 */
class RSB_EXPORT FilteringHandler : public Handler {
public:
    FilteringHandler(rsb::filter::FilterPtr  filter,
                     HandlerPtr next)
        : filter(filter), next(next) {
    }

    void handle(EventPtr event) {
        if (this->filter->match(event)) {
            this->next->handle(event);
        }
    }
protected:
    rsb::filter::FilterPtr filter;
    HandlerPtr             next;
};

}

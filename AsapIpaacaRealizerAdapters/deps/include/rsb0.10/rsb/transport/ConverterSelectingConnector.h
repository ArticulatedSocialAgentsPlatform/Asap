/* ============================================================
 *
 * This file is part of the RSB project.
 *
 * Copyright (C) 2011, 2012 Jan Moringen <jmoringe@techfak.uni-bielefeld.de>
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

#include <rsc/logging/Logger.h>

#include "../converter/Converter.h"
#include "../converter/ConverterSelectionStrategy.h"

namespace rsb {
namespace transport {

/**
 * This base class enables look up of suitable
 * @ref rsb::converter::Converter s in
 * @ref rsb::transport::Connector classes.
 *
 * @author jmoringe
 */
template <typename WireType>
class ConverterSelectingConnector {
public:
    typedef typename converter::Converter<WireType>::Ptr ConverterPtr;
    typedef typename converter::ConverterSelectionStrategy<WireType>::Ptr ConverterSelectionStrategyPtr;
protected:

    ConverterSelectingConnector(ConverterSelectionStrategyPtr converters) :
        logger(rsc::logging::Logger::getLogger("rsb.transport.ConverterSelectingConnector")),
        converters(converters) {
    }

    /**
     * Try to find a suitable converter for @a key . It is considered
     * a program error if no such converter can be found. The error
     * condition can be avoided by:
     * -# registering converters for all occuring wire-schemas or data-types
     * -# registering a dummy converter that accepts but discard anything.
     *
     * @param key the wire-schema or data-type of the converter being
     *            requested.
     * @return The requested converter.
     * @throw rsc::runtime::NoSuchObject If no converter could be
     * found for @a key.
     */
    ConverterPtr getConverter(const std::string& key) const {
        return this->converters->getConverter(key);
    }
private:
    rsc::logging::LoggerPtr logger;

    ConverterSelectionStrategyPtr converters;
};

}
}

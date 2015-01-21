/* ============================================================
 *
 * This file is part of the RSC project
 *
 * Copyright (C) 2010, 2011 Jan Moringen
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

#include <stdexcept>

#include <string>

#include "InvalidMangledName.h"
#include "rsc/rscexports.h"

namespace rsc {
namespace runtime {

/**
 * This function takes the mangled name of a symbol and returns the demangled
 * name of the symbol.
 *
 * @param mangled_symbol The mangled name of the symbol.
 * @return The demangled name of the symbol.
 * @throw runtime_error
 * @throw InvalidMangledName
 *
 * @author Jan Moringen <jmoringe@techfak.uni-bielefeld.de>
 */
RSC_EXPORT std::string demangle(const char* mangledSymbol);

/**
 * This function takes the mangled name of a symbol and returns the demangled
 * name of the symbol.
 *
 * @param mangled_symbol The mangled name of the symbol.
 * @return The demangled name of the symbol.
 * @throw runtime_error
 * @throw InvalidMangledName
 *
 * @author Jan Moringen <jmoringe@techfak.uni-bielefeld.de>
 */
RSC_EXPORT std::string demangle(const std::string& mangledSymbol);

}
}

/* ============================================================
 *
 * This file is a part of RSC project
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

#include <map>
#include <string>

#if defined (_WIN32) 
    #if defined(rsc0_10_EXPORTS)
        #define RSC_EXPORT __declspec(dllexport)
        #define RSC_EXPIMP
    #else
        #define RSC_EXPORT __declspec(dllimport)
        #define RSC_EXPIMP extern
    #endif
#else
    #define RSC_EXPORT
#endif

// stl exports...
//#ifdef _WIN32
//RSC_EXPIMP template class RSC_EXPORT std::multimap<std::string, std::string>;
//#endif


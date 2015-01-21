/* ============================================================
 *
 * This file is a part of the RSB project.
 *
 * Copyright (C) 2011 by Johannes Wienke <jwienke at techfak dot uni-bielefeld dot de>
 *
 * Written by Ulrich Drepper <drepper@gnu.ai.mit.edu> and heavily modified
 * for GnuPG by <werner.koch@guug.de> and adapted for the need of FPM Blowfish
 * Plugin
 *
 * Latest author:
 * Frederic RUAUDEL <grumz@users.sf.net>
 * FPMBlowfishPlugin
 * Copyleft (c) 2003 Frederic RUAUDEL, all rights reversed
 * Copyleft (C) 1995, 1996, 1998, 1999 Free Software Foundation, Inc.
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

#include <iostream>
#include <string>

#include <boost/shared_ptr.hpp>
#include <boost/shared_array.hpp>

#include "rsb/rsbexports.h"

namespace rsb {
namespace util {

class MD5Hasher;

/**
 * A simple class representing an md5 sum for a given string.
 *
 * @author jwienke
 */
class RSB_EXPORT MD5 {
public:

    /**
     * Creates a new m5s sum for the given string.
     *
     * @param s string to create sum for
     */
    MD5(const std::string& s);

    virtual ~MD5();

    /**
     * Returns a hex encoded string of the sum.
     *
     * @param pretty if @c true, the string will be separated in blocks by
     *               spaces
     */
    std::string toHexString(const bool& pretty = false) const;

private:

    boost::shared_ptr<MD5Hasher> hasher;
    boost::shared_array<unsigned char> hash;

};

RSB_EXPORT std::ostream& operator<<(std::ostream& stream, const MD5& sum);

}
}


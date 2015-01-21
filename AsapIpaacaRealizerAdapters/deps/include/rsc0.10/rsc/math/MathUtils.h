/* ============================================================
 *
 * This file is a part of the RSC project.
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

#include "rsc/rscexports.h"

namespace rsc {
namespace math {

/**
 * General math-related utility functions.
 *
 * @author jwienke
 */
class RSC_EXPORT MathUtils {
public:

    /**
     * Default precision used for operations with angles in rad.
     */
    static double getDefaultAnglePrecision();

    /**
     * Normalizes the given angle in rad to interval \f$[0,2\pi[\f$.
     *
     * @param angleInRad arbitrary angle in rad
     * @return normalization of the angle to interval \f$[0,2\pi[\f$
     */
    static double normalizeAngle(const double& angleInRad);

    /**
     * Tests if two given angles in rad are the same angle regarding things like
     * \f$0 = n\cdot2\cdot\pi\f$ etc.
     *
     * @param firstInRad first angle to test in rad
     * @param secondInRad second angle to test in rad
     * @param precision precision for the equality check in rad
     * @return <code>true</code> if the difference of both angles is less than
     *         <code>precision</code>, else <code>false</code>
     */
    static bool isSameAngle(const double& firstInRad,
            const double& secondInRad, const double& precision =
                    getDefaultAnglePrecision());

    /**
     * Converts an angle given in degrees to radian.
     *
     * @param degree angle in degree
     * @return angle in rad
     */
    static double radianFromDegree(const double& degree);

    /**
     * Converts an angle given in radian to degrees.
     *
     * @param radian angle in rad
     * @return angle in degree
     */
    static double degreeFromRadian(const double& radian);

    /**
     * Returns the default precision used for #isClose.
     *
     * @return default precision
     */
    static double getDefaultClosePrecision();

    /**
     * Checks two floating point numbers for equality using a specified
     * tolerance.
     *
     * @param a first number to compare
     * @param b second number to compare
     * @param precision precision to use for the comparison, default is
     *                  #getDefaultClosePrecision
     * @return @c true if \f$|a-b| < \textrm{precision}\f$
     */
    static bool isClose(const double& a, const double& b,
            const double& precision = getDefaultClosePrecision());

private:
    static double getTwoPi();

};

}
}


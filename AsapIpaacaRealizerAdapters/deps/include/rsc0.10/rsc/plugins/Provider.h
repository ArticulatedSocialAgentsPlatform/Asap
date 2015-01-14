/* ============================================================
 *
 * This file is part of the RSB project.
 *
 * Copyright (C) 2012 Jan Moringen <jmoringe@techfak.uni-bielefeld.de>
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
 * ============================================================  */

#pragma once

/**
 * @name Low-level plugin symbols
 *
 * Detailed symbols to use for providing plugins by implementing required
 * methods. Instead of using these symbols directly it is usually easier to
 * use @ref RSC_PLUGIN_INIT_SIGNATURE and @ref RSC_PLUGIN_SHUTDOWN_SIGNATURE.
 */
//@{

/**
 * The method name to expose in plugins for initializing a plugin.
 */
#define RSC_PLUGIN_INIT_SYMBOL     rsc_plugin_init

/**
 * The method name to expose in plugins for shutting down a plugin.
 */
#define RSC_PLUGIN_SHUTDOWN_SYMBOL rsc_plugin_shutdown

#if defined (_WIN32)
    /**
     * Export symbol to put in front of plugin init and shutdown method to
     * expose them outside of a plugin's library.
     */
    #define RSC_PLUGIN_EXPORT __declspec(dllexport)
#else
    /**
     * Export symbol to put in front of plugin init and shutdown method to
     * expose them outside of a plugin's library.
     */
    #define RSC_PLUGIN_EXPORT
#endif

//@}

/**
 * @name hogh-level plugin initialization
 *
 * To provide a plugin implement the following stub:
 *
 * RSC_PLUGIN_INIT_SIGNATURE() {}
 * RSC_PLUGIN_SHUTDOWN_SIGNATURE() {}
 */
//@{
#if defined (_WIN32)
    #define RSC_PLUGIN_INIT_SIGNATURE __declspec(dllexport) void rsc_plugin_init
    #define RSC_PLUGIN_SHUTDOWN_SIGNATURE __declspec(dllexport) void rsc_plugin_shutdown
#else
    #define RSC_PLUGIN_INIT_SIGNATURE void rsc_plugin_init
    #define RSC_PLUGIN_SHUTDOWN_SIGNATURE void rsc_plugin_shutdown
#endif
//@}

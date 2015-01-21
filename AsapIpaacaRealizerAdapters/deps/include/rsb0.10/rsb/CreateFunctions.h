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

#include "rsb/rsbexports.h"

#include "Scope.h"
#include "ParticipantConfig.h"
#include "Factory.h"

namespace rsb {

/**
 * Creates and returns a new @ref Informer that publishes @ref Event s
 * under the @ref Scope @a scope.
 *
 * @tparam DataType the C++ data type this informer publishes
 * @param scope The scope of the informer.
 * @param config The configuration for the informer to use. Defaults
 *               to the global default configuration stored in the
 *               @ref Factory instance.
 * @param dataType A string representation of the type of data sent
 *                 via the new @ref Informer.
 * @return A shared_ptr pointing to the new @ref Informer instance.
 * @throw RSBError If the requested informer cannot be created.
 */
template<class DataType>
typename Informer<DataType>::Ptr
createInformer(const Scope& scope,
               const ParticipantConfig& config
               = getFactory().getDefaultParticipantConfig(),
               const std::string& dataType
               = detail::TypeName<DataType>()()) {
    return getFactory().createInformer<DataType>(scope, config, dataType);
}

/**
 * Creates and returns a new @ref Informer that publishes @ref Event s
 * under the @ref Scope @a scope.
 *
 * @param scope The scope of the informer.
 * @param dataType A string representation of the type of data sent
 *                 via the new @ref Informer.
 * @param config The configuration for the informer to use. Defaults
 *               to the global default configuration stored in the
 *               @ref Factory instance.
 * @return A shared_ptr pointing to the new @ref Informer instance.
 * @throw RSBError If the requested informer cannot be created.
 */
RSB_EXPORT InformerBasePtr crateInformerBase(const Scope&             scope,
                                             const std::string&       dataType,
                                             const ParticipantConfig& config
                                             = getFactory().getDefaultParticipantConfig());

/**
 * Creates and returns a new @ref Listener for the @ref Scope @a
 * scope.
 *
 * @param scope The scope of the new listener
 * @param config The configuration for the listener to use. Defaults
 *               to the global default configuration stored in the
 *               @ref Factory instance.
 * @return A shared_ptr pointing to the new @ref Listener instance.
 * @throw RSBError If the requested listener cannot be created.
 */
RSB_EXPORT ListenerPtr createListener(const Scope& scope,
                                      const ParticipantConfig& config
                                      = getFactory().getDefaultParticipantConfig());

/**
 * Creates and returns a new @ref Reader object for @ref Scope @a
 * scope.
 *
 * @ref Reader objects receive event via a pull-style interface
 * by calls to @ref Reader::read.
 *
 * @param scope the scope of the new receiver
 * @param config The configuration for the reader to use. Defaults to
 *               the global default configuration stored in the @ref
 *               Factory instance.
 * @return A shared_ptr to the new @ref Reader object.
 * @throw RSBError If the requested reader cannot be created.
 **/
RSB_EXPORT ReaderPtr createReader(const Scope& scope,
                                  const ParticipantConfig& config
                                  = getFactory().getDefaultParticipantConfig());

/**
 * Creates and returns a Service instance operating on @ref Scope @a
 * scope.
 *
 * @param scope Parent-scope of the new service.
 * @return pointer to the new @ref Service instance.
 */
RSB_EXPORT ServicePtr createService(const Scope& scope);

/**
 * Creates and returns a @ref Server object that exposes methods under
 * the @ref Scope @a scope.
 *
 * @param scope The scope under which the new server exposes its
 *              methods.
 * @return A shared_ptr to the new @ref Server object.
 */
RSB_EXPORT patterns::ServerPtr createServer(const Scope& scope);

/**
 * Creates and returns a @ref RemoteServer object for the server at
 * @ref Scope @a scope.
 *
 * @param scope The scope at which the remote server object exposes
 *              itself.
 * @return A shared_ptr to the new @ref RemoteServer object.
 */
RSB_EXPORT patterns::RemoteServerPtr createRemoteServer(const Scope& scope);

}

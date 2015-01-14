/* ============================================================
 *
 * This file is a part of the RSB project.
 *
 * Copyright (C) 2011 by Johannes Wienke <jwienke at techfak dot uni-bielefeld dot de>
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
#include <vector>

#include <boost/shared_ptr.hpp>
#include <boost/thread/recursive_mutex.hpp>

#include <rsc/logging/Logger.h>
#include <rsc/runtime/TypeStringTools.h>
#include <rsc/patterns/Singleton.h>
#include <rsc/config/OptionHandler.h>
#include <rsc/plugins/Manager.h>

#include "rsb/rsbexports.h"

#include "ParticipantConfig.h"
#include "Reader.h"
#include "Listener.h"
#include "Informer.h"
#include "Service.h"

#include "transport/Connector.h"
#include "transport/Factory.h"

#include "patterns/Server.h"
#include "patterns/RemoteServer.h"

namespace rsb {

class Factory;

/**
 * Returns a factory for client-level RSB objects.
 *
 * @return the factory instance to create client level RSB objects.
 */
RSB_EXPORT Factory& getFactory();

/**
 * Factory for RSB user-level domain objects for communication patterns.
 *
 * @author jwienke
 */
class RSB_EXPORT Factory: private rsc::patterns::Singleton<Factory> {
public:

    /**
     * @deprecated Singletons will be removed from RSB (see bug 1245). Please
     *             use #getFactory instead.
     * @todo Remove this after the 0.8 release.
     */
    DEPRECATED(static Factory& getInstance());

    virtual ~Factory();

    /**
     * Creates and returns a new @ref Informer that publishes @ref
     * Event s under the @ref Scope @a scope.
     *
     * @tparam DataType the C++ data type this informer publishes
     * @param scope the scope of the informer
     * @param config the configuration for the informer to use, defaults to the
     *               general config held in this factory.
     * @param dataType A string representation of the type of data
     *                 sent via the new @ref Informer
     * @return a shared_ptr pointing to the new @ref Informer
     *         instance.
     * @throw RSBError If the requested informer cannot be created.
     */
    template<class DataType>
    typename Informer<DataType>::Ptr
    createInformer(const Scope& scope,
                   const ParticipantConfig& config
                   = getFactory().getDefaultParticipantConfig(),
                   const std::string& dataType
                   = detail::TypeName<DataType>()()) {
        return typename Informer<DataType>::Ptr(new Informer<DataType> (
            createOutConnectors(config), scope, config, dataType));
    }

    /**
     * Creates and returns a new @ref Informer that publishes @ref
     * Event s under the @ref Scope @a scope.
     *
     * @param scope the scope of the informer
     * @param dataType A string representation of the type of data
     *                 sent via the new @ref Informer.
     * @param config The configuration for the informer to use, defaults to the
     *               general config held in this factory.
     * @return A shared_ptr pointing to the a @ref InformerBase
     *         instance.
     * @throw RSBError If the requested informer cannot be created.
     */
    InformerBasePtr createInformerBase(const Scope&             scope,
                                       const std::string&       dataType
                                       = "",
                                       const ParticipantConfig& config
                                       = getFactory().getDefaultParticipantConfig());

    /**
     * Creates a new listener for the specified scope.
     *
     * @param scope the scope of the new listener
     * @param config the configuration for the LISTENER to use, defaults to the
     *               general config held in this factory.f
     * @return new listener instance
     */
    ListenerPtr createListener(const Scope& scope,
            const ParticipantConfig& config =
                    getFactory().getDefaultParticipantConfig());

    /**
     * Creates a new @ref Reader object for the specified scope.
     *
     * @ref Reader objects receive event via a pull-style interface
     * by calls to @ref Reader::read.
     *
     * @param scope the scope of the new receiver
     * @throw RSBError when the requested connection cannot be
     * established.
     * @return A shared_ptr to the new @ref Reader object.
     **/
    ReaderPtr createReader(const Scope& scope,
                           const ParticipantConfig& config =
                           getFactory().getDefaultParticipantConfig());

    /**
     * Creates a Service instance operating on the given scope.
     *
     * @param scope parent-scope of the new service
     * @return new service instance
     */
    ServicePtr createService(const Scope& scope);

    /**
     * Creates a @ref Server object that exposes methods under the
     * scope @a scope.
     *
     * @param scope The scope under which the new server exposes its
     * methods.
     * @param listenerConfig configuration to use for all request listeners
     * @param informerConfig configuration to use for all reply informers
     * @return A shared_ptr to the new @ref Server object.
     */
    patterns::ServerPtr createServer(
            const Scope& scope,
            const ParticipantConfig &listenerConfig =
                    getFactory().getDefaultParticipantConfig(),
            const ParticipantConfig &informerConfig =
                    getFactory().getDefaultParticipantConfig());

    /**
     * Creates a @ref RemoteServer object for the server at scope @a
     * scope.
     *
     * @param scope The scope at which the remote server object
     * exposes itself.
     * @param listenerConfig configuration to use for all reply listeners
     * @param informerConfig configuration to use for all request informers
     * @return A shared_ptr to the new @ref RemoteServer object.
     */
    patterns::RemoteServerPtr createRemoteServer(
            const Scope& scope,
            const ParticipantConfig &listenerConfig =
                    getFactory().getDefaultParticipantConfig(),
            const ParticipantConfig &informerConfig =
                    getFactory().getDefaultParticipantConfig());

    /**
     * Returns the default configuration for new participants.
     *
     * @return copy of the default configuration
     */
    ParticipantConfig getDefaultParticipantConfig() const;

    /**
     * Sets the default config for participants that will be used for every new
     * participant that is created after this call.
     *
     * @param config new config
     */
    void setDefaultParticipantConfig(const ParticipantConfig& config);

    /**
     * Returns the plugin manager instance used by the RSB core.
     *
     * @return plugin manager instance
     */
    rsc::plugins::ManagerPtr getPluginManager() const;

    friend class rsc::patterns::Singleton<Factory>;

private:
    /**
     * Singleton constructor.
     */
    Factory();

    static Factory& getInstanceBase();
    friend Factory& getFactory();

    rsc::logging::LoggerPtr logger;

    rsc::plugins::ManagerPtr pluginManager;

    /**
     * Always acquire configMutex before reading or writing the config.
     */
    ParticipantConfig defaultConfig;
    mutable boost::recursive_mutex configMutex;

    std::vector<transport::OutConnectorPtr>
        createOutConnectors(const ParticipantConfig& config);

    std::vector<transport::InPullConnectorPtr>
        createInPullConnectors(const ParticipantConfig& config);

    std::vector<transport::InPushConnectorPtr>
        createInPushConnectors(const ParticipantConfig& config);

    void configureSubsystem(rsc::config::OptionHandler& handler,
                            const std::string& environmentVariablePrefix = "RSB_");

};

}

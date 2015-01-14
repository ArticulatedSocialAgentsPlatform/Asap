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

#include <string>
#include <vector>
#include <map>

#include <rsc/logging/Logger.h>
#include <rsc/logging/LoggerFactory.h>
#include <rsc/misc/langutils.h>
#include <rsc/runtime/NoSuchObject.h>
#include <rsc/runtime/TypeStringTools.h>
#include <rsc/runtime/Printable.h>
#include <rsc/patterns/Factory.h>

#include <boost/type_traits.hpp>

#include "InPullConnector.h"
#include "InPushConnector.h"
#include "OutConnector.h"
#include "rsb/rsbexports.h"

namespace rsb {
namespace transport {

template <typename Interface>
class ConnectorFactory;

typedef ConnectorFactory<InPullConnector> InPullFactory;

typedef ConnectorFactory<InPushConnector> InPushFactory;

typedef ConnectorFactory<OutConnector> OutFactory;

RSB_EXPORT InPullFactory& getInPullFactory();
RSB_EXPORT InPushFactory& getInPushFactory();
RSB_EXPORT OutFactory& getOutFactory();

/**
 * Objects of this class are specialized factories that construct @ref
 * Connector objects and provide introspection for connector
 * implementations without instantiating them.
 *
 * @author jmoringe
 */
template <typename Interface>
class ConnectorFactory: public rsc::patterns::Factory<std::string, Interface>,
                        private rsc::patterns::Singleton< ConnectorFactory<Interface> >,
                        public rsc::runtime::Printable {
public:

    /**
     * @deprecated Singletons will be removed from RSB (see bug 1245). Please
     *             use one of the get*Factory functions in the rsb::transport
     *             namespace instead.
     * @todo Remove this after the 0.8 release.
     */
    DEPRECATED(static ConnectorFactory<Interface>& getInstance());

    /**
     * Instances of this class describe capabilities and properties of
     * connector implementations.
     *
     * @author jmoringe
     */
    class ConnectorInfo: public rsc::runtime::Printable {
    public:
        typedef std::set<std::string> SchemaList;
        typedef std::set<std::string> OptionList;

        ConnectorInfo(const std::string& name,
                      const SchemaList& schemas,
                      bool remote,
                      const OptionList& options) :
            name(name), schemas(schemas), remote(remote), options(options) {
            this->options.insert("enabled");
        }

        /**
         * Return the name of the implementation.
         */
        std::string getName() const {
            return this->name;
        }

        /**
         * Return the set of schemas supported by the connector
         * implementation.
         *
         * @return A @ref std::set containing the supported schemas as
         * strings.
         */
        SchemaList getSchemas() const {
            return this->schemas;
        }

        /**
         * Return a list of option names describing configurations
         * options recognized by the implementation.
         *
         * @return A @ref std::set containing the names of recognized
         * options.
         */
        OptionList getOptions() const {
            return this->options;
        }

        /**
         * Return "remoteness" of the implementation.
         *
         * @return @c true if the transport allows communication
         *         across process boundaries, @c false otherwise.
         */
        bool isRemote() const {
            return this->remote;
        }

        bool operator<(const ConnectorInfo& other) const {
            if (this->name < other.name) {
                return true;
            } else if (this->name == other.name) {
                if (this->schemas < other.schemas) {
                    return true;
                } else if (this->schemas == other.schemas) {
                    if (this->remote < other.remote) {
                        return true;
                    } else if (this->remote == other.remote) {
                        return this->options < other.options;
                    }
                }
            }
            return false;
        }
    private:
        std::string name;
        SchemaList schemas;
        bool remote;
        OptionList options;

        void printContents(std::ostream& stream) const {
            stream << this->name
                   << ", schemas = " << this->schemas
                   << ", remote = " << this->remote
                   << ", options = " << this->options;
        }
    };

private:
    rsc::logging::LoggerPtr logger;

    static ConnectorFactory<Interface>& getInstanceBase() {
        return rsc::patterns::Singleton< ConnectorFactory<Interface> >::getInstance();
    }
    friend InPullFactory& getInPullFactory();
    friend InPushFactory& getInPushFactory();
    friend OutFactory& getOutFactory();

    typedef rsc::patterns::Factory<std::string, Interface> Factory;
    typedef typename Factory::CreateFunction CreateFunction;
    typedef typename Factory::ImplMapProxy ImplMapProxy;
    typedef std::map<std::string, ConnectorInfo> InfoMap; // forward
public:
    ConnectorFactory() :
        logger(rsc::logging::Logger::getLogger("rsb.transport.ConnectorFactory<" + rsc::runtime::typeName<Interface>() + ">")) {
    }

    /** Return information regarding the connector implementation
     * named @a name.
     *
     * @param name Name of the implementation for which information
     * should be returned.
     * @return A @ref ConnectorInfo object.
     * @throw rsc::runtime::NoSuchObject If a record for @a name
     * cannot be found.
     */
    ConnectorInfo getConnectorInfo(const std::string& name) const {
        typename InfoMap::const_iterator it = this->infos.find(name);
        if (it == this->infos.end()) {
            throw rsc::runtime::NoSuchObject(name);
        }
        return it->second;
    }

    std::set<ConnectorInfo> getConnectorInfos() const {
        std::set<ConnectorInfo> result;

        for (typename InfoMap::const_iterator it = this->infos.begin();
             it != this->infos.end(); ++it) {
            result.insert(it->second);
        }
        return result;
    }

    /**
     * For the connector implementation named @a name, register the
     * construct function @a constructor, supported schemas @a schemas
     * and recognized configuration options @a options.
     *
     * @param name Name of the connector implementation.
     * @param constructor Construct function.
     * @param schemas A list of strings designating schemas supported
     * by the implementation.
     * @param options A list of strings describing configuration
     * options recognized by the implementation.
     */
    void registerConnector(const std::string& name,
            const CreateFunction& constructor,
            const std::set<std::string>& schemas = std::set<std::string>(),
            bool remote = true,
            const std::set<std::string>& options = std::set<std::string>()) {
        RSCINFO(this->logger, "Registering connector "
                << name
                << " for schemas " << schemas);

        Factory::impls().register_(name, constructor);

        ConnectorInfo info(name, schemas, remote, options);
        this->infos.insert(std::make_pair(name, info));
    }

    void registerConnector(const std::string& name,
            const CreateFunction& constructor, const std::string& schema,
            bool remote = true,
            const std::set<std::string>& options = std::set<std::string>()) {
        std::set<std::string> schemas;
        schemas.insert(schema);
        registerConnector(name, constructor, schemas, remote, options);
    }
private:
    InfoMap infos;

    void printContents(std::ostream& stream) const {
        const ImplMapProxy& implementations = Factory::impls();
        stream << std::endl;
        for (typename ImplMapProxy::const_iterator it = implementations.begin(); it
                 != implementations.end(); ++it) {
            stream << "\t" << getConnectorInfo(it->first) << std::endl;
        }
    }
};

template <typename Interface>
ConnectorFactory<Interface>& ConnectorFactory<Interface>::getInstance() {

    // This weird implementation is a tribute to backwards compatibility. We
    // previously had a generic template class but now need to map it to
    // specific getter implementations depending on the type. However, there
    // is no chance in C++ to provide template specializations based on a return
    // type of a method. Therefore, we do the distinction at runtime using.
    // As all paths of the if-else expression are always possible from the
    // compiler's point of view, we need to convince it for all paths that for
    // any template parameter the correct type is returned by doing a harsh
    // cast. This is unfortunately a bit complicated from a syntactical point of
    // view to also achieve the correct reference behavior.

    if (boost::is_same<Interface, InPullConnector>::value) {
        return (*(ConnectorFactory<Interface>*) &getInPullFactory());
    } else if (boost::is_same<Interface, InPushConnector>::value) {
        return (*(ConnectorFactory<Interface>*) &getInPushFactory());
    } else if (boost::is_same<Interface, OutConnector>::value) {
        return (*(ConnectorFactory<Interface>*) &getOutFactory());
    } else {
        assert(false);
        return (*(ConnectorFactory<Interface>*) 0);
    }
}

}
}

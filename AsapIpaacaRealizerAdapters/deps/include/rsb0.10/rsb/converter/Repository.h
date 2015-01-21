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
#include <stdexcept>
#include <set>
#include <iomanip>

#include <boost/format.hpp>

#include <rsc/runtime/Printable.h>
#include <rsc/runtime/TypeStringTools.h>
#include <rsc/runtime/NoSuchObject.h>
#include <rsc/logging/Logger.h>

#include "Converter.h"
#include "ConverterSelectionStrategy.h"
#include "UnambiguousConverterMap.h"
#include "rsb/rsbexports.h"

namespace rsb {
namespace converter {

/**
 * Maintains a collection of converters for a specific wire format. Each
 * converter has a wire type describing the actual message that is written on
 * the wire and a data type that indicates which data it can serialize on the
 * wire.
 *
 * @author jwienke
 * @tparam WireType the wire-type of the collected converters.
 */
template<class WireType>
class Repository: public rsc::runtime::Printable {
public:
    typedef typename Converter<WireType>::Ptr ConverterPtr;

    /** WireSchema and DataType */
    typedef std::pair<std::string, std::string> ConverterSignature;

    typedef std::map<std::string, std::string> ConverterSelectionMap;

    Repository() :
            logger(rsc::logging::Logger::getLogger("rsb.converter.Repository")) {
    }

    typename ConverterSelectionStrategy<WireType>::Ptr getConvertersForSerialization(
            const ConverterSelectionMap& selection =
                    ConverterSelectionMap()) const {
        RSCDEBUG(this->logger, "Building unambiguous map for serialization with selection "
                 << selection);

        boost::shared_ptr< UnambiguousConverterMap<WireType> >
            result(new UnambiguousConverterMap<WireType>());
        for (typename ConverterMap::const_iterator it =
                this->converters.begin(); it != this->converters.end(); ++it) {
            std::string wireSchema = it->first.first;
            std::string dataType = it->first.second;
            RSCTRACE(this->logger, "Considering converter " << it->second);

            // The data-type is not mentioned in the explicit
            // selection. Try to add the converter. This may throw in
            // case of ambiguity.
            if (selection.find(dataType) == selection.end()) {
                RSCTRACE(this->logger, "No explicit selection for data-type "
                         << dataType);
                try {
                    result->addConverter(dataType, it->second);
                } catch (const std::invalid_argument& e) {
                    std::set<std::string> wireSchemas;
                    for (typename ConverterMap::const_iterator it_ =
                            this->converters.begin();
                            it_ != this->converters.end(); ++it_) {
                        if (dataType == it_->first.second)
                            wireSchemas.insert(it_->first.first);
                    }
                    throw std::runtime_error(
                            boost::str(
                                    boost::format(
                                            "Ambiguous converter set for wire-type `%1%' and data-type `%2%': candidate wire-schemas are %3%; hint: add a configuration option `transport.<name>.converter.cpp.<one of %3%> = %2%' to resolve the ambiguity (%4%).")
                                            % rsc::runtime::typeName<WireType>()
                                    % dataType % wireSchemas % e.what()));
                }
            }
            // There is an entry for data-type in the explicit
            // selection. Add the converter if the wire-schema matches.
            else if (wireSchema == selection.find(dataType)->second) {
                RSCDEBUG(this->logger, "Explicit selection "
                         << *selection.find(dataType)
                         << " chooses data wire-schema " << wireSchema
                         << "; adding the converter");
                result->addConverter(dataType, it->second);
            } else {
                RSCDEBUG(this->logger, "Explicit selection "
                         << *selection.find(dataType)
                         << " chooses wire-schema "
                         << selection.find(dataType)->second
                         << " (not " << wireSchema
                         << "); not adding the converter");
            }
        }
        return result;
    }

    typename ConverterSelectionStrategy<WireType>::Ptr getConvertersForDeserialization(
            const ConverterSelectionMap& selection =
                    ConverterSelectionMap()) const {
        RSCDEBUG(this->logger, "Building unambiguous map for deserialization with selection "
                 << selection);

        boost::shared_ptr< UnambiguousConverterMap<WireType> >
            result(new UnambiguousConverterMap<WireType>());
        for (typename ConverterMap::const_iterator it =
                this->converters.begin(); it != this->converters.end(); ++it) {
            std::string wireSchema = it->first.first;
            std::string dataType = it->first.second;
            RSCTRACE(this->logger, "Considering converter " << it->second);

            // The wire-schema is not mentioned in the explicit
            // selection. Try to add the converter. This may throw in
            // case of ambiguity.
            if (selection.find(wireSchema) == selection.end()) {
                RSCTRACE(this->logger, "No explicit selection for wire-schema "
                         << wireSchema);
                try {
                    result->addConverter(wireSchema, it->second);
                } catch (const std::invalid_argument& e) {
                    std::set<std::string> dataTypes;
                    for (typename ConverterMap::const_iterator it_ =
                            this->converters.begin();
                            it_ != this->converters.end(); ++it_) {
                        if (wireSchema == it_->first.first)
                            dataTypes.insert(it_->first.second);
                    }
                    throw std::runtime_error(
                        boost::str(
                            boost::format(
                                "Ambiguous converter set for wire-type `%1%' and wire-schema `%2%': candidate data-types are %3%; hint: add a configuration option `transport.<name>.converter.cpp.\"%2%\" = <one of %3%>' to resolve the ambiguity (%4%).")
                            % rsc::runtime::typeName<WireType>()
                            % wireSchema % dataTypes % e.what()));
                }
            }
            // There is an entry for wire-schema in the explicit
            // selection. Add the converter if the data-type matches.
            else if (dataType == selection.find(wireSchema)->second) {
                RSCDEBUG(this->logger, "Explicit selection "
                         << *selection.find(wireSchema)
                         << " chooses data-type " << dataType
                         << "; adding the converter");
                result->addConverter(wireSchema, it->second);
            } else {
                RSCDEBUG(this->logger, "Explicit selection "
                         << *selection.find(wireSchema)
                         << " chooses data-type "
                         << selection.find(wireSchema)->second
                         << " (not " << dataType
                         << "); not adding the converter");
            }
        }
        return result;
    }

    /**
     * Registers @a converter in the collection.
     *
     * @param converter The converter to register.
     * @param replace If a converter with the same wire schema and
     *                data type as @a converter is already registered,
     *                should it be replaced?
     * @throw std::invalid_argument If there is already a converter
     *                              registered with the same wire
     *                              schema and data type.
     */
    void registerConverter(ConverterPtr converter, bool replace = false) {
        RSCINFO(this->logger, "Registering converter " << converter);

        std::string wireSchema = converter->getWireSchema();
        std::string dataType = converter->getDataType();
        if (this->converters.find(std::make_pair(wireSchema, dataType))
            != this->converters.end()
            && !replace) {
            // TODO use RSB exception; but do we have one for invalid argument?
            throw std::invalid_argument(
                    boost::str(
                            boost::format(
                                    "There already is a converter for wire-schema `%1%' and data-type `%2%'")
                                    % wireSchema % dataType));
        }
        this->converters[std::make_pair(wireSchema, dataType)] = converter;
    }

    ConverterPtr getConverter(const std::string& wireSchema,
            const std::string& dataType) const {
        typename ConverterMap::const_iterator it = this->converters.find(
                std::make_pair(wireSchema, dataType));
        if (it == this->converters.end()) {
            throw rsc::runtime::NoSuchObject(
                    boost::str(
                            boost::format(
                                    "Could not find a converter for wire-schema `%1%' and data-type `%2%'")
                                    % wireSchema % dataType));
        }
        return it->second;
    }

    ConverterPtr getConverter(const ConverterSignature& signature) const {
        return getConverter(signature.first, signature.second);
    }

    void clear() {
        this->converters.clear();
    }

    typedef boost::shared_ptr<Repository<WireType> > Ptr;

private:
    typedef std::map<ConverterSignature, ConverterPtr> ConverterMap;

    rsc::logging::LoggerPtr logger;
    ConverterMap converters;

    std::string getClassName() const {
        return "Repository<" + rsc::runtime::typeName<WireType>() + ">";
    }

    void printContents(std::ostream& stream) const {
        stream << std::endl;
        for (typename ConverterMap::const_iterator it =
                this->converters.begin(); it != this->converters.end(); ++it) {
            stream << "\t" << std::setw(16) << std::left << it->first.first
                   << " <-> " << std::setw(16) << std::left << it->first.second
                   << std::endl
                   << "\t\t" << *it->second << std::endl;
        }
    }
};


/**
 * @name internal repository implementation
 *
 * All this is done so that the memory of Repositories is maintained inside the
 * RSB DLL on windows. Otherwise multiple clients would have different
 * instances of the repositories.
 *
 * Moreover, I have added a general name-based lookup for repositories instead
 * of being fixed to wire types (c++ types) as this might become a scalability
 * problem later. We might find out that e.g. several connectors use string as
 * their data type and still converters need to be fundamentally different. For
 * this purpose general name-based lookup is possible now. So far only
 * internally and the type-based methods map to this mechanism by using RTTI
 * names.
 *
 * @todo somehow find out if we can get rid of all the void pointer handling.
 *       right now I am missing a replacement of reinterpret_cast for
 *       shared_ptrs. The underlying problem is that Converter uses references
 *       and hence no Repository<void> can be declared as a polymorphic base
 *       type for the generic handling by name
 */
//@{

/**
 * An internal factory object to create typed converter repositories.
 *
 * @author jwienke
 * @note internal class
 */
class RSB_EXPORT RepositoryCreater {
public:
    virtual ~RepositoryCreater();

    /**
     * Factory method to create a new Repository.
     *
     * @return new Repository instance
     */
    virtual void* create() = 0;
};

/**
 * Returns a repository for a converters of a certain wire type by a look up
 * based on a name for the wire type.
 *
 * @param wireTypeName of of the wire type.
 * @param creater if no repository exists with this name, this object is used
 *                to instantiate a new one.
 * @return converter repository.
 * @note currently an internal method. Do not use it.
 */
RSB_EXPORT void* converterRepositoryByName(const std::string &wireTypeName,
        RepositoryCreater &creater);

/**
 * A RepositoryCreate which can be statically typed using a template argument
 * for the desired wire type.
 *
 * @tparam WireType type of the wire of underlying converters
 * @author jwienke
 */
template<class WireType>
class TypeBasedRepositoryCreater: public RepositoryCreater {
public:
    virtual ~TypeBasedRepositoryCreater() {
    }
    void* create() {
        return new Repository<WireType> ;
    }
};

//@}

/**
 * Returns a Repository of Converter instances specifically for the given wire
 * type.
 *
 * @tparam WireType type of the wire to serialize to / from
 * @return converter repository
 */
template<class WireType>
typename Repository<WireType>::Ptr converterRepository() {
    static TypeBasedRepositoryCreater<WireType> creater;
    return typename Repository<WireType>::Ptr(
            (Repository<WireType>*) converterRepositoryByName(
                    rsc::runtime::typeName<WireType>(), creater),
            rsc::misc::NullDeleter());
}

/**
 * @deprecated use #converterRepository() instead
 * @return converter repository for converters that converter to std::string
 *         wires
 */
DEPRECATED(RSB_EXPORT Repository<std::string>::Ptr stringConverterRepository());

}
}

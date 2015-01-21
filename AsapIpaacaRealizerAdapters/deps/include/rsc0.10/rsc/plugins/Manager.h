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

#include <vector>
#include <set>
#include <map>

#include <boost/regex.hpp>
#include <boost/filesystem/path.hpp>
#include <boost/noncopyable.hpp>
#include <boost/shared_ptr.hpp>

#include "../logging/Logger.h"

#include "Plugin.h"

#include "rsc/rscexports.h"

namespace rsc {
namespace plugins {

/**
 * Instances of this class manages plugin search path entries and plugins.
 *
 * Plugins can be retrieved as #Plugin objects, loaded and unloaded.
 *
 * Plugins names are constructed from the shared library file name up to the
 * first dot in the file name. Common shared library prefixes are stripped.
 * E.g. libfoo.0.9.so will have the name foo.
 *
 * Manager instances are not thread-safe and access needs to be synchronized.
 *
 * @author jmoringe
 */
class RSC_EXPORT Manager: public boost::noncopyable {
public:

    Manager();

    virtual ~Manager();

    /**
     * Returns the current plugin search path.
     *
     * @return A #std::vector of #boost::filesystem::path objects.
     */
    std::vector<boost::filesystem::path> getPath() const;

    /**
     * Adds @a path to the list of search path entries.
     *
     * @param path A #boost::filesystem::path designating a directory
     *             which should be searched for plugins.
     * @throw exception If @a path does not exist or is otherwise
     *                  inaccessible.
     * @throw runtime_error If the path contains a plugin which has the same
     *                      plugin name as other already loaded plugins. This
     *                      might also happen for two plugins in the same
     *                      directory.
     */
    void addPath(const boost::filesystem::path& path);

    /**
     * Returns the set of known plugins, potentially filtered by name
     * according to regular expression @a regex.
     *
     * @param regex A regular expression object which is matched
     *              against plugin names, causing only matching plugin
     *              to be returned.
     * @return A copy of the set of known plugins (or subset thereof).
     */
    std::set<PluginPtr> getPlugins(const boost::regex& regex
                                   = boost::regex(".*")) const;

    /**
     * Returns the set of known plugins, filtered by name according to
     * regular expression @a regex.
     *
     * @param regex A regular expression string which is matched
     *              against plugin names, causing only matching plugin
     *              to be returned.
     * @return A copy of the set of known plugins (or subset thereof).
     */
    std::set<PluginPtr> getPlugins(const std::string& regex) const;

    /**
     * Return the plugin designated by @a name.
     *
     * @param name The name of the plugin which should be returned.
     * @return The requested plugin.
     * @throw NoSuchObject If @a name does not designate a plugin.
     */
    PluginPtr getPlugin(const std::string& name) const;
private:

    typedef std::vector<boost::filesystem::path> PathList;
    typedef std::map<std::string, PluginPtr>     PluginMap;

    logging::LoggerPtr logger;

    PathList  path;
    PluginMap plugins;

};

typedef boost::shared_ptr<Manager> ManagerPtr;

}
}

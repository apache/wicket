/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket;

/**
 * Interface to an application. An application has a name and settings. The settings
 * specify how the application is to function. Currently there is only one kind of
 * application, and that is HttpApplication, which is a servlet and therefore subclasses
 * javax.servlet.http.HttpServlet. HttpApplication's name and settings are available
 * through its IApplication interface.
 * @see wicket.protocol.http.HttpApplication
 * @author Jonathan Locke
 */
public interface IApplication
{ // TODO finalize javadoc
    /**
     * Gets the name of this application.
     * @return Returns the name.
     */
    public String getName();

    /**
     * Gets settings for this application.
     * @return The applications settings.
     */
    public ApplicationSettings getSettings();
}

///////////////////////////////// End of File /////////////////////////////////

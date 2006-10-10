/*
 * $Id: ApplicationStringResourceLoader.java,v 1.5 2005/01/19 08:07:57
 * jonathanlocke Exp $ $Revision$ $Date: 2006-04-26 21:35:33 +0000 (Wed,
 * 26 Apr 2006) $
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.resource.loader;

import wicket.Application;

/**
 * This string resource loader attempts to find a single resource bundle that
 * has the same name and location as the application. If this bundle is found
 * then strings are obtained from here. This implementation is fully aware of
 * both locale and style values when trying to obtain the appropriate bundle.
 * 
 * @author Chris Turner
 * @author Juergen Donnerstag
 * 
 * TODO Post 1.2: remove it. It doesn't provide any value compared to
 * ClassStringResourceLoader
 */
public class ApplicationStringResourceLoader extends ClassStringResourceLoader
{
	/**
	 * Create and initialise the resource loader.
	 * 
	 * @param application
	 *            The application that this resource loader is associated with
	 */
	public ApplicationStringResourceLoader(final Application application)
	{
		super(application, application.getClass());
	}
}
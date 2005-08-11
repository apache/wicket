/*
 * $Id$
 * $Revision$
 * $Date$
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
package wicket.markup.html;

import wicket.Application;
import wicket.Resource;
import wicket.ResourceReference;

/**
 * A convenience class for creating resource references to static resources.
 * 
 * @author Jonathan Locke
 */
public class PackageResourceReference extends ResourceReference
{
	/**
	 * Constuctor to get a resource reference to a packaged resource.
	 * It will bind itself directly to the given application object, so
	 * that the resource will be created if it did not exist and added to 
	 * the application shared resources.
	 * 
	 * Package resources should be added by a IComponentInitializer implementation
	 * So that all needed packaged resources are there on startup of the application. 
	 * 
	 * @param application
	 *            The application to bind to
	 * @param scope
	 *            The scope of the binding
	 * @param name
	 *            The name of the resource
	 * @see ResourceReference#ResourceReference(Class, String)
	 */
	public PackageResourceReference(Application application, Class scope, String name)
	{
		super(scope, name);
		bind(application);
	}

	/**
	 * Constuctor to get a resource reference to a packaged resource.
	 * It will bind itself directly to the given application object, so
	 * that the resource will be created if it did not exist and added to 
	 * the application shared resources.
	 * 
	 * Package resources should be added by a IComponentInitializer implementation
	 * So that all needed packaged resources are there on startup of the application. 
	 *
	 * The scope of this constructor will be the Application class itself.
	 * 
	 * @param application
	 *            The application to bind to
	 * @param name
	 *            The name of the resource
	 * @see ResourceReference#ResourceReference(Class, String)
	 */
	public PackageResourceReference(Application application, String name)
	{
		super(name);
		bind(application);
	}

	/**
	 * Constuctor to get a resource reference to a packaged resource that
	 * is already bindend to the current applicaiton. 
	 * 
	 * It will not bind a resource to the current application object, 
	 * so the resource must be created by a IComponentInitializer implementation.
	 * So that it is already binded at startup. 
	 *
	 * @param scope
	 *            The scope of the binding
	 * @param name
	 *            The name of the resource
	 * @see ResourceReference#ResourceReference(Class, String)
	 */
	public PackageResourceReference(Class scope, String name)
	{
		super(scope, name);
	}
	
	/**
	 * @see wicket.ResourceReference#newResource()
	 */
	protected Resource newResource()
	{
		return PackageResource.get(getScope().getPackage(), getName(), getLocale(), getStyle());
	}
}
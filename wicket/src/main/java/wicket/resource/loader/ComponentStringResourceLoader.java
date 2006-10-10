/*
 * $Id: ComponentStringResourceLoader.java,v 1.5 2005/01/19 08:07:57
 * jonathanlocke Exp $ $Revision$ $Date: 2006-04-08 21:48:26 +0000 (Sat,
 * 08 Apr 2006) $
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
 * @inheritDoc
 */
public class ComponentStringResourceLoader extends AbstractStringResourceLoader
{
	/**
	 * Create and initialise the resource loader.
	 * 
	 * @param application
	 *            Wickets application object
	 */
	public ComponentStringResourceLoader(final Application application)
	{
		super(application);
	}
}
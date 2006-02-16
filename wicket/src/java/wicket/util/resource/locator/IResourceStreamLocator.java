/*
 * $Id$
 * $Revision$ $Date$
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
package wicket.util.resource.locator;

import java.util.Locale;

import wicket.util.resource.IResourceStream;

/**
 * Interface for code that locates resources.
 * 
 * @author Jonathan Locke
 */
public interface IResourceStreamLocator
{
	/**
	 * Loads a resource, given a path, style, locale and extension.
	 * 
	 * @param clazz
	 *            The class loader for delegating the loading of the resource
	 * @param path
	 *            The path of the resource
	 * @param style
	 *            Any resource style, such as a skin style (see
	 *            {@link wicket.Session})
	 * @param locale
	 *            The locale of the resource to load
	 * @param extension
	 *            The extension of the resource
	 * 
	 * @return The resource
	 */
	public IResourceStream locate(Class clazz, String path, String style, Locale locale,
			String extension);
}

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

import wicket.util.file.Path;
import wicket.util.resource.IResourceStream;

/**
 * A resource locator that looks in default places for resources. At the present
 * time, the path and default classloader are searched.
 * 
 * @author Jonathan Locke
 */
public final class DefaultResourceStreamLocator extends ResourceStreamLocator
{
	/**
	 * Constructor
	 * 
	 * @param path
	 *            The path to search
	 */
	public DefaultResourceStreamLocator(final Path path)
	{
		super(new IResourceStreamLocator()
		{
			private final PathResourceStreamLocator pathLocator = new PathResourceStreamLocator(path);
			private final ClassLoaderResourceStreamLocator classLoaderLocator = new ClassLoaderResourceStreamLocator();

			public IResourceStream locate(String path, String style, Locale locale, String extension)
			{
				IResourceStream resource = pathLocator.locate(path, style, locale, extension);
				if (resource != null)
				{
					return resource;
				}
				return classLoaderLocator.locate(path, style, locale, extension);
			}
		});
	}
}

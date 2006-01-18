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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import wicket.util.file.IResourceFinder;
import wicket.util.resource.IResourceStream;

/**
 * A resource locator that looks in default places for resources. At the present
 * time, the given finder and the default classloader are searched.
 * 
 * @author Jonathan Locke
 */
public final class DefaultResourceStreamLocator extends ResourceStreamLocator
{
	private final List locators = new ArrayList();

	/**
	 * Constructor
	 * 
	 * @param finder
	 *            The finder to search
	 */
	public DefaultResourceStreamLocator(final IResourceFinder finder)
	{
		super(null);
		
		setResourceStreamLocator(new IResourceStreamLocator()
		{
			public IResourceStream locate(ClassLoader loader, String path, String style,
					Locale locale, String extension)
			{
				Iterator iter = locators.iterator();
				while (iter.hasNext())
				{
					IResourceStream resource = ((IResourceStreamLocator)iter.next()).locate(loader,
							path, style, locale, extension);
					if (resource != null)
					{
						return resource;
					}
				}
				return null;
			}
		});

		locators.add(new ResourceFinderResourceStreamLocator(finder));
		locators.add(new ClassLoaderResourceStreamLocator());
	}

	/**
	 * Add a resource stream locator 
	 * 
	 * @param index
	 * @param locator
	 */
	public void add(final int index, final IResourceStreamLocator locator)
	{
		locators.add(index, locator);
	}

	/**
	 * Add a resource stream locator 
	 * 
	 * @param locator
	 */
	public void add(final IResourceStreamLocator locator)
	{
		locators.add(locator);
	}

	/**
	 * Remove locator from list
	 * 
	 * @param index
	 * @return locator which has been removed
	 */
	public IResourceStreamLocator remove(final int index)
	{
		return (IResourceStreamLocator) locators.remove(index);
	}

	/**
	 * Remove locator from list
	 * 
	 * @param locator
	 * @return true, if object has been found and removed
	 */
	public boolean remove(final IResourceStreamLocator locator)
	{
		return locators.remove(locator);
	}

	/**
	 * 
	 * @return Number of locators in the list
	 */
	public int size()
	{
		return locators.size();
	}
}

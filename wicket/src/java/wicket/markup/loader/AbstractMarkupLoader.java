/*
 * $Id: MarkupCache.java 4639 2006-02-26 01:44:07 -0800 (Sun, 26 Feb 2006)
 * jdonnerstag $ $Revision$ $Date: 2006-02-26 01:44:07 -0800 (Sun, 26 Feb
 * 2006) $
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
package wicket.markup.loader;

import java.io.IOException;

import wicket.MarkupContainer;
import wicket.markup.IMarkup;
import wicket.markup.MarkupResourceStream;
import wicket.util.resource.ResourceStreamNotFoundException;

/**
 * Default implementation of a IMarkupLoader. We expect most real
 * implementations of IMarkupLoader to provide there own loadMarkup()
 * implementation and call the default implementation provided in this class if
 * chaining the loaders is required.
 * 
 * @author Juergen Donnerstag
 */
public abstract class AbstractMarkupLoader implements IMarkupLoader
{
	private IMarkupLoader parentLoader;

	/**
	 * Constructor.
	 */
	public AbstractMarkupLoader()
	{
	}

	/**
	 * 
	 * @param parent
	 */
	public void setParent(final IMarkupLoader parent)
	{
		this.parentLoader = parent;
	}

	/**
	 * @see wicket.markup.loader.IMarkupLoader#loadMarkup(wicket.MarkupContainer,
	 *      wicket.markup.MarkupResourceStream)
	 */
	public IMarkup loadMarkup(final MarkupContainer container,
			final MarkupResourceStream markupResourceStream) throws IOException,
			ResourceStreamNotFoundException
	{
		if (this.parentLoader == null)
		{
			return null;
		}

		return parentLoader.loadMarkup(container, markupResourceStream);
	}
}

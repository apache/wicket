/*
 * $Id: AbstractMarkupFilter.java 3916 2006-01-20 13:07:21 +0000 (Fri, 20 Jan
 * 2006) jdonnerstag $ $Revision$ $Date: 2006-01-20 13:07:21 +0000 (Fri,
 * 20 Jan 2006) $
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
package wicket.markup.parser;

import java.text.ParseException;

import wicket.markup.ComponentTag;

/**
 * Base class for markup filters
 * 
 * @see wicket.markup.MarkupParser
 * 
 * @author Jonathan Locke
 */
public abstract class AbstractMarkupFilter implements IMarkupFilter
{
	/** The next MarkupFilter in the chain */
	private IMarkupFilter parent;

	/**
	 * Construct.
	 * 
	 * @param parent
	 *            The parent of this component The next element in the chain.
	 */
	public AbstractMarkupFilter()
	{
	}

	/**
	 * Construct.
	 * 
	 * @param parent
	 *            The parent of this component The next element in the chain.
	 */
	public AbstractMarkupFilter(final IMarkupFilter parent)
	{
		this.parent = parent;
	}

	/**
	 * @return The next MarkupFilter in the chain
	 */
	public final IMarkupFilter getParent()
	{
		return parent;
	}

	/**
	 * Set new parent.
	 * 
	 * @param parent
	 *            The parent of this component The next element in the chain
	 */
	public final void setParent(final IMarkupFilter parent)
	{
		this.parent = parent;
	}

	/**
	 * A convinience function to retrieve the next tag (same as nextTag()),
	 * however assuming that it is a ComponentTag.
	 * 
	 * @return ComponentTag
	 * @throws ParseException
	 */
	protected final ComponentTag nextComponentTag() throws ParseException
	{
		return (ComponentTag)getParent().nextTag();
	}
}

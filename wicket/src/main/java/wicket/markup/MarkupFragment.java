/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.markup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.util.string.AppendingStringBuffer;

/**
 * A list of markup elements associated with a Markup. Might be all elements of
 * a markup resource, might be just the elements associated with a specific tag.
 * 
 * @see wicket.markup.Markup
 * @see wicket.markup.MarkupElement
 * @see wicket.markup.ComponentTag
 * @see wicket.markup.RawMarkup
 * 
 * @author Juergen Donnerstag
 */
public class MarkupFragment
{
	private static final Log log = LogFactory.getLog(MarkupFragment.class);

	/** Placeholder that indicates no markup */
	public static final MarkupFragment NO_MARKUP_FRAGMENT = new MarkupFragment(Markup.NO_MARKUP);

	/** The list of markup elements */
	private/* final */List markupElements;

	/** The associate markup */
	private final Markup markup;

	/**
	 * Constructor
	 * 
	 * @param markup
	 *            The associated Markup
	 */
	MarkupFragment(final Markup markup)
	{
		this.markup = markup;
		this.markupElements = new ArrayList();
	}

	/**
	 * @return String representation of markup list
	 */
	public final String toString()
	{
		final AppendingStringBuffer buf = new AppendingStringBuffer(400);
		buf.append(this.markup.toString());
		buf.append("\n");

		final Iterator iter = this.markupElements.iterator();
		while (iter.hasNext())
		{
			buf.append(iter.next());
			buf.append(",");
		}

		return buf.toString();
	}

	/**
	 * For Wicket it would be sufficient for this method to be package
	 * protected. However to allow wicket-bench easy access to the information
	 * ...
	 * 
	 * @param index
	 *            Index into markup list
	 * @return Markup element
	 */
	public final MarkupElement get(final int index)
	{
		return (MarkupElement)markupElements.get(index);
	}

	/**
	 * Gets the associate markup
	 * 
	 * @return The associated markup
	 */
	public final Markup getMarkup()
	{
		return this.markup;
	}

	/**
	 * For Wicket it would be sufficient for this method to be package
	 * protected. However to allow wicket-bench easy access to the information
	 * ...
	 * 
	 * @return Number of markup elements
	 */
	public int size()
	{
		return markupElements.size();
	}

	/**
	 * Add a MarkupElement
	 * 
	 * @param markupElement
	 */
	final void addMarkupElement(final MarkupElement markupElement)
	{
		this.markupElements.add(markupElement);
	}

	/**
	 * Add a MarkupElement
	 * 
	 * @param pos
	 * @param markupElement
	 */
	final void addMarkupElement(final int pos, final MarkupElement markupElement)
	{
		this.markupElements.add(pos, markupElement);
	}

	/**
	 * Make all tags immutable and the list of elements unmodifable.
	 */
	final void makeImmutable()
	{
		for (int i = 0; i < this.markupElements.size(); i++)
		{
			MarkupElement elem = (MarkupElement)this.markupElements.get(i);
			if (elem instanceof ComponentTag)
			{
				// Make the tag immutable
				((ComponentTag)elem).makeImmutable();
			}
		}

		this.markupElements = Collections.unmodifiableList(this.markupElements);
	}

	/**
	 * Reset the markup to its defaults, except for the wicket namespace which
	 * remains unchanged.
	 */
	final void reset()
	{
		this.markupElements = new ArrayList();
	}

	/**
	 * Create an iterator for the markup elements
	 * 
	 * @return Iterator
	 */
	public final Iterator iterator()
	{
		return iterator(0, null);
	}

	/**
	 * Create an iterator for the tags being an istance of 'matchClass'
	 * 
	 * @param startIndex
	 *            The index to start with
	 * @param matchClass
	 *            Iterate over elements matching the class
	 * @return Iterator
	 */
	public final Iterator iterator(final int startIndex, final Class matchClass)
	{
		return new Iterator()
		{
			int index = startIndex - 1;

			public boolean hasNext()
			{
				while (++index < size())
				{
					MarkupElement element = get(index);
					if ((matchClass == null) || matchClass.isInstance(element))
					{
						return true;
					}
				}
				return false;
			}

			public Object next()
			{
				return get(index);
			}

			public void remove()
			{
				markupElements.remove(index);
				index -= 1;
			}
		};
	}
}

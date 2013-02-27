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
package org.apache.wicket.markup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The content of a markup file, consisting of a list of markup elements.
 * 
 * @see MarkupResourceStream
 * @see MarkupElement
 * @see ComponentTag
 * @see RawMarkup
 * 
 * @author Juergen Donnerstag
 */
public class Markup implements IMarkupFragment
{
	private static final Logger log = LoggerFactory.getLogger(Markup.class);

	/** Placeholder that indicates no markup */
	public static final Markup NO_MARKUP = new Markup();

	/** The list of markup elements */
	private/* final */List<MarkupElement> markupElements;

	/** The associated markup file */
	private final MarkupResourceStream markupResourceStream;

	/**
	 * Take the markup string, parse it and return the Markup (list of MarkupElements).
	 * <p>
	 * Limitation: Please note that MarkupFactory is NOT used and thus no caching is used (which
	 * doesn't matter for Strings anyway), but what might matter is that your own MarkupFilters are
	 * not applied, which you might have registered with MarkupFactory.
	 * 
	 * @param markup
	 *      the string to use as markup
	 * @return Markup The parsed markup
	 */
	public static Markup of(final String markup)
	{
		return of(markup, MarkupParser.WICKET);
	}

	/**
	 * Take the markup string, parse it and return the Markup (list of MarkupElements).
	 * <p>
	 * Limitation: Please note that MarkupFactory is NOT used and thus no caching is used (which
	 * doesn't matter for Strings anyway), but what might matter is that your own MarkupFilters are
	 * not applied, which you might have registered with MarkupFactory.
	 *
	 * @param markup
	 *      the string to use as markup
	 * @param wicketNamespace
	 *      the namespace for Wicket elements and attributes
	 * @return Markup The parsed markup
	 */
	public static Markup of(final String markup, String wicketNamespace)
	{
		try
		{
			MarkupParser markupParser = new MarkupParser(markup);
			markupParser.setWicketNamespace(wicketNamespace);
			return markupParser.parse();
		}
		catch (IOException ex)
		{
			throw new RuntimeException(ex);
		}
		catch (ResourceStreamNotFoundException ex)
		{
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Private Constructor for NO_MARKUP only
	 */
	private Markup()
	{
		markupResourceStream = null;
	}

	/**
	 * Constructor
	 * 
	 * @param markupResourceStream
	 *            The associated Markup
	 */
	public Markup(final MarkupResourceStream markupResourceStream)
	{
		if (markupResourceStream == null)
		{
			throw new IllegalArgumentException("Parameter 'markupResourceStream' must not be null");
		}

		this.markupResourceStream = markupResourceStream;
		markupElements = new ArrayList<MarkupElement>();
	}

	@Override
	public final MarkupElement get(final int index)
	{
		return markupElements.get(index);
	}

	@Override
	public final MarkupResourceStream getMarkupResourceStream()
	{
		return markupResourceStream;
	}

	/**
	 * 
	 * @param index
	 * @param elem
	 */
	public final void replace(final int index, final MarkupElement elem)
	{
		Args.notNull(elem, "elem");

		if ((index < 0) || (index >= size()))
		{
			throw new IndexOutOfBoundsException("'index' must be smaller than size(). Index:" +
				index + "; size:" + size());
		}

		markupElements.set(index, elem);
	}

	/**
	 * 
	 * @return The fixed location as a string, e.g. the file name or the URL. Return null to avoid
	 *         caching the markup.
	 */
	public String locationAsString()
	{
		return markupResourceStream.locationAsString();
	}

	@Override
	public final int size()
	{
		return markupElements.size();
	}

	/**
	 * Add a MarkupElement
	 * 
	 * @param markupElement
	 */
	final public void addMarkupElement(final MarkupElement markupElement)
	{
		markupElements.add(markupElement);
	}

	/**
	 * Add a MarkupElement
	 * 
	 * @param pos
	 * @param markupElement
	 */
	final public void addMarkupElement(final int pos, final MarkupElement markupElement)
	{
		markupElements.add(pos, markupElement);
	}

	/**
	 * Make all tags immutable and the list of elements unmodifiable.
	 */
	final public void makeImmutable()
	{
		for (MarkupElement markupElement : markupElements)
		{
			if (markupElement instanceof ComponentTag)
			{
				// Make the tag immutable
				((ComponentTag)markupElement).makeImmutable();
			}
		}

		markupElements = Collections.unmodifiableList(markupElements);
	}

	@Override
	public final IMarkupFragment find(final String id)
	{
		Args.notEmpty(id, "id");

		MarkupStream stream = new MarkupStream(this);
		stream.setCurrentIndex(0);
		while (stream.hasMore())
		{
			MarkupElement elem = stream.get();
			if (elem instanceof ComponentTag)
			{
				ComponentTag tag = stream.getTag();
				if (tag.isOpen() || tag.isOpenClose())
				{
					if (tag.getId().equals(id))
					{
						return stream.getMarkupFragment();
					}
					if (tag.isOpen() && !tag.hasNoCloseTag() && !(tag instanceof WicketTag) &&
						!"head".equals(tag.getName()) && !tag.isAutoComponentTag())
					{
						stream.skipToMatchingCloseTag(tag);
					}
				}
			}

			stream.next();
		}

		return null;
	}

	@Override
	public final String toString()
	{
		return toString(false);
	}

	/**
	 * @param markupOnly
	 *            True, if only the markup shall be returned
	 * @return String
	 */
	@Override
	public final String toString(final boolean markupOnly)
	{
		final AppendingStringBuffer buf = new AppendingStringBuffer(400);

		if (markupOnly == false)
		{
			if (markupResourceStream != null)
			{
				buf.append(markupResourceStream.toString());
			}
			else
			{
				buf.append("null MarkupResouceStream");
			}
			buf.append("\n");
		}

		if (markupElements != null)
		{
			for (MarkupElement markupElement : markupElements)
			{
				buf.append(markupElement);
			}
		}

		return buf.toString();
	}

	/**
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public final Iterator<MarkupElement> iterator()
	{
		return markupElements.iterator();
	}

	/**
	 * @param startIndex
	 * @param size
	 * @return Iterator
	 */
	public final Iterator<MarkupElement> iterator(int startIndex, int size)
	{
		return markupElements.subList(startIndex, startIndex + size).iterator();
	}
}

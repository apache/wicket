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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.util.resource.IFixedLocationResourceStream;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The content of a markup file, consisting of aalist of markup elements.
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

	/**
	 * @see org.apache.wicket.markup.IMarkupFragment#get(int)
	 */
	public final MarkupElement get(final int index)
	{
		return markupElements.get(index);
	}

	/**
	 * @see org.apache.wicket.markup.IMarkupFragment#getMarkupResourceStream()
	 */
	public final MarkupResourceStream getMarkupResourceStream()
	{
		return markupResourceStream;
	}

	/**
	 * @see MarkupResourceStream#locationAsString()
	 * @see IFixedLocationResourceStream#locationAsString()
	 * 
	 * @return The fixed location as a string, e.g. the file name or the URL. Return null to avoid
	 *         caching the markup.
	 */
	public String locationAsString()
	{
		return markupResourceStream.locationAsString();
	}

	/**
	 * @see org.apache.wicket.markup.IMarkupFragment#size()
	 */
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
				((ComponentTag) markupElement).makeImmutable();
			}
		}

		markupElements = Collections.unmodifiableList(markupElements);
	}

	/**
	 * @see org.apache.wicket.markup.IMarkupFragment#find(java.lang.String)
	 */
	public final IMarkupFragment find(final String id)
	{
		if (Strings.isEmpty(id))
		{
			throw new IllegalArgumentException("Parameter 'id' must not be null or empty");
		}

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

	/**
	 * @see org.apache.wicket.markup.IMarkupFragment#toString()
	 */
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
	public final String toString(final boolean markupOnly)
	{
		final AppendingStringBuffer buf = new AppendingStringBuffer(400);
		if (markupOnly == false)
		{
			buf.append(markupResourceStream.toString());
			buf.append("\n");
		}

		for (MarkupElement markupElement : markupElements)
		{
			buf.append(markupElement);
		}

		return buf.toString();
	}
}

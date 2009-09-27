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

import org.apache.wicket.util.string.AppendingStringBuffer;

/**
 * Represents a portion of a markup file, but always spans a complete tag. E.g.
 * 
 * <pre>
 * open-body-close: &lt;span&gt;body&lt;/span&gt;
 * open-close:      &lt;span/&gt;
 * open-no-close:   &lt;input ...&gt;body
 * </pre>
 * 
 * @see Markup
 * @see MarkupElement
 * 
 * @author Juergen Donnerstag
 */
public class MarkupFragment implements IMarkupFragment
{
	/** The parent markup. Must not be null. */
	private final IMarkupFragment markup;

	/** The index at which the fragment starts, relative to the parent markup */
	private final int startIndex;

	/** The size of the fragment (usually from open to close tag) */
	private final int size;

	/** the component id of the start tag */
	private final String rootPath;

	/**
	 * Construct.
	 * 
	 * @param markup
	 *            The parent markup. May not be null.
	 * @param startIndex
	 *            The start index of the child markup
	 * @throws IndexOutOfBoundsException
	 *             if the index is out of range (<tt>index &lt; 0 || index &gt;= size()</tt>)
	 */
	public MarkupFragment(final IMarkupFragment markup, final int startIndex)
	{
		if (markup == null)
		{
			throw new IllegalArgumentException("Parameter 'markup' must not be null");
		}

		if (startIndex < 0)
		{
			throw new IllegalArgumentException("Parameter 'startIndex' must not be < 0");
		}

		if (startIndex >= markup.size())
		{
			throw new IllegalArgumentException(
				"Parameter 'startIndex' must not be >= markup.size()");
		}

		this.markup = markup;
		this.startIndex = startIndex;

		// Make sure we are at an open tag
		MarkupElement startElem = markup.get(startIndex);
		if ((startElem instanceof ComponentTag) == false)
		{
			throw new IllegalArgumentException(
				"Parameter 'index' does not point to a Wicket open tag");
		}

		// Determine the size. Find the close tag
		int endIndex;
		ComponentTag startTag = (ComponentTag)startElem;
		if (startTag.isOpenClose())
		{
			endIndex = startIndex;
		}
		else if (startTag.hasNoCloseTag())
		{
			for (endIndex = startIndex + 1; endIndex < markup.size(); endIndex++)
			{
				MarkupElement elem = markup.get(endIndex);
				if (elem instanceof ComponentTag)
				{
					endIndex--;
					break;
				}
			}
		}
		else
		{
			for (endIndex = startIndex + 1; endIndex < markup.size(); endIndex++)
			{
				MarkupElement elem = markup.get(endIndex);
				if (elem instanceof ComponentTag)
				{
					ComponentTag tag = (ComponentTag)elem;
					if (tag.closes(startTag))
					{
						break;
					}
				}
			}
		}

		if (endIndex >= markup.size())
		{
			throw new MarkupException("Unable to find close tag for: '" + startTag.toString() +
				"' in " + getRootMarkup().getMarkupResourceStream().toString());
		}

		size = endIndex - startIndex + 1;

		// @TODO Setting the rootPath depending on specific tags is really ugly, since there is
		// no way for user to enhance it in case he creates its own wicket tag. It probably can
		// be removed once we switch to the markup resolution process.
		if (startTag instanceof WicketTag)
		{
			WicketTag tag = (WicketTag)startTag;
			if (tag.isFragementTag() || tag.isContainerTag())
			{
				rootPath = startTag.getId();
			}
			else
			{
				rootPath = null;
			}
		}
		else
		{
			rootPath = startTag.getId();
		}
	}

	/**
	 * @see org.apache.wicket.markup.IMarkupFragment#get(int)
	 */
	public final MarkupElement get(final int index)
	{
		if ((index < 0) || (index > size))
		{
			throw new IndexOutOfBoundsException("Parameter 'index' is out of range: 0 <= " + index +
				" <= " + size);
		}

		// Ask the parent markup
		return markup.get(startIndex + index);
	}

	/**
	 * @see org.apache.wicket.markup.IMarkupFragment#findComponentIndex(java.lang.String,
	 *      java.lang.String)
	 */
	public final int findComponentIndex(String path, final String id, final int startIndex)
	{
		// Prepend rootPath to the 'path' parameter
		path = (path == null ? rootPath : (rootPath == null ? path : rootPath + ":" + path));

		// Search the markup
		int index = markup.findComponentIndex(path, id, this.startIndex + startIndex) -
			this.startIndex;

		if ((index >= 0) && (index < size))
		{
			return index;
		}

		return -1;
	}

	/**
	 * @see org.apache.wicket.markup.IMarkupFragment#find(java.lang.String, java.lang.String, int)
	 */
	public final IMarkupFragment find(final String path, final String id, final int startIndex)
	{
		int index = findComponentIndex(path, id, startIndex);
		if (index >= 0)
		{
			return new MarkupFragment(this, index);
		}
		return null;
	}

	/**
	 * @see org.apache.wicket.markup.IMarkupFragment#getMarkupResourceStream()
	 */
	public final MarkupResourceStream getMarkupResourceStream()
	{
		return markup.getMarkupResourceStream();
	}

	/**
	 * @see org.apache.wicket.markup.IMarkupFragment#size()
	 */
	public final int size()
	{
		return size;
	}

	/**
	 * @return The parent markup. Null if that is a a markup file.
	 */
	public final IMarkupFragment getParentMarkup()
	{
		return markup;
	}

	/**
	 * @return The Markup representing the underlying markup file with all its content
	 */
	public final Markup getRootMarkup()
	{
		IMarkupFragment markup = getParentMarkup();
		while ((markup != null) && ((markup instanceof Markup) == false))
		{
			markup = ((MarkupFragment)markup).getParentMarkup();
		}
		return (Markup)markup;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return toString(false);
	}

	/**
	 * @see org.apache.wicket.markup.IMarkupFragment#toString(boolean)
	 */
	public String toString(boolean markupOnly)
	{
		final AppendingStringBuffer buf = new AppendingStringBuffer(400);
		if (markupOnly == false)
		{
			buf.append(getRootMarkup().getMarkupResourceStream().toString());
			buf.append("\n");
		}

		for (int i = 0; i < size(); i++)
		{
			buf.append(get(i));
		}
		return buf.toString();
	}
}

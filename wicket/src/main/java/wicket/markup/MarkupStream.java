/*
 * $Id: MarkupStream.java 5791 2006-05-20 00:32:57 +0000 (Sat, 20 May 2006)
 * joco01 $ $Revision$ $Date: 2006-05-20 00:32:57 +0000 (Sat, 20 May
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
package wicket.markup;

import java.util.Iterator;
import java.util.List;

import wicket.util.resource.IResourceStream;
import wicket.util.string.Strings;

/**
 * A stream of {@link wicket.markup.MarkupElement}s, subclases of which are
 * {@link wicket.markup.ComponentTag} and {@link wicket.markup.RawMarkup}. A
 * markupFragments stream has a current index in the list of markupFragments
 * elements. The next markupFragments element can be retrieved and the index
 * advanced by calling next(). If the index hits the end, hasMore() will return
 * false.
 * <p>
 * The current markupFragments element can be accessed with get() and as a
 * ComponentTag with getTag().
 * <p>
 * The stream can be seeked to a particular location with setCurrentIndex().
 * <p>
 * Convenience methods also exist to skip component tags (and any potentially
 * nested markupFragments) or raw markupFragments.
 * <p>
 * Several boolean methods of the form at*() return true if the markupFragments
 * stream is positioned at a tag with a given set of characteristics.
 * <p>
 * The resource from which the markupFragments was loaded can be retrieved with
 * getResource().
 * 
 * @author Jonathan Locke
 * @author Juergen Donnerstag
 */
public final class MarkupStream implements Iterable<MarkupElement>
{
	/** Element at currentIndex */
	private MarkupElement current;

	/** Current index in markupFragments stream */
	private int currentIndex = 0;

	/** The markupFragments element list */
	private final MarkupFragment markupFragments;

	/** The markupFragments flattened into a list */
	private final List<MarkupElement> markup;

	/**
	 * Construct.
	 * 
	 * @param fragment
	 */
	public MarkupStream(final MarkupFragment fragment)
	{
		if (fragment == null)
		{
			throw new IllegalArgumentException(
					"You can not create a MarkupStream without a markupFragments fragment. Parameter 'fragment' == null");
		}
		this.markupFragments = fragment;
		this.markupFragments.makeImmutable();
		this.markup = fragment.getAllElementsFlat();

		if (this.markup.size() > 0)
		{
			current = get(currentIndex);
		}
	}

	/**
	 * @return True if current markupFragments element is a close tag
	 */
	public boolean atCloseTag()
	{
		return atTag() && getTag().isClose();
	}

	/**
	 * @return True if current markupFragments element is an openclose tag
	 */
	public boolean atOpenCloseTag()
	{
		return atTag() && getTag().isOpenClose();
	}

	/**
	 * @param componentId
	 *            Required component name attribute
	 * @return True if the current markupFragments element is an openclose tag
	 *         with the given component name
	 */
	public boolean atOpenCloseTag(final String componentId)
	{
		return atOpenCloseTag() && componentId.equals(getTag().getId());
	}

	/**
	 * @return True if current markupFragments element is an open tag
	 */
	public boolean atOpenTag()
	{
		return atTag() && getTag().isOpen();
	}

	/**
	 * @param id
	 *            Required component id attribute
	 * @return True if the current markupFragments element is an open tag with
	 *         the given component name
	 */
	public boolean atOpenTag(final String id)
	{
		return atOpenTag() && id.equals(getTag().getId());
	}

	/**
	 * @return True if current markupFragments element is a tag
	 */
	public boolean atTag()
	{
		return current instanceof ComponentTag;
	}

	/**
	 * @return The current markupFragments element
	 */
	public MarkupElement get()
	{
		return current;
	}

	/**
	 * @return The current markupFragments element as a markupFragments tag
	 */
	public ComponentTag getTag()
	{
		return getTag(true);
	}

	/**
	 * @param throwException
	 *            If true throw an exception if the element is not a
	 *            ComponentTag
	 * @return The current markupFragments element as a markupFragments tag
	 */
	public ComponentTag getTag(final boolean throwException)
	{
		if (current instanceof ComponentTag)
		{
			return (ComponentTag)current;
		}

		if (throwException)
		{
			throwMarkupException("Tag expected");
		}

		return null;
	}

	/**
	 * @return Current index in markupFragments stream
	 */
	public int getCurrentIndex()
	{
		return currentIndex;
	}

	/**
	 * @return The resource where this markupFragments stream came from
	 */
	public IResourceStream getResource()
	{
		return this.markupFragments.getMarkup().getResource();
	}

	/**
	 * @return True if this markupFragments stream has more MarkupElement
	 *         elements
	 */
	public boolean hasMore()
	{
		return currentIndex < markup.size();
	}

	/**
	 * 
	 * @return True, if more ComponentTags are in the stream
	 */
	public boolean hasMoreComponentTags()
	{
		while (hasMore())
		{
			final MarkupElement elem = next();
			if (elem instanceof ComponentTag)
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * 
	 * @return The next markupFragments element in the stream
	 */
	public MarkupElement next()
	{
		// @TODO I think it is a bug. You'll never be able to get(0).
		if (++currentIndex < markup.size())
		{
			return current = get(currentIndex);
		}

		return null;
	}

	/**
	 * @param currentIndex
	 *            New current index in the stream
	 */
	public void setCurrentIndex(final int currentIndex)
	{
		current = get(currentIndex);
		this.currentIndex = currentIndex;
	}

	/**
	 * Skips this component and all nested components
	 */
	public final void skipComponent()
	{
		// Get start tag
		final ComponentTag startTag = getTag();

		if (startTag.isOpen())
		{
			// With HTML not all tags require a close tag which
			// must have been detected by the HtmlHandler earlier on.
			if (startTag.hasNoCloseTag() == false)
			{
				// Skip <tag>
				next();

				// Skip nested components
				skipToMatchingCloseTag(startTag);
			}

			// Skip </tag>
			next();
		}
		else if (startTag.isOpenClose())
		{
			// Skip <tag/>
			next();
		}
		else
		{
			// We were something other than <tag> or <tag/>
			throwMarkupException("Skip component called on bad markupFragments element " + startTag);
		}
	}

	/**
	 * Skips any raw markupFragments at the current position
	 */
	public void skipRawMarkup()
	{
		while (current instanceof RawMarkup)
		{
			next();
		}
	}

	/**
	 * Skips any markupFragments at the current position until the wicket tag
	 * name is found.
	 * 
	 * @param wicketTagName
	 *            wicket tag name to seek
	 */
	public void skipUntil(final String wicketTagName)
	{
		while (true)
		{
			if ((current instanceof ComponentTag) && ((ComponentTag)current).isWicketTag()
					&& ((ComponentTag)current).getName().equals(wicketTagName))
			{
				return;
			}

			// go on until we reach the end
			if (next() == null)
			{
				return;
			}
		}
	}

	/**
	 * @param index
	 *            The index of a markupFragments element
	 * @return The MarkupElement element
	 */
	private MarkupElement get(final int index)
	{
		return markup.get(index);
	}

	/**
	 * Renders markupFragments until a closing tag for openTag is reached.
	 * 
	 * @param openTag
	 *            The open tag
	 */
	public void skipToMatchingCloseTag(final ComponentTag openTag)
	{
		// Loop through the markupFragments in this container
		while (hasMore())
		{
			// If the current markupFragments tag closes the openTag
			if (get().closes(openTag))
			{
				// Done!
				return;
			}

			// Skip element
			next();
		}
		throwMarkupException("Expected close tag for " + openTag);
	}

	/**
	 * Return the XML declaration string, in case if found in the
	 * markupFragments.
	 * 
	 * @return Null, if not found.
	 */
	public String getXmlDeclaration()
	{
		return this.markupFragments.getMarkup().getXmlDeclaration();
	}

	/**
	 * Gets the markupFragments encoding. A markupFragments encoding may be
	 * specified in a markupFragments file with an XML encoding specifier of the
	 * form &lt;?xml ... encoding="..." ?&gt;.
	 * 
	 * @return The encoding, or null if not found
	 */
	public final String getEncoding()
	{
		return this.markupFragments.getMarkup().getEncoding();
	}

	/**
	 * Get the component/container's Class which is directly associated with the
	 * stream.
	 * 
	 * @return The component's class
	 */
	public final Class getContainerClass()
	{
		return this.markupFragments.getMarkup().getResource().getMarkupClass();
	}

	/**
	 * Get the wicket namespace valid for this specific markupFragments
	 * 
	 * @return wicket namespace
	 */
	public final String getWicketNamespace()
	{
		return this.markupFragments.getMarkup().getWicketNamespace();
	}

	/**
	 * True, if associate markupFragments is the same. It will change e.g. if
	 * the markupFragments file has been re-loaded or the locale has been
	 * changed.
	 * 
	 * @param markupStream
	 *            The markupFragments stream to compare with.
	 * @return true, if markupFragments has not changed
	 */
	public final boolean equalMarkup(final MarkupStream markupStream)
	{
		if (markupStream == null)
		{
			return false;
		}
		return (this.markupFragments == markupStream.markupFragments);
	}

	/**
	 * Get the immutable markupFragments associated with the stream
	 * 
	 * @return markupFragments
	 */
	public final IMarkup getMarkupFragments()
	{
		return this.markupFragments.getMarkup();
	}

	/**
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<MarkupElement> iterator()
	{
		return this.markup.iterator();
	}

	/**
	 * Throws a new markupFragments exception
	 * 
	 * @param message
	 *            The exception message
	 * @throws MarkupException
	 */
	public void throwMarkupException(final String message)
	{
		throw new MarkupException(this, message);
	}

	/**
	 * @return An HTML string highlighting the current position in the
	 *         markupFragments stream
	 */
	public String toHtmlDebugString()
	{
		final StringBuffer buffer = new StringBuffer();

		for (int i = 0; i < markup.size(); i++)
		{
			if (i == currentIndex)
			{
				buffer.append("<font color = \"red\">");
			}

			final MarkupElement element = markup.get(i);

			buffer.append(Strings.escapeMarkup(element.toString(), true));

			if (i == currentIndex)
			{
				buffer.append("</font>");
			}
		}

		return buffer.toString();
	}

	/**
	 * @return String representation of markupFragments stream
	 */
	@Override
	public String toString()
	{
		return "[markupFragments = " + String.valueOf(markupFragments.getMarkup()) + ", index = "
				+ currentIndex + ", current = "
				+ ((current == null) ? "null" : current.toUserDebugString()) + "]";
	}
}

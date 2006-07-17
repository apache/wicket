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

import wicket.util.resource.IResourceStream;
import wicket.util.string.Strings;

/**
 * A stream of {@link wicket.markup.MarkupElement}s, subclases of which are
 * {@link wicket.markup.ComponentTag} and {@link wicket.markup.RawMarkup}. A
 * markup stream has a current index in the list of markup elements. The next
 * markup element can be retrieved and the index advanced by calling next(). If
 * the index hits the end, hasMore() will return false.
 * <p>
 * The current markup element can be accessed with get() and as a ComponentTag
 * with getTag().
 * <p>
 * The stream can be seeked to a particular location with setCurrentIndex().
 * <p>
 * Convenience methods also exist to skip component tags (and any potentially
 * nested markup) or raw markup.
 * <p>
 * Several boolean methods of the form at*() return true if the markup stream is
 * positioned at a tag with a given set of characteristics.
 * <p>
 * The resource from which the markup was loaded can be retrieved with
 * getResource().
 * 
 * @author Jonathan Locke
 */
public final class MarkupStream
{
	/** Element at currentIndex */
	private MarkupElement current;

	/** Current index in markup stream */
	private int currentIndex = 0;

	/** The markup element list */
	private final IMarkup markup;

	/**
	 * DO NOT YOU THIS CONSTRUCTOR. IT WILL MOST LIKELY BE REPLACED IN THE NEAR
	 * FUTURE.
	 */
	protected MarkupStream()
	{
		markup = null;
	}

	/**
	 * Constructor
	 * 
	 * @param markup
	 *            List of markup elements
	 */
	public MarkupStream(final IMarkup markup)
	{
		this.markup = markup;

		if (markup.size() > 0)
		{
			current = get(currentIndex);
		}
	}

	/**
	 * @return True if current markup element is a close tag
	 */
	public boolean atCloseTag()
	{
		return atTag() && getTag().isClose();
	}

	/**
	 * @return True if current markup element is an openclose tag
	 */
	public boolean atOpenCloseTag()
	{
		return atTag() && getTag().isOpenClose();
	}

	/**
	 * @param componentId
	 *            Required component name attribute
	 * @return True if the current markup element is an openclose tag with the
	 *         given component name
	 */
	public boolean atOpenCloseTag(final String componentId)
	{
		return atOpenCloseTag() && componentId.equals(getTag().getId());
	}

	/**
	 * @return True if current markup element is an open tag
	 */
	public boolean atOpenTag()
	{
		return atTag() && getTag().isOpen();
	}

	/**
	 * @param id
	 *            Required component id attribute
	 * @return True if the current markup element is an open tag with the given
	 *         component name
	 */
	public boolean atOpenTag(final String id)
	{
		return atOpenTag() && id.equals(getTag().getId());
	}

	/**
	 * @return True if current markup element is a tag
	 */
	public boolean atTag()
	{
		return current instanceof ComponentTag;
	}

	/**
	 * @return The current markup element
	 */
	public MarkupElement get()
	{
		return current;
	}

	/**
	 * @return The current markup element as a markup tag
	 */
	public ComponentTag getTag()
	{
		return getTag(true);
	}

	/**
	 * @param throwException
	 *            If true throw an exception if the element is not a
	 *            ComponentTag
	 * @return The current markup element as a markup tag
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
	 * @return Current index in markup stream
	 */
	public int getCurrentIndex()
	{
		return currentIndex;
	}

	/**
	 * @return The resource where this markup stream came from
	 */
	public IResourceStream getResource()
	{
		return markup.getResource();
	}

	/**
	 * @return True if this markup stream has more MarkupElement elements
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
			MarkupElement elem = next();
			if (elem instanceof ComponentTag)
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * 
	 * @return The next markup element in the stream
	 */
	public MarkupElement next()
	{
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
			throwMarkupException("Skip component called on bad markup element " + startTag);
		}
	}

	/**
	 * Skips any raw markup at the current position
	 */
	public void skipRawMarkup()
	{
		while (current instanceof RawMarkup)
		{
			next();
		}
	}

	/**
	 * Skips any markup at the current position until the wicket tag name is
	 * found.
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
	 * Throws a new markup exception
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
	 * @return An HTML string highlighting the current position in the markup
	 *         stream
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
	 * @return String representation of markup stream
	 */
	@Override
	public String toString()
	{
		return "[markup = " + String.valueOf(markup) + ", index = " + currentIndex + ", current = "
				+ ((current == null) ? "null" : current.toUserDebugString()) + "]";
	}

	/**
	 * @param index
	 *            The index of a markup element
	 * @return The MarkupElement element
	 */
	private MarkupElement get(final int index)
	{
		return markup.get(index);
	}

	/**
	 * Renders markup until a closing tag for openTag is reached.
	 * 
	 * @param openTag
	 *            The open tag
	 */
	private void skipToMatchingCloseTag(final ComponentTag openTag)
	{
		// Loop through the markup in this container
		while (hasMore())
		{
			// If the current markup tag closes the openTag
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
	 * Return the XML declaration string, in case if found in the markup.
	 * 
	 * @return Null, if not found.
	 */
	public String getXmlDeclaration()
	{
		return markup.getXmlDeclaration();
	}

	/**
	 * Gets the markup encoding. A markup encoding may be specified in a markup
	 * file with an XML encoding specifier of the form &lt;?xml ...
	 * encoding="..." ?&gt;.
	 * 
	 * @return The encoding, or null if not found
	 */
	public final String getEncoding()
	{
		return markup.getEncoding();
	}

	/**
	 * Get the component/container's Class which is directly associated with the
	 * stream.
	 * 
	 * @return The component's class
	 */
	public final Class getContainerClass()
	{
		return markup.getResource().getMarkupClass();
	}

	/**
	 * Get the wicket namespace valid for this specific markup
	 * 
	 * @return wicket namespace
	 */
	public final String getWicketNamespace()
	{
		return this.markup.getWicketNamespace();
	}

	/**
	 * True, if associate markup is the same. It will change e.g. if the markup
	 * file has been re-loaded or the locale has been changed.
	 * 
	 * @param markupStream
	 *            The markup stream to compare with.
	 * @return true, if markup has not changed
	 */
	public final boolean equalMarkup(final MarkupStream markupStream)
	{
		if (markupStream == null)
		{
			return false;
		}
		return this.markup == markupStream.markup;
	}

	/**
	 * Find the markup element index of the component with 'path'
	 * 
	 * @param path
	 *            The component path expression
	 * @param id
	 *            The component's id to search for
	 * @return -1, if not found
	 */
	public final int findComponentIndex(final String path, final String id)
	{
		return this.markup.findComponentIndex(path, id);
	}

	/**
	 * 
	 * @return true, if underlying markup has been merged (inheritance)
	 */
	public final boolean isMergedMarkup()
	{
		return this.markup instanceof MergedMarkup;
	}
}

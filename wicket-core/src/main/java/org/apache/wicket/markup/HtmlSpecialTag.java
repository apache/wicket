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

import org.apache.wicket.Component;
import org.apache.wicket.markup.parser.IXmlPullParser.HttpTagType;
import org.apache.wicket.markup.parser.XmlTag;
import org.apache.wicket.markup.parser.XmlTag.TagType;
import org.apache.wicket.request.Response;


/**
 * 
 * @author Juergen Donnerstag
 */
public class HtmlSpecialTag extends MarkupElement
{
	/** The underlying xml tag */
	protected final XmlTag xmlTag;

	/** Boolean flags. See above */
	private int flags = 0;

	private final HttpTagType httpTagType;

	/**
	 * Construct.
	 * 
	 * @param tag
	 *            The underlying xml tag
	 * @param httpTagType
	 */
	public HtmlSpecialTag(final XmlTag tag, final HttpTagType httpTagType)
	{
		xmlTag = tag.makeImmutable();
		this.httpTagType = httpTagType;
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT USE IT!
	 * 
	 * @param flag
	 *            The flag to set
	 * @param set
	 *            True to turn the flag on, false to turn it off
	 */
	public final void setFlag(final int flag, final boolean set)
	{
		if (set)
		{
			flags |= flag;
		}
		else
		{
			flags &= ~flag;
		}
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT USE IT!
	 * 
	 * @param flag
	 *            The flag to test
	 * @return True if the flag is set
	 */
	public final boolean getFlag(final int flag)
	{
		return (flags & flag) != 0;
	}

	/**
	 * Gets the length of the tag in characters.
	 * 
	 * @return The tag's length
	 */
	public final int getLength()
	{
		return xmlTag.getLength();
	}

	/**
	 * @see org.apache.wicket.markup.parser.XmlTag#getPos()
	 * @return Tag location (index in input string)
	 */
	public final int getPos()
	{
		return xmlTag.getPos();
	}

	/**
	 * @return the tag type (OPEN, CLOSE or OPEN_CLOSE).
	 */
	public final TagType getType()
	{
		return xmlTag.getType();
	}

	/**
	 * @see org.apache.wicket.markup.parser.XmlTag#isClose()
	 * @return True if this tag is a close tag
	 */
	public final boolean isClose()
	{
		return xmlTag.isClose();
	}

	/**
	 * @see org.apache.wicket.markup.parser.XmlTag#isOpen()
	 * @return True if this tag is an open tag
	 */
	public final boolean isOpen()
	{
		return xmlTag.isOpen();
	}

	/**
	 * @see org.apache.wicket.markup.parser.XmlTag#isOpenClose()
	 * @return True if this tag is an open and a close tag
	 */
	public final boolean isOpenClose()
	{
		return xmlTag.isOpenClose();
	}

	/**
	 * Copies all internal properties from this tag to <code>dest</code>. This is basically cloning
	 * without instance creation.
	 * 
	 * @param dest
	 *            tag whose properties will be set
	 */
	void copyPropertiesTo(final HtmlSpecialTag dest)
	{
		dest.flags = flags;
	}

	@Override
	public CharSequence toCharSequence()
	{
		return xmlTag.toCharSequence();
	}

	/**
	 * Converts this object to a string representation.
	 * 
	 * @return String version of this object
	 */
	@Override
	public final String toString()
	{
		return "" + httpTagType + ": '" + xmlTag.toString() + "'";
	}

	/**
	 * Write the tag to the response
	 * 
	 * @param response
	 *            The response to write to
	 * @param stripWicketAttributes
	 *            if true, wicket:id are removed from output
	 * @param namespace
	 *            Wicket's namespace to use
	 */
	public final void writeOutput(final Response response, final boolean stripWicketAttributes,
		final String namespace)
	{
		response.write(toString());
	}

	/**
	 * Converts this object to a string representation including useful information for debugging
	 * 
	 * @return String version of this object
	 */
	@Override
	public final String toUserDebugString()
	{
		return xmlTag.toUserDebugString();
	}

	/**
	 * @return Returns the underlying xml tag.
	 */
	public final XmlTag getXmlTag()
	{
		return xmlTag;
	}

	@Override
	public boolean equalTo(final MarkupElement element)
	{
		if (element instanceof HtmlSpecialTag)
		{
			final HtmlSpecialTag that = (HtmlSpecialTag)element;
			return getXmlTag().equalTo(that.getXmlTag());
		}
		return false;
	}

	/**
	 * For subclasses to override. Gets called just before a Component gets rendered. It is
	 * guaranteed that the markupStream is set on the Component and determineVisibility is not yet
	 * called.
	 * 
	 * @param component
	 *            The component that is about to be rendered
	 * @param markupStream
	 *            The current amrkup stream
	 */
	public void onBeforeRender(final Component component, final MarkupStream markupStream)
	{
	}

	/**
	 * Gets httpTagType.
	 * 
	 * @return httpTagType
	 */
	public final HttpTagType getHttpTagType()
	{
		return httpTagType;
	}
}

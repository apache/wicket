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
package org.apache.wicket.core.util.string;

import org.apache.wicket.request.Response;
import org.apache.wicket.util.encoding.UrlEncoder;
import org.apache.wicket.util.lang.Classes;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.value.AttributeMap;
import org.apache.wicket.util.value.IValueMap;

/**
 * Utility methods for CSS.
 *
 * @author eelcohillenius
 */
public final class CssUtils
{
	// FIXME type text/css can be omitted for the style tag in supported browsers
	/** start of CSS inline open tag */
	public final static String INLINE_OPEN_TAG_START = "<style type=\"text/css\"";

	/** CSS inline open tag */
	public final static String INLINE_OPEN_TAG = INLINE_OPEN_TAG_START + ">\n";

	/** CSS inline close tag */
	public final static String INLINE_CLOSE_TAG = "</style>\n";

	public static final String ATTR_ID = "id";
	public static final String ATTR_TYPE = "type";
	public static final String ATTR_LINK_HREF = "href";
	public static final String ATTR_LINK_MEDIA = "media";
	public static final String ATTR_LINK_REL = "rel";
	public static final String ATTR_CSP_NONCE = "nonce";
	public static final String ATTR_SRI_INTEGRITY = "integrity";
	public static final String ATTR_SRI_CROSSORIGIN = "crossorigin";

	/**
	 * Hidden constructor.
	 */
	private CssUtils()
	{
	}

	/**
	 * Write the simple text to the response object surrounded by a style tag.
	 *
	 * @param response
	 *            The HTTP: response
	 * @param text
	 *            The text to added in between the style tags
	 * @param id
	 *            Unique identifier of element
	 * @deprecated please use {@link #writeInlineStyle(Response, CharSequence, IValueMap)} instead
	 */
	@Deprecated
	public static void writeCss(final Response response, final CharSequence text, String id)
	{
		writeOpenTag(response, id);
		response.write(text);
		writeCloseTag(response);
	}

	/**
	 * Write the simple text to the response object surrounded by a style tag.
	 * In most cases the text simply an inline CSS.
	 *
	 * @param response
	 * 		The HTTP: response
	 * @param text
	 * 		The text to added in between the style tags
	 * @param attributes
	 * 		Tag attributes map
	 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/style">Style HTML Element</a>
	 */
	public static void writeInlineStyle(final Response response, final CharSequence text, IValueMap attributes)
	{
		writeOpenTag(response, attributes);
		response.write(text);
		writeCloseTag(response);
	}

	/**
	 * Write open style tag for the inline CSS
	 *
	 * @param response
	 * @param id
	 */
	public static void writeOpenTag(final Response response, String id)
	{
		AttributeMap attributes = new AttributeMap();
		attributes.put(ATTR_ID, id);
		writeOpenTag(response, attributes);
	}

	/**
	 * Write open style tag for the inline CSS
	 *
	 * @param response
	 * 		the response to write to
	 * @param attributes
	 * 		Tag attributes map
	 */
	public static void writeOpenTag(final Response response, IValueMap attributes)
	{
		response.write(INLINE_OPEN_TAG_START);
		if (attributes != null)
		{
			response.write(" " + attributes);
		}
		response.write(">\n");
	}

	/**
	 *
	 * @param response
	 */
	public static void writeCloseTag(final Response response)
	{
		response.write(INLINE_CLOSE_TAG);
	}

	/**
	 * Writes a reference to a css file in the response object
	 *
	 * @param response
	 *            the response to write to
	 * @param url
	 *            the url of the css reference
	 * @param media
	 *            the CSS media
	 * @param markupId
	 *            the markupId
	 * @deprecated please use {@link #writeLink(Response, IValueMap)} instead
	 */
	@Deprecated
	public static void writeLinkUrl(final Response response, final CharSequence url,
		final CharSequence media, final String markupId)
	{
		CssUtils.writeLinkUrl(response, url, media, markupId, null);
	}

	/**
	 * Writes a reference to a css file in the response object
	 *
	 * @param response
	 *            the response to write to
	 * @param url
	 *            the url of the css reference
	 * @param media
	 *            the CSS media
	 * @param markupId
	 *            the markupId
	 * @param rel
	 *            the rel attribute
	 * @deprecated please use {@link #writeLink(Response, IValueMap)} instead
	 */
	@Deprecated
	public static void writeLinkUrl(final Response response, final CharSequence url,
		final CharSequence media, final String markupId, final String rel)
	{
		// TODO to decide something on escaping URLs
		AttributeMap attributes = new AttributeMap();
		attributes.put(ATTR_LINK_REL, Strings.isEmpty(rel) ? "stylesheet" : rel);
		attributes.put(ATTR_TYPE, "text/css");
		attributes.put(ATTR_LINK_HREF, UrlEncoder.FULL_URL_INSTANCE.encode(String.valueOf(url),"UTF-8"));
		if (Strings.isEmpty(media) == false)
		{
			attributes.put(ATTR_LINK_MEDIA, media.toString());
		}
		if (Strings.isEmpty(markupId) == false)
		{
			attributes.put(ATTR_ID, markupId);
		}
		writeLink(response, attributes);
	}

	/**
	 * Writes a reference to a css file in the response object
	 *
	 * @param response
	 * 		the response to write to
	 * @param attributes
	 * 		Attributes map
	 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/link">Link HTML Element</a>
	 */
	public static void writeLink(final Response response, IValueMap attributes)
	{
		response.write("<link ");
		response.write(attributes.toString());
		response.write("/>");
	}

	/**
	 * Get a standardized key for a CSS class.
	 * 
	 * @param scope
	 *            scope of CSS class
	 * @param facet
	 *            facet of CSS class
	 * @return CSS key
	 */
	public static String key(Class<?> scope, String facet)
	{
		return Classes.simpleName(scope) + ".CSS." + facet;
	}
}

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
import org.apache.wicket.util.string.Strings;

/**
 * Utility methods for CSS.
 *
 * @author eelcohillenius
 */
public final class CssUtils
{
	/** CSS inline open tag */
	public final static String INLINE_OPEN_TAG = "<style type=\"text/css\"><!--\n";

	/** CSS inline close tag */
	public final static String INLINE_CLOSE_TAG = "--></style>\n";

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
	 */
	public static void writeCss(final Response response, final CharSequence text, String id)
	{
		writeOpenTag(response, id);
		response.write(text);
		writeCloseTag(response);
	}

	/**
	 *
	 * @param response
	 * @param id
	 */
	public static void writeOpenTag(final Response response, String id)
	{
		response.write("<style type=\"text/css\" ");
		if (id != null)
		{
			response.write("id=\"" + id + "\"");
		}
		response.write("><!--\n");
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
	 *      the response to write to
	 * @param url
	 *      the url of the css reference
	 * @param media
	 *      the CSS media
	 */
	public static void writeLinkUrl(final Response response, final CharSequence url, final CharSequence media)
	{
		response.write("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
		response.write(Strings.escapeMarkup(url));
		response.write("\"");
		if (media != null)
		{
			response.write(" media=\"");
			response.write(Strings.escapeMarkup(media));
			response.write("\"");
		}
		response.write(" />");
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
		return scope.getSimpleName() + ".CSS." + facet;
	}
}

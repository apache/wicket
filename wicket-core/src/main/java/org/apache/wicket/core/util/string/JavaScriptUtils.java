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
import org.apache.wicket.util.value.AttributeMap;

/**
 * Provide some helpers to write javascript related tags to the response object.
 * 
 * @author Juergen Donnerstag
 */
public class JavaScriptUtils
{
	/**
	 * Prefix for JavaScript CDATA content. If this is changed, also update
	 * Wicket.Head.Contributor.processScript() function from wicket-ajax-jquery.js
	 */
	public final static String SCRIPT_CONTENT_PREFIX = "\n/*<![CDATA[*/\n";

	/**
	 * Suffix for JavaScript CDATA content. If this is changed, also update
	 * Wicket.Head.Contributor.processScript() function from wicket-ajax-jquery.js
	 */
	public final static String SCRIPT_CONTENT_SUFFIX = "\n/*]]>*/\n";

	/** Script open tag including content prefix */
	public final static String SCRIPT_OPEN_TAG = "<script type=\"text/javascript\">" +
		SCRIPT_CONTENT_PREFIX;

	/** Script close tag including content suffix */
	public final static String SCRIPT_CLOSE_TAG = SCRIPT_CONTENT_SUFFIX + "</script>\n";

	public static final String ATTR_ID = "id";
	public static final String ATTR_TYPE = "type";
	public static final String ATTR_SCRIPT_SRC = "src";
	public static final String ATTR_SCRIPT_DEFER = "defer";
	public static final String ATTR_SCRIPT_ASYNC = "async";
	public static final String ATTR_CSP_NONCE = "nonce";
	public static final String ATTR_CROSS_ORIGIN = "crossOrigin";
	public static final String ATTR_INTEGRITY = "integrity";

	/**
	 * Escape single and double quotes so that they can be part of e.g. an alert call.
	 * 
	 * Note: JSON values need to escape only the double quote, so this method wont help.
	 * 
	 * @param input
	 *            the JavaScript which needs to be escaped
	 * @return Escaped version of the input
	 */
	public static CharSequence escapeQuotes(final CharSequence input)
	{
		CharSequence s = input;
		if (s != null)
		{
			s = Strings.replaceAll(s, "'", "\\'");
			s = Strings.replaceAll(s, "\"", "\\\"");
		}
		return s;
	}

	/**
	 * Write a reference to a javascript file to the response object
	 *
	 * @param response
	 *            The HTTP response
	 * @param attributes
	 *            Extra tag attributes
	 */
	public static void writeScript(final Response response, AttributeMap attributes)
	{
		response.write("<script");
		response.write(attributes.toCharSequence());
		response.write("></script>");
		response.write("\n");
	}

	/**
	 * Write the simple text to the response object surrounded by a script tag.
	 *
	 * @param response
	 * 		The HTTP: response
	 * @param text
	 * 		The text to added in between the script tags
	 * @param attributes
	 * 		Extra tag attributes
	 */
	public static void writeInlineScript(final Response response, final CharSequence text, AttributeMap attributes)
	{
		writeOpenTag(response, attributes);
		response.write(Strings.replaceAll(text, "</", "<\\/"));
		writeCloseTag(response);
	}

	/**
	 * Write the simple text to the response object surrounded by a script tag.
	 * 
	 * @param response
	 *            The HTTP: response
	 * @param text
	 *            The text to added in between the script tags
	 */
	public static void writeJavaScript(final Response response, final CharSequence text)
	{
		AttributeMap attributes = new AttributeMap();
		attributes.putAttribute(ATTR_TYPE, "text/javascript");
		writeInlineScript(response, text, attributes);
	}

	/**
	 * Write open script tag for inline script.
	 * Content is prefixed with {@link #SCRIPT_CONTENT_PREFIX}.
	 *
	 * @param response
	 * 		the response to write to
	 * @param attributes
	 * 		Tag attributes map
	 */
	public static void writeOpenTag(final Response response, AttributeMap attributes)
	{
		response.write("<script");
		response.write(attributes.toCharSequence());
		response.write(">");
		response.write(SCRIPT_CONTENT_PREFIX);
	}

	/**
	 * Write close script tag for inline script. The close tag is prefixed with {@link #SCRIPT_CONTENT_SUFFIX}
	 *
	 * @param response
	 * 		the response to write to
	 */
	public static void writeCloseTag(final Response response)
	{
		response.write(SCRIPT_CONTENT_SUFFIX);
		response.write("</script>\n");
	}
}
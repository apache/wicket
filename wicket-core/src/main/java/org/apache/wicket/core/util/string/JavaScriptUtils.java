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
 * Provide some helpers to write javascript related tags to the response object.
 *
 * @author Juergen Donnerstag
 */
public class JavaScriptUtils
{
	/** Script open tag */
	public final static String SCRIPT_OPEN_TAG = "<script type=\"text/javascript\">\n/*<![CDATA[*/\n";

	/** Script close tag */
	public final static String SCRIPT_CLOSE_TAG = "\n/*]]>*/\n</script>\n";

	/**
	 * Script open tag. If this tag is changed, also update Wicket.Head.Contributor.processScript()
	 * function from wicket-ajax.js
	 */
	public final static String SCRIPT_CONTENT_PREFIX = "\n/*<![CDATA[*/\n";

	/**
	 * Script close tag. If this tag is changed, also update Wicket.Head.Contributor.processScript()
	 * function from wicket-ajax.js
	 */
	public final static String SCRIPT_CONTENT_SUFFIX = "\n/*]]>*/\n";


	/** The response object */
	private final Response response;

	/**
	 * Construct.
	 *
	 * @param response
	 *            The response object
	 * @param id
	 */
	public JavaScriptUtils(final Response response, String id)
	{
		this.response = response;
		writeOpenTag(response, id);
	}

	/**
	 * Constructor without id for backward compatibility
	 *
	 * @param response
	 *            The response object
	 */
	public JavaScriptUtils(final Response response)
	{
		this.response = response;
		writeOpenTag(response);
	}


	/**
	 * Escape single and double quotes so that they can be part of e.g. an alert call.
	 *
	 * @param input
	 *            input
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
	 * @param url
	 *            The javascript file URL
	 * @param id
	 *            Unique identifier of element
	 */
	public static void writeJavaScriptUrl(final Response response, final CharSequence url,
		final String id)
	{
		writeJavaScriptUrl(response, url, id, false, null);
	}

	/**
	 * Write a reference to a javascript file to the response object
	 *
	 * @param response
	 *            The HTTP response
	 * @param url
	 *            The javascript file URL
	 * @param id
	 *            Unique identifier of element
	 * @param defer
	 *            specifies that the execution of a script should be deferred (delayed) until after
	 *            the page has been loaded.
	 * @param charset
	 *            a non null value specifies the charset attribute of the script tag
	 */
	public static void writeJavaScriptUrl(final Response response, final CharSequence url,
		final String id, boolean defer, String charset)
	{
		response.write("<script type=\"text/javascript\" ");
		if (id != null)
		{
			response.write("id=\"" + Strings.escapeMarkup(id) + "\" ");
		}
		if (defer)
		{
			response.write("defer=\"defer\" ");
		}
		if (charset != null)
		{
			response.write("charset=\"" + Strings.escapeMarkup(charset) + "\" ");
		}
		response.write("src=\"");
		response.write(Strings.escapeMarkup(url));
		response.write("\"></script>");
		response.write("\n");
	}

	/**
	 * Write a reference to a javascript file to the response object
	 *
	 * @param response
	 *            The HTTP response
	 * @param url
	 *            The javascript file URL
	 */
	public static void writeJavaScriptUrl(final Response response, final CharSequence url)
	{
		writeJavaScriptUrl(response, url, null);
	}

	/**
	 * Write the simple text to the response object surrounded by a script tag.
	 *
	 * @param response
	 *            The HTTP: response
	 * @param text
	 *            The text to added in between the script tags
	 * @param id
	 *            Unique identifier of element
	 */
	public static void writeJavaScript(final Response response, final CharSequence text, String id)
	{
		writeOpenTag(response, id);
		response.write(text);
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
		writeJavaScript(response, text, null);
	}

	/**
	 *
	 * @param response
	 * @param id
	 */
	public static void writeOpenTag(final Response response, String id)
	{
		response.write("<script type=\"text/javascript\" ");
		if (id != null)
		{
			response.write("id=\"" + id + "\"");
		}
		response.write(">");
		response.write(SCRIPT_CONTENT_PREFIX);
	}

	/**
	 *
	 * @param response
	 */
	public static void writeOpenTag(final Response response)
	{
		writeOpenTag(response, null);
	}

	/**
	 *
	 * @param response
	 */
	public static void writeCloseTag(final Response response)
	{
		response.write(SCRIPT_CONTENT_SUFFIX);
		response.write("</script>\n");

	}

	/**
	 * @see Response#write(java.lang.CharSequence)
	 * @param script
	 */
	public void write(final CharSequence script)
	{
		response.write(script);
	}

	/**
	 * @see Response#write(CharSequence)
	 * @param script
	 */
	public void println(final CharSequence script)
	{
		response.write(script);
	}

	/**
	 * Write the script close tag to the response. The response output stream remains open.
	 */
	public void close()
	{
		writeCloseTag(response);
	}
}

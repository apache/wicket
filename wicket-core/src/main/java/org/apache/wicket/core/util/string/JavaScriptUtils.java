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
import org.apache.wicket.util.value.HeaderItemAttribute;
import org.apache.wicket.util.value.IValueMap;


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
	 * @param url
	 *            The javascript file URL
	 * @param id
	 *            Unique identifier of element
	 * @deprecated please use {@link #writeScript(Response, IValueMap)} instead
	 */
	@Deprecated
	public static void writeJavaScriptUrl(final Response response, final CharSequence url,
		final String id)
	{
		writeJavaScriptUrl(response, url, id, false, null, false);
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
	 * @deprecated please use {@link #writeScript(Response, IValueMap)} instead
	 */
	@Deprecated
	public static void writeJavaScriptUrl(final Response response, final CharSequence url,
	                                      final String id, boolean defer, String charset)
	{
		writeJavaScriptUrl(response, url, id, defer, charset, false);
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
	 * @param async
	 *            specifies that the script can be loaded asynchronously by the browser
	 * @deprecated please use {@link #writeScript(Response, IValueMap)} instead
	 */
	@Deprecated
	public static void writeJavaScriptUrl(final Response response, final CharSequence url,
		final String id, boolean defer, String charset, boolean async)
	{
		response.write("<script ");
		AttributeMap attributes = new AttributeMap();
		// XXX JS mimetype can be omitted (also see below)
		attributes.add(HeaderItemAttribute.TYPE, "text/javascript");
		attributes.add(HeaderItemAttribute.SCRIPT_SRC, url.toString());
		if (id != null)
		{
			attributes.add(HeaderItemAttribute.ID, String.valueOf(Strings.escapeMarkup(id)));
		}
		if (defer)
		{
			attributes.add(HeaderItemAttribute.SCRIPT_DEFER, "defer");
		}
		if (async)
		{
			attributes.add(HeaderItemAttribute.SCRIPT_ASYNC, "async");
		}
		if (charset != null)
		{
			// FIXME charset attr is deprecated
			// https://developer.mozilla.org/en-US/docs/Web/HTML/Element/script#Deprecated_attributes
			attributes.add("charset", Strings.escapeMarkup(charset).toString());
		}
		response.write(attributes.toString());
		response.write("></script>");
		response.write("\n");
	}

	/**
	 * Write a reference to a javascript file to the response object
	 *
	 * @param response
	 *            The HTTP response
	 * @param attributes
	 *            Extra tag attributes
	 */
	public static void writeScript(final Response response, IValueMap attributes)
	{
		response.write("<script ");
		response.write(attributes.toString());
		response.write("></script>");
		response.write("\n");
	}

	/**
	 * Write a reference to a javascript file to the response object
	 * 
	 * @param response
	 *            The HTTP response
	 * @param url
	 *            The javascript file URL
	 * @deprecated please use {@link #writeScript(Response, IValueMap)} instead
	 */
	@Deprecated
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
	 * @deprecated please use {@link #writeInlineScript(Response, CharSequence, IValueMap)} instead
	 */
	public static void writeJavaScript(final Response response, final CharSequence text, String id)
	{
		writeOpenTag(response, id);
		response.write(Strings.replaceAll(text, "</", "<\\/"));
		writeCloseTag(response);
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
	public static void writeInlineScript(final Response response, final CharSequence text, IValueMap attributes)
	{
		writeInlineScriptOpenTag(response, attributes);
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
		attributes.add(HeaderItemAttribute.TYPE, "text/javascript");
		writeInlineScript(response, text, attributes);
	}

	/**
	 * 
	 * @param response
	 * @param id
	 * @deprecated please use {@link #writeInlineScriptOpenTag(Response, IValueMap)}
	 */
	@Deprecated
	public static void writeOpenTag(final Response response, String id)
	{
		AttributeMap attributes = new AttributeMap();
		attributes.add(HeaderItemAttribute.TYPE, "text/javascript");
		if (id != null)
		{
			attributes.add(HeaderItemAttribute.ID, id);
		}
		writeInlineScriptOpenTag(response, attributes);
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
	public static void writeInlineScriptOpenTag(final Response response, IValueMap attributes)
	{
		response.write("<script ");
		if (attributes != null)
		{
			response.write(attributes.toString());
		}
		response.write(">");
		response.write(SCRIPT_CONTENT_PREFIX);
	}

	/**
	 * 
	 * @param response
	 * @deprecated please use {@link #writeInlineScriptOpenTag(Response, IValueMap)}
	 */
	@Deprecated
	public static void writeOpenTag(final Response response)
	{
		AttributeMap attributes = new AttributeMap();
		attributes.add(HeaderItemAttribute.TYPE, "text/javascript");
		writeInlineScriptOpenTag(response, attributes);
	}

	/**
	 *
	 * @param response
	 * @deprecated use {@link #writeInlineScriptCloseTag(Response)} instead
	 */
	public static void writeCloseTag(final Response response)
	{
		writeInlineScriptCloseTag(response);
	}

	/**
	 * Write close script tag for inline script. The close tag is prefixed with {@link #SCRIPT_CONTENT_SUFFIX}
	 *
	 * @param response
	 * 		the response to write to
	 */
	public static void writeInlineScriptCloseTag(final Response response)
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
	 * Write the inline script close tag to the response. The response output stream remains open.
	 * Calls {@link #writeInlineScriptCloseTag(Response)} internally.
	 * The close tag is prefixed with {@link #SCRIPT_CONTENT_SUFFIX}.
	 */
	public void close()
	{
		writeInlineScriptCloseTag(response);
	}
}

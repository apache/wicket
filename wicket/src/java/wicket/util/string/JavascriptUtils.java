/*
 * $Id: AbstractStringList.java 3947 2006-01-25 16:18:39Z joco01 $ $Revision:
 * 3947 $ $Date: 2006-01-25 17:18:39 +0100 (Mi, 25 Jan 2006) $
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
package wicket.util.string;

import wicket.Response;

/**
 * Provide some helpers to write javascript related tags to the response object.
 * 
 * @author Juergen Donnerstag
 */
public class JavascriptUtils
{

	/** Script open tag */
	public final static String SCRIPT_OPEN_TAG = "<script type=\"text/javascript\"><!--/*--><![CDATA[/*><!--*/\n";

	/** Script close tag */
	public final static String SCRIPT_CLOSE_TAG = "\n/*-->]]>*/</script>\n";

	/** Script open tag */
	public final static String SCRIPT_CONTENT_PREFIX = "<!--/*--><![CDATA[/*><!--*/\n";

	/** Script close tag */
	public final static String SCRIPT_CONTENT_SUFFIX = "\n/*-->]]>*/";


	/** The response object */
	private Response response;

	/**
	 * Construct.
	 * 
	 * @param response
	 *            The response object
	 * @param id
	 *            any id to be rendered
	 */
	public JavascriptUtils(final Response response, String id)
	{
		this.response = response;
		writeOpenTag(response, id);
	}

	/**
	 * Escape quotes and double quotes so that they can be part of e.g. an alert
	 * call.
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
	public static void writeJavascriptUrl(final Response response, final CharSequence url,
			final String id)
	{
		response.write("<script type=\"text/javascript\" ");
		if (id != null)
		{
			response.write("id=\"" + id + "\" ");
		}
		response.write("src=\"");
		response.write(url);
		response.println("\"></script>");
	}

	/**
	 * Write a reference to a javascript file to the response object
	 * 
	 * @param response
	 *            The HTTP response
	 * @param url
	 *            The javascript file URL
	 */
	public static void writeJavascriptUrl(final Response response, final CharSequence url)
	{
		writeJavascriptUrl(response, url, null);
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
	public static void writeJavascript(final Response response, final CharSequence text, String id)
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
	public static void writeJavascript(final Response response, final CharSequence text)
	{
		writeJavascript(response, text, null);
	}

	/**
	 * Write an open tag.
	 * 
	 * @param response
	 *            The response object
	 * @param id
	 *            any id to be rendered
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
		response.println("</script>\n");

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
	 * @see Response#println(java.lang.CharSequence)
	 * @param script
	 */
	public void println(final CharSequence script)
	{
		response.println(script);
	}

	/**
	 * Write the script close tag to the response. The response output stream
	 * remains open.
	 */
	public void close()
	{
		writeCloseTag(response);
	}
}

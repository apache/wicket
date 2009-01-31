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
package org.apache.wicket.protocol.http.portlet;

import org.apache.wicket.IResponseFilter;
import org.apache.wicket.RequestContext;
import org.apache.wicket.markup.MarkupException;
import org.apache.wicket.util.string.AppendingStringBuffer;

/**
 * This filter removes html page top level markup elements like <code>&lt;html&gt;</code>,
 * <code>&lt;head&gt</code> and <code>&lt;body&gt;</code>. The filter is configured automatically by
 * WicketFilter if it detects the application is (potentially) invoked as a Portlet.
 * 
 * @author Ate Douma
 */
public class PortletInvalidMarkupFilter implements IResponseFilter
{
	/*
	 * @see org.apache.wicket.IResponseFilter#filter(AppendingStringBuffer)
	 */
	public AppendingStringBuffer filter(AppendingStringBuffer responseBuffer)
	{
		RequestContext rc = RequestContext.get();
		if (rc.isPortletRequest() && ((PortletRequestContext)rc).isEmbedded())
		{
			deleteFragment(responseBuffer, "<?xml", "?>");
			deleteFragment(responseBuffer, "<!DOCTYPE", ">");
			deleteOpenTag(responseBuffer, "html");
			deleteOpenTag(responseBuffer, "head");
			deleteOpenTag(responseBuffer, "body");
		}

		return responseBuffer;
	}

	/**
	 * Removes entire html fragments from the response buffer (inclusive of fragment body).
	 * 
	 * @param responseBuffer
	 *            the buffer to delete from
	 * @param prefix
	 *            the beginning string to delete
	 * @param postfix
	 *            the end string to delete
	 */
	private void deleteFragment(AppendingStringBuffer responseBuffer, String prefix, String postfix)
	{
		int startIndex, endIndex;
		if ((startIndex = responseBuffer.indexOf(prefix)) > -1)
		{
			if ((endIndex = responseBuffer.indexOf(postfix, startIndex)) > -1)
			{
				responseBuffer.delete(startIndex, endIndex + postfix.length());
			}
		}
	}

	/**
	 * Finds and removes the opening and closing tag, if it exists, from the responseBuffer.
	 * 
	 * @param responseBuffer
	 *            the buffer to search
	 * @param tagName
	 *            the tag to delete
	 */
	private void deleteOpenTag(AppendingStringBuffer responseBuffer, String tagName)
	{
		int startIndex, endIndex;
		// find and remove opening tag, if it exists
		if ((startIndex = responseBuffer.indexOf("<" + tagName)) > -1)
		{
			if ((endIndex = responseBuffer.indexOf(">", startIndex)) > -1)
			{
				responseBuffer.delete(startIndex, endIndex + 1);
			}
			else
			{
				// FIXME if the closing brace of the element cannot be found - doesn't that mean
				// that the entire response fragment is invalid and we should throw exception here?
				// or are we just not making that our problem? wait - doesn't that mean that the
				// fragment is also effectively empty if there are no further '>' chars?
				throw new MarkupException("Cannot find end of element tag named: " + tagName);
			}
			// remove closing tag, if it exists
			if ((startIndex = responseBuffer.indexOf("</" + tagName + ">")) > -1)
			{
				responseBuffer.delete(startIndex, startIndex + tagName.length() + 3);
			}
		}
	}
}
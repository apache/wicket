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
package org.apache.wicket.response.filter;

import org.apache.wicket.util.string.AppendingStringBuffer;

/**
 * An IResponseFilter that removes all invalid XML characters.
 * By default it is used only for Wicket <em>Ajax</em> responses.
 *
 * <p>If the application needs to use it for other use cases then it can either override
 * {@linkplain #shouldFilter(AppendingStringBuffer)} in the case it is used as IResponseFilter or
 * {@linkplain #stripNonValidXMLCharacters(AppendingStringBuffer)} can be used directly.
 * </p>
 *
 * <p>Usage:
 *
 *     MyApplication.java
 *     <code><pre>
 *         public void init() {
 *             super.init();
 *
 *             getRequestCycleSettings().addResponseFilter(new XmlCleaningResponseFilter());
 *         }
 *     </pre></code>
 * </p>
 */
public class XmlCleaningResponseFilter implements IResponseFilter
{
	public AppendingStringBuffer filter(AppendingStringBuffer responseBuffer)
	{
		AppendingStringBuffer result = responseBuffer;
		if (shouldFilter(responseBuffer))
		{
			result = stripNonValidXMLCharacters(responseBuffer);
		}
		return result;
	}

	/**
	 * Decides whether the filter should be applied.
	 *
	 * @param responseBuffer The buffer to filter
	 * @return {@code true} if the buffer brings Ajax response
	 */
	protected boolean shouldFilter(AppendingStringBuffer responseBuffer)
	{
		// To avoid reading the whole buffer for non-Ajax responses
		// read just the first N chars. A candidate can start with:
		// <?xml version="1.0" encoding="UTF-8" standalone="yes"?><ajax-response>
		int min = Math.min(150, responseBuffer.length());
		String firstNChars = responseBuffer.substring(0, min);
		return firstNChars.contains("<ajax-response>");
	}

	/**
	 * This method ensures that the output String has only
	 * valid XML unicode characters as specified by the
	 * XML 1.0 standard. For reference, please see
	 * <a href="http://www.w3.org/TR/2000/REC-xml-20001006#NT-Char">the
	 * standard</a>. This method will return an empty
	 * String if the input is null or empty.
	 *
	 * @param input The StringBuffer whose non-valid characters we want to remove.
	 * @return The in String, stripped of non-valid characters.
	 */
	public AppendingStringBuffer stripNonValidXMLCharacters(AppendingStringBuffer input)
	{
		char[] chars = input.getValue();
		AppendingStringBuffer out = null;

		int codePoint;

		int i = 0;

		while (i < input.length())
		{
			codePoint = Character.codePointAt(chars, i, chars.length);

			if (!isValidXmlChar(codePoint))
			{
				if (out == null)
				{
					out = new AppendingStringBuffer(chars.length);
					out.append(input.subSequence(0, i));
				}
				else
				{
					out.append(Character.toChars(codePoint));
				}
			}
			else if (out != null)
			{
				out.append(Character.toChars(codePoint));
			}

			// Increment with the number of code units(java chars) needed to represent a Unicode char.
			i += Character.charCount(codePoint);
		}

		return out != null ? out : input;
	}

	/**
	 * Checks whether the character represented by this codePoint is
	 * a valid in XML documents.
	 *
	 * @param codePoint The codePoint for the checked character
	 * @return {@code true} if the character can be used in XML documents
	 */
	protected boolean isValidXmlChar(int codePoint)
	{
		return (codePoint == 0x9) ||
			(codePoint == 0xA) ||
			(codePoint == 0xD) ||
			((codePoint >= 0x20) && (codePoint <= 0xD7FF)) ||
			((codePoint >= 0xE000) && (codePoint <= 0xFFFD)) ||
			((codePoint >= 0x10000) && (codePoint <= 0x10FFFF));
	}
}

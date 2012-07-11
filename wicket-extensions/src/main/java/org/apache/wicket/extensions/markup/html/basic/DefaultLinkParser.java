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
package org.apache.wicket.extensions.markup.html.basic;

import org.apache.wicket.util.string.AppendingStringBuffer;

/**
 * This implementation adds link render strategies for email addresses and urls.
 * 
 * @see SmartLinkLabel
 * @see SmartLinkMultiLineLabel
 * 
 * @author Gerolf Seitz
 */
public class DefaultLinkParser extends LinkParser
{
	/** Email address pattern */
	private static final String emailPattern = "[\\w\\.\\-\\\\+]+@[\\w\\.\\-]+";

	/** URL pattern */
	private static final String urlPattern = "([a-zA-Z]+://[\\w\\.\\-\\:\\/~]+)[\\w\\.:\\-/?&=%]*";

	/**
	 * Email address render strategy.<br/>
	 * Renders &lt;a href="mailto:{EMAIL}"&gt;{EMAIL}&lt;/a&gt;
	 */
	public static final ILinkRenderStrategy EMAIL_RENDER_STRATEGY = new ILinkRenderStrategy()
	{
		@Override
		public String buildLink(final String linkTarget)
		{
			return "<a href=\"mailto:" + linkTarget + "\">" + linkTarget + "</a>";
		}
	};

	/**
	 * Email address render strategy. Similar to <code>EMAIL_RENDER_STRATEGY</code>, but encrypts
	 * the email address with html entities.
	 */
	public static final ILinkRenderStrategy ENCRYPTED_EMAIL_RENDER_STRATEGY = new ILinkRenderStrategy()
	{
		@Override
		public String buildLink(final String linkTarget)
		{
			AppendingStringBuffer cryptedEmail = new AppendingStringBuffer(64);
			for (int i = 0; i < linkTarget.length(); i++)
			{
				cryptedEmail.append("&#");
				cryptedEmail.append(Integer.toString(linkTarget.charAt(i)));
				cryptedEmail.append(";");
			}

			AppendingStringBuffer result = new AppendingStringBuffer(256);
			result.append("<a href=\"mailto:");
			result.append(cryptedEmail.toString());
			result.append("\">");
			result.append(cryptedEmail.toString());
			result.append("</a>");

			return result.toString();
		}
	};

	/**
	 * Url render strategy.<br/>
	 * Renders &lt;a href="{URL}"&gt;{URL}&lt;/a&gt;
	 */
	public static final ILinkRenderStrategy URL_RENDER_STRATEGY = new ILinkRenderStrategy()
	{
		@Override
		public String buildLink(final String linkTarget)
		{
			int indexOfQuestion = linkTarget.indexOf('?');
			return "<a href=\"" + linkTarget + "\">" +
				(indexOfQuestion == -1 ? linkTarget : linkTarget.substring(0, indexOfQuestion)) +
				"</a>";
		}
	};

	/**
	 * Default constructor.
	 */
	public DefaultLinkParser()
	{
		addLinkRenderStrategy(emailPattern, EMAIL_RENDER_STRATEGY);
		addLinkRenderStrategy(urlPattern, URL_RENDER_STRATEGY);
	}
}

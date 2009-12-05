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
package org.apache.wicket.ng.request.mapper;

import java.util.Locale;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.wicket.Request;
import org.apache.wicket.ng.markup.html.link.ILinkListener;
import org.apache.wicket.ng.request.Url;
import org.apache.wicket.ng.request.component.RequestablePage;
import org.apache.wicket.ng.request.listener.RequestListenerInterface;

/**
 * @author Matej Knopp
 */
public abstract class AbstractEncoderTest extends TestCase
{

	/**
	 * Construct.
	 */
	public AbstractEncoderTest()
	{
	}

	protected TestMapperContext context = new TestMapperContext();

	@Override
	protected void setUp() throws Exception
	{
		// inititalize the interface
		@SuppressWarnings("unused")
		RequestListenerInterface i = ILinkListener.INTERFACE;
	}

	protected Request getRequest(final Url url)
	{
		return new Request()
		{
			@Override
			public Url getUrl()
			{
				return url;
			}

			@Override
			public Locale getLocale()
			{
				return null;
			}

			@Override
			public String getParameter(String key)
			{
				return null;
			}

			@Override
			public Map<String, String[]> getParameterMap()
			{
				return null;
			}

			@Override
			public String[] getParameters(String key)
			{
				return null;
			}

			@Override
			public String getPath()
			{
				return null;
			}

			@Override
			public String getQueryString()
			{
				return null;
			}

			@Override
			public String getRelativePathPrefixToContextRoot()
			{
				return null;
			}

			@Override
			public String getRelativePathPrefixToWicketHandler()
			{
				return null;
			}
		};
	}

	protected void checkPage(RequestablePage page, int id)
	{
		assertEquals(id, page.getPageId());
	}

}

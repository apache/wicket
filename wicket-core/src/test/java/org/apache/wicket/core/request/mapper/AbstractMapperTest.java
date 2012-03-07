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
package org.apache.wicket.core.request.mapper;

import java.nio.charset.Charset;
import java.util.Locale;

import org.apache.wicket.RequestListenerInterface;
import org.apache.wicket.markup.html.link.ILinkListener;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.component.IRequestablePage;
import org.junit.Assert;
import org.junit.Before;

/**
 * @author Matej Knopp
 */
public abstract class AbstractMapperTest extends Assert
{
	/**
	 * Construct.
	 */
	public AbstractMapperTest()
	{
	}

	protected TestMapperContext context = new TestMapperContext();

	/**
	 * @throws Exception
	 */
	@Before
	public void before() throws Exception
	{
		// inititalize the interface
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
			public Charset getCharset()
			{
				return Charset.forName("UTF-8");
			}

			@Override
			public Url getClientUrl()
			{
				return url;
			}

			@Override
			public Object getContainerRequest()
			{
				return null;
			}
		};
	}

	protected void checkPage(IRequestablePage page, int id)
	{
		assertEquals(id, page.getPageId());
	}

}

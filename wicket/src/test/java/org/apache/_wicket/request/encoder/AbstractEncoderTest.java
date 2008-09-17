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
package org.apache._wicket.request.encoder;

import org.apache._wicket.IPage;
import org.apache._wicket.request.RequestParameters;
import org.apache._wicket.request.Url;
import org.apache._wicket.request.UrlRequestParameters;
import org.apache._wicket.request.request.Request;
import org.apache.wicket.RequestListenerInterface;
import org.apache.wicket.markup.html.link.ILinkListener;

import junit.framework.TestCase;

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

	protected TestEncoderContext context = new TestEncoderContext();

	@Override
	protected void setUp() throws Exception
	{
		// inititalize the interface
		RequestListenerInterface i = ILinkListener.INTERFACE;
	}

	protected Request getRequest(final Url url)
	{
		return new Request()
		{
			@Override
			public RequestParameters getRequestParameters()
			{
				return new UrlRequestParameters(getUrl());
			}

			@Override
			public Url getUrl()
			{
				return url;
			}
		};
	}

	protected void checkPage(IPage page, int id, int version, String pageMapName)
	{
		assertEquals(id, page.getPageId());
		assertEquals(version, page.getPageVersionNumber());
		assertEquals(pageMapName, page.getPageMapName());
	}

}

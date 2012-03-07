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
package org.apache.wicket.protocol.http.request;

import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.SimplePage;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.mapper.CompoundRequestMapper;
import org.apache.wicket.core.request.mapper.CryptoMapper;
import org.apache.wicket.util.crypt.Base64;
import org.apache.wicket.util.crypt.ICrypt;
import org.apache.wicket.util.crypt.ICryptFactory;
import org.junit.Test;

/**
 * Simple test using the WicketTester
 */
public class CryptedUrlWebRequestCodingStrategyTest extends WicketTestCase
{

	@Override
	protected WebApplication newApplication()
	{
		return new MockApplication()
		{
			@Override
			protected void init()
			{
				super.init();
				// install crypto mapper to encrypt all application urls
				getSecuritySettings().setCryptFactory(new TestCryptFactory());
				CompoundRequestMapper root = new CompoundRequestMapper();
				root.add(new CryptoMapper(getRootRequestMapper(), this));
				setRootRequestMapper(root);
			}
		};
	}

	/**
	 * 
	 */
	@Test
	public void clientBidListPage()
	{
		WebPage page = new SimplePage();
		WebPage p = (WebPage)tester.startPage(page);
		assertEquals(page.getClass(), p.getClass());
	}

	/**
	 * testRenderMyPagePost()
	 */
	@Test
	public void renderMyPagePost()
	{
		// start and render the test page
		tester.startPage(HomePage.class);
		tester.assertRenderedPage(HomePage.class);

		// POST
		tester.submitForm("form1");
		tester.assertRenderedPage(HomePage.class);
	}

	/**
	 * testRenderMyPageGet()
	 */
	@Test
	public void renderMyPageGet()
	{
		// start and render the test page
		tester.startPage(HomePage.class);
		tester.assertRenderedPage(HomePage.class);

		// POST
		tester.submitForm("form2");
		tester.assertRenderedPage(HomePage.class);
	}

	/**
	 * Simple obfuscation crypt for test purposes
	 * 
	 * @author igor.vaynberg
	 */
	private static class TestCryptFactory implements ICryptFactory
	{

		@Override
		public ICrypt newCrypt()
		{
			return new ICrypt()
			{

				@Override
				public String decryptUrlSafe(String text)
				{
					return new String(new Base64(true).decode(text));
				}

				@Override
				public String encryptUrlSafe(String plainText)
				{
					return new String(new Base64(true).encode(plainText.getBytes()));
				}

				@Override
				public void setKey(String key)
				{
				}

			};
		}
	}

}
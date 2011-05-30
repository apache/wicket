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
package org.apache.wicket.request.mapper;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.handler.PageProvider;
import org.apache.wicket.request.handler.RenderPageRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.tester.DummyHomePage;
import org.apache.wicket.util.tester.WicketTester;

/**
 * Tests for {@link CryptoMapper}
 */
public class CryptoMapperTest extends AbstractMapperTest
{
	/**
	 * the encrypted version of {@link #EXPECTED_URL}
	 */
	private static final String ENCRYPTED_URL = "SnPh82L4Kl4/SnPe4/4Sn8e/nPh75/h8211";

	/**
	 * The url to encrypt
	 */
	private static final Url EXPECTED_URL = Url.parse("a/b/c/d");

	private CryptoMapper mapper;

	private WicketTester tester;

	/**
	 * Creates the {@link CryptoMapper}
	 * 
	 * @throws Exception
	 */
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();

		tester = new WicketTester();
		WebApplication webApplication = tester.getApplication();
		webApplication.mountPage(EXPECTED_URL.toString(), DummyHomePage.class);
		mapper = new CryptoMapper(webApplication.getRootRequestMapper(), webApplication);
	}

	@Override
	protected void tearDown() throws Exception
	{
		tester.destroy();

		super.tearDown();
	}

	/**
	 * Tests that {@link CryptoMapper} wraps the original request mapper and encrypts the url
	 * produced by it
	 */
	public void testEncrypt()
	{
		Url url = mapper.mapHandler(new RenderPageRequestHandler(new PageProvider(
			DummyHomePage.class, new PageParameters())));
		assertEquals(ENCRYPTED_URL, url.toString());
	}

	/**
	 * Tests that {@link CryptoMapper} decrypts the passed url and pass it to the original request
	 * mapper which resolves the page from the application mounts
	 */
	public void testDecrypt()
	{
		Request request = getRequest(Url.parse(ENCRYPTED_URL));
		IRequestHandler requestHandler = mapper.mapRequest(request);
		assertTrue(requestHandler instanceof RenderPageRequestHandler);

		RenderPageRequestHandler handler = (RenderPageRequestHandler)requestHandler;
		assertEquals(DummyHomePage.class, handler.getPageClass());
	}

	/**
	 * Tests that named and indexed parameters are properly (en|de)crypted
	 */
	public void testPageParameters()
	{
		String expectedEncrypted = "ywKWg-Qpk7YQBiYCmj9MaAJSIV1gtssNinjiALijtet62VMQc2-sMK_RchttkidUpYM_cplXKeZSfGxBkvWzH_E_zWv4Ii7MNSm5nXKno7o/ywK6c/MK_c0/nji3c/Qpk1b/XKnba/c2-cd?namedKey1=namedValue1&namedKey2=namedValue2";

		PageParameters expectedParameters = new PageParameters();
		expectedParameters.add("namedKey1", "namedValue1");
		expectedParameters.add("namedKey2", "namedValue2");
		expectedParameters.set(0, "indexedValue1");
		expectedParameters.set(1, "indexedValue2");
		RenderPageRequestHandler renderPageRequestHandler = new RenderPageRequestHandler(
			new PageProvider(DummyHomePage.class, expectedParameters));
		Url url = mapper.mapHandler(renderPageRequestHandler);
// System.err.println(url.toString());
		assertEquals(expectedEncrypted, url.toString());

		Request request = getRequest(url);
		IRequestHandler requestHandler = mapper.mapRequest(request);
		assertTrue(requestHandler instanceof RenderPageRequestHandler);

		RenderPageRequestHandler handler = (RenderPageRequestHandler)requestHandler;
		assertEquals(DummyHomePage.class, handler.getPageClass());
		PageParameters actualParameters = handler.getPageParameters();
		assertEquals(expectedParameters, actualParameters);
	}
}

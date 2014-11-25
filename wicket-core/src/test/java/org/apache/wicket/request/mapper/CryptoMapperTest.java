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
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.settings.ISecuritySettings;
import org.apache.wicket.util.IProvider;
import org.apache.wicket.util.crypt.CachingSunJceCryptFactory;
import org.apache.wicket.util.crypt.ICrypt;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.util.tester.DummyHomePage;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
	@Before
	public void before() throws Exception
	{

		tester = new WicketTester();
		WebApplication webApplication = tester.getApplication();
		webApplication.mountPage(EXPECTED_URL.toString(), DummyHomePage.class);
		mapper = new CryptoMapper(webApplication.getRootRequestMapper(),  new IProvider<ICrypt>()
		{
			public ICrypt get()
			{
				return new CachingSunJceCryptFactory(ISecuritySettings.DEFAULT_ENCRYPTION_KEY).newCrypt();
			}
		});
	}

	/**
	 * @throws Exception
	 */
	@After
	public void after() throws Exception
	{
		tester.destroy();
	}

	/**
	 * Tests that {@link CryptoMapper} wraps the original request mapper and encrypts the url
	 * produced by it
	 */
	@Test
	public void encrypt()
	{
		Url url = mapper.mapHandler(new RenderPageRequestHandler(new PageProvider(
			DummyHomePage.class, new PageParameters())));
		assertEquals(ENCRYPTED_URL, url.toString());
	}

	/**
	 * Tests that {@link CryptoMapper} decrypts the passed url and pass it to the original request
	 * mapper which resolves the page from the application mounts
	 */
	@Test
	public void decrypt()
	{
		Request request = getRequest(Url.parse(ENCRYPTED_URL));
		IRequestHandler requestHandler = mapper.mapRequest(request);
		assertTrue(requestHandler instanceof RenderPageRequestHandler);

		RenderPageRequestHandler handler = (RenderPageRequestHandler)requestHandler;
		assertEquals(DummyHomePage.class, handler.getPageClass());
	}

	/**
	 * Verifies that the home page can be reached with non-encrypted query parameters.
	 * https://issues.apache.org/jira/browse/WICKET-4345
	 */
	@Test
	public void decryptHomePageWithNonEncryptedQueryParameters()
	{
		Request request = getRequest(Url.parse("?named1=value1"));
		IRequestHandler requestHandler = mapper.mapRequest(request);
		assertTrue(requestHandler instanceof RenderPageRequestHandler);

		RenderPageRequestHandler handler = (RenderPageRequestHandler)requestHandler;
		assertEquals(tester.getApplication().getHomePage(), handler.getPageClass());
		StringValue queryParam = handler.getPageParameters().get("named1");
		assertEquals("value1", queryParam.toOptionalString());
	}

	/**
	 * Test a failed decrypt, WICKET-4139
	 */
	@Test
	public void decryptFailed()
	{
		String encrypted = "style.css";

		Request request = getRequest(Url.parse(encrypted));

		assertNull(mapper.mapRequest(request));
	}

	/**
	 * Tests that named and indexed parameters are properly (en|de)crypted
	 */
	@Test
	public void pageParameters()
	{
		String expectedEncrypted = "ywKWg-Qpk7YQBiYCmj9MaAJSIV1gtssNinjiALijtet62VMQc2-sMK_RchttkidUpYM_cplXKeZSfGxBkvWzH_E_zWv4Ii7MNSm5nXKno7o/ywK6c/MK_c0/nji3c/Qpk1b/XKnba/c2-cd";

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

	/**
	 * https://issues.apache.org/jira/browse/WICKET-3926
	 */
	@Test
	public void homePageWithParameters()
	{
		String expectedEncrypted = "0lhSFdMIt3yZUNwbtLuXgDePMclxSbks";
		PageParameters expectedParameters = new PageParameters();
		expectedParameters.add("namedKey1", "namedValue1");

		RenderPageRequestHandler renderPageRequestHandler = new RenderPageRequestHandler(
			new PageProvider(tester.getApplication().getHomePage(), expectedParameters));
		Url url = mapper.mapHandler(renderPageRequestHandler);
		assertEquals(expectedEncrypted, url.toString());

		Request request = getRequest(url);
		IRequestHandler requestHandler = mapper.mapRequest(request);
		assertTrue(requestHandler instanceof RenderPageRequestHandler);

		RenderPageRequestHandler handler = (RenderPageRequestHandler)requestHandler;
		assertEquals(tester.getApplication().getHomePage(), handler.getPageClass());
		PageParameters actualParameters = handler.getPageParameters();
		assertEquals(expectedParameters, actualParameters);
	}

	/**
	 * Relative ResourceReferences, WICKET-3514
	 */
	@Test
	public void resourceReference()
	{
		String encrypted = "X5EA-RpmG5-t7GSByiSposVVWJ28fpoU-XgFo7bOPITjbCTT6mLI5l-7b-WJucu-Kc8StVsu-PL5htkbIxuxphv3mYi5-mmkCvkxPsriihj5VPg3naw2fA/X5E87/b-W6b/l-795/Juc97/mG5fa";

		Request request = getRequest(Url.parse(encrypted));

		IRequestHandler requestHandler = mapper.mapRequest(request);

		assertTrue(requestHandler instanceof ResourceReferenceRequestHandler);
		ResourceReferenceRequestHandler handler = (ResourceReferenceRequestHandler)requestHandler;

		assertEquals(getClass(), handler.getResourceReference().getScope());
		assertEquals("crypt/crypt.txt", handler.getResourceReference().getName());
	}

	/**
	 * Relative ResourceReferences, WICKET-3514
	 */
	@Test
	public void resourceReferenceWithModifiedSegments()
	{
		String encrypted = "X5EA-RpmG5-t7GSByiSposVVWJ28fpoU-XgFo7bOPITjbCTT6mLI5l-7b-WJucu-Kc8StVsu-PL5htkbIxuxphv3mYi5-mmkCvkxPsriihj5VPg3naw2fA/X5E87/b-W6b/l-795/Juc97/modified-crypt.txt";

		Request request = getRequest(Url.parse(encrypted));

		IRequestHandler requestHandler = mapper.mapRequest(request);

		assertTrue(requestHandler instanceof ResourceReferenceRequestHandler);
		ResourceReferenceRequestHandler handler = (ResourceReferenceRequestHandler)requestHandler;

		assertEquals(getClass(), handler.getResourceReference().getScope());
		assertEquals("crypt/modified-crypt.txt", handler.getResourceReference().getName());
	}

	/**
	 * Relative ResourceReferences, WICKET-3514
	 */
	@Test
	public void resourceReferenceWithMoreSegments()
	{
		String encrypted = "X5EA-RpmG5-t7GSByiSposVVWJ28fpoU-XgFo7bOPITjbCTT6mLI5l-7b-WJucu-Kc8StVsu-PL5htkbIxuxphv3mYi5-mmkCvkxPsriihj5VPg3naw2fA/X5E87/b-W6b/l-795/Juc97/more/crypt.txt";

		Request request = getRequest(Url.parse(encrypted));

		IRequestHandler requestHandler = mapper.mapRequest(request);

		assertTrue(requestHandler instanceof ResourceReferenceRequestHandler);
		ResourceReferenceRequestHandler handler = (ResourceReferenceRequestHandler)requestHandler;

		assertEquals(getClass(), handler.getResourceReference().getScope());
		assertEquals("crypt/more/crypt.txt", handler.getResourceReference().getName());
	}

	/**
	 * Relative ResourceReferences, WICKET-3514
	 */
	@Test
	public void resourceReferenceWithLessSegments()
	{
		String encrypted = "X5EA-RpmG5-t7GSByiSposVVWJ28fpoU-XgFo7bOPITjbCTT6mLI5l-7b-WJucu-Kc8StVsu-PL5htkbIxuxphv3mYi5-mmkCvkxPsriihj5VPg3naw2fA/X5E87/b-W6b/l-795/less-crypt.txt";

		Request request = getRequest(Url.parse(encrypted));

		IRequestHandler requestHandler = mapper.mapRequest(request);

		assertTrue(requestHandler instanceof ResourceReferenceRequestHandler);
		ResourceReferenceRequestHandler handler = (ResourceReferenceRequestHandler)requestHandler;

		assertEquals(getClass(), handler.getResourceReference().getScope());
		assertEquals("less-crypt.txt", handler.getResourceReference().getName());
	}
}

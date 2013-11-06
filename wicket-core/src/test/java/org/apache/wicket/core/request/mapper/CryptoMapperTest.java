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

import static org.hamcrest.CoreMatchers.instanceOf;

import org.apache.wicket.MockPage;
import org.apache.wicket.core.request.handler.ListenerInterfaceRequestHandler;
import org.apache.wicket.core.request.handler.PageAndComponentProvider;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.core.request.handler.RequestSettingRequestHandler;
import org.apache.wicket.markup.html.link.ILinkListener;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.Url.StringMode;
import org.apache.wicket.request.component.IRequestableComponent;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.UrlResourceReference;
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
		mapper = new CryptoMapper(webApplication.getRootRequestMapper(), webApplication);
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
		assertThat(requestHandler, instanceOf(RequestSettingRequestHandler.class));
		requestHandler = ((RequestSettingRequestHandler)requestHandler).getDelegateHandler();
		assertThat(requestHandler, instanceOf(RenderPageRequestHandler.class));

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
		assertThat(requestHandler, instanceOf(RequestSettingRequestHandler.class));
		requestHandler = ((RequestSettingRequestHandler)requestHandler).getDelegateHandler();
		assertThat(requestHandler, instanceOf(RenderPageRequestHandler.class));

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
		assertThat(requestHandler, instanceOf(RequestSettingRequestHandler.class));
		requestHandler = ((RequestSettingRequestHandler)requestHandler).getDelegateHandler();
		assertThat(requestHandler, instanceOf(RenderPageRequestHandler.class));

		RenderPageRequestHandler handler = (RenderPageRequestHandler)requestHandler;
		assertEquals(DummyHomePage.class, handler.getPageClass());
		PageParameters actualParameters = handler.getPageParameters();
		assertEquals(expectedParameters, actualParameters);
	}

	/**
	 * When the home page url is requested, with parameters, the url will contain only page
	 * parameters. It should not be encrypted, otherwise we get needless redirects.
	 */
	@Test
	public void homePageWithParameters()
	{
		String expectedEncrypted = "?namedKey1=namedValue1";
		PageParameters expectedParameters = new PageParameters();
		expectedParameters.add("namedKey1", "namedValue1");

		RenderPageRequestHandler renderPageRequestHandler = new RenderPageRequestHandler(
			new PageProvider(tester.getApplication().getHomePage(), expectedParameters));
		Url url = mapper.mapHandler(renderPageRequestHandler);
		assertEquals(expectedEncrypted, url.toString());

		Request request = getRequest(url);
		IRequestHandler requestHandler = mapper.mapRequest(request);
		assertThat(requestHandler, instanceOf(RequestSettingRequestHandler.class));
		requestHandler = ((RequestSettingRequestHandler)requestHandler).getDelegateHandler();
		assertThat(requestHandler, instanceOf(RenderPageRequestHandler.class));

		RenderPageRequestHandler handler = (RenderPageRequestHandler)requestHandler;
		assertEquals(tester.getApplication().getHomePage(), handler.getPageClass());
		PageParameters actualParameters = handler.getPageParameters();
		assertEquals(expectedParameters, actualParameters);
	}

	/**
	 * UrlResourceReferences, WICKET-5319
	 */
	@Test
	public void urlResourceReference()
	{
		UrlResourceReference resource = new UrlResourceReference(
			Url.parse("http://wicket.apache.org/"));
		Url url = mapper.mapHandler(new ResourceReferenceRequestHandler(resource));

		assertEquals("http://wicket.apache.org/", url.toString(StringMode.FULL));
	}

	/**
	 * Relative ResourceReferences, WICKET-3514
	 */
	@Test
	public void resourceReference()
	{
		PackageResourceReference resource = new PackageResourceReference(getClass(),
			"crypt/crypt.txt");
		Url url = mapper.mapHandler(new ResourceReferenceRequestHandler(resource));

		Request request = getRequest(url);

		IRequestHandler requestHandler = mapper.mapRequest(request);

		assertThat(requestHandler, instanceOf(RequestSettingRequestHandler.class));
		requestHandler = ((RequestSettingRequestHandler)requestHandler).getDelegateHandler();
		assertThat(requestHandler, instanceOf(ResourceReferenceRequestHandler.class));
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
		PackageResourceReference resource = new PackageResourceReference(getClass(),
			"crypt/crypt.txt");
		Url url = mapper.mapHandler(new ResourceReferenceRequestHandler(resource));
		url.getSegments().remove(url.getSegments().size() - 1);
		url.getSegments().add("modified-crypt.txt");

		Request request = getRequest(url);

		IRequestHandler requestHandler = mapper.mapRequest(request);

		assertThat(requestHandler, instanceOf(RequestSettingRequestHandler.class));
		requestHandler = ((RequestSettingRequestHandler)requestHandler).getDelegateHandler();
		assertThat(requestHandler, instanceOf(ResourceReferenceRequestHandler.class));
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
		PackageResourceReference resource = new PackageResourceReference(getClass(),
			"crypt/crypt.txt");
		Url url = mapper.mapHandler(new ResourceReferenceRequestHandler(resource));
		url.getSegments().remove(url.getSegments().size() - 1);
		url.getSegments().add("more");
		url.getSegments().add("more-crypt.txt");

		Request request = getRequest(url);

		IRequestHandler requestHandler = mapper.mapRequest(request);

		assertThat(requestHandler, instanceOf(RequestSettingRequestHandler.class));
		requestHandler = ((RequestSettingRequestHandler)requestHandler).getDelegateHandler();
		assertThat(requestHandler, instanceOf(ResourceReferenceRequestHandler.class));
		ResourceReferenceRequestHandler handler = (ResourceReferenceRequestHandler)requestHandler;

		assertEquals(getClass(), handler.getResourceReference().getScope());
		assertEquals("crypt/more/more-crypt.txt", handler.getResourceReference().getName());
	}

	/**
	 * Relative ResourceReferences, WICKET-3514
	 */
	@Test
	public void resourceReferenceWithLessSegments()
	{
		PackageResourceReference resource = new PackageResourceReference(getClass(),
			"crypt/crypt.txt");
		Url url = mapper.mapHandler(new ResourceReferenceRequestHandler(resource));
		url.getSegments().remove(url.getSegments().size() - 1);
		url.getSegments().remove(url.getSegments().size() - 1);
		url.getSegments().add("less-crypt.txt");

		Request request = getRequest(url);

		IRequestHandler requestHandler = mapper.mapRequest(request);

		assertThat(requestHandler, instanceOf(RequestSettingRequestHandler.class));
		requestHandler = ((RequestSettingRequestHandler)requestHandler).getDelegateHandler();
		assertThat(requestHandler, instanceOf(ResourceReferenceRequestHandler.class));
		ResourceReferenceRequestHandler handler = (ResourceReferenceRequestHandler)requestHandler;

		assertEquals(getClass(), handler.getResourceReference().getScope());
		assertEquals("less-crypt.txt", handler.getResourceReference().getName());
	}

	/**
	 * Additional parameters, WICKET-4923
	 */
	@Test
	public void additionalParameters()
	{
		MockPage page = new MockPage();
		IRequestableComponent c = page.get("foo:bar");
		PageAndComponentProvider provider = new PageAndComponentProvider(page, c);
		IRequestHandler handler = new ListenerInterfaceRequestHandler(provider,
			ILinkListener.INTERFACE);

		Url url = mapper.mapHandler(handler);
		url.addQueryParameter("q", "foo");

		Request request = getRequest(url);

		IRequestHandler requestHandler = mapper.mapRequest(request);

		assertThat(requestHandler, instanceOf(RequestSettingRequestHandler.class));

		assertEquals("foo", ((RequestSettingRequestHandler)requestHandler).getRequest().getUrl()
			.getQueryParameterValue("q").toString());
	}
}
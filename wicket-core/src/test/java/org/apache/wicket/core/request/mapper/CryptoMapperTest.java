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
import org.apache.wicket.core.request.handler.BookmarkableListenerInterfaceRequestHandler;
import org.apache.wicket.core.request.handler.ListenerInterfaceRequestHandler;
import org.apache.wicket.core.request.handler.PageAndComponentProvider;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.core.request.handler.RequestSettingRequestHandler;
import org.apache.wicket.markup.IMarkupFragment;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.ILinkListener;
import org.apache.wicket.protocol.http.PageExpiredException;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.IRequestHandlerDelegate;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.Url.StringMode;
import org.apache.wicket.request.component.IRequestableComponent;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache.wicket.request.mapper.info.PageComponentInfo;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.UrlResourceReference;
import org.apache.wicket.settings.SecuritySettings;
import org.apache.wicket.util.IProvider;
import org.apache.wicket.util.crypt.CachingSunJceCryptFactory;
import org.apache.wicket.util.crypt.ICrypt;
import org.apache.wicket.util.crypt.ICryptFactory;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link CryptoMapper}
 */
public class CryptoMapperTest extends AbstractMapperTest
{
	private static final String PLAIN_BOOKMARKABLE_URL = "wicket/bookmarkable/" + Page2.class.getName();
	private static final String ENCRYPTED_BOOKMARKABLE_URL = "L7ExSNbPC4sb6TPJDblCAopL53TWmZP5y7BQEaJSJAC05HXod5M5U7gT2yNT0lK5L6L09ZAOoZkGyUhseyPrC4S5tqUUrV6zipc4_Ni877EmwR8AyCyA-A/L7E59/5y7f2";
	private static final String PLAIN_PAGE_INSTANCE_URL = "wicket/page?5";
	private static final String ENCRYPTED_PAGE_INSTANCE_URL = "fyBfZ9p6trOhokHCzsQS6Q/fyBce";
	private static final String MOUNTED_URL = "path/to/mounted/page";

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

		WebApplication application = tester.getApplication();
		application.mountPage(MOUNTED_URL, Page1.class);

		/**
		 * Use explicit crypt provider to prevent crypt warning output, see
		 * SecuritySettings#getCryptFactory()
		 */
		IProvider<ICrypt> cryptProvider = new IProvider<ICrypt>()
		{
			private ICryptFactory cryptFactory = new CachingSunJceCryptFactory(
				SecuritySettings.DEFAULT_ENCRYPTION_KEY);

			@Override
			public ICrypt get()
			{
				return cryptFactory.newCrypt();
			}
		};

		mapper = new CryptoMapper(application.getRootRequestMapper(), cryptProvider);
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
	 * Tests that the home page is requestable.
	 */
	@Test
	public void homePage()
	{
		IRequestHandler requestHandler = mapper.mapRequest(getRequest(Url.parse("")));
		assertNotNull("Unable to map request for home page", requestHandler);
		requestHandler = unwrapRequestHandlerDelegate(requestHandler);
		assertThat(requestHandler, instanceOf(RenderPageRequestHandler.class));
		RenderPageRequestHandler handler = (RenderPageRequestHandler) requestHandler;
		assertSame(tester.getApplication().getHomePage(), handler.getPageClass());
	}

	/**
	 * Verifies that the home page can be reached with non-encrypted query parameters.
	 * https://issues.apache.org/jira/browse/WICKET-4345
	 *
	 * Also, test that the URL for the home page with non-encrypted parameters is not encrypted, to avoid unnecessary redirects.
	 */
	@Test
	public void homePageWithNonEncryptedQueryParameters()
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
		assertNotNull(requestHandler);

		requestHandler = unwrapRequestHandlerDelegate(requestHandler);
		assertThat(requestHandler, instanceOf(RenderPageRequestHandler.class));
		RenderPageRequestHandler handler = (RenderPageRequestHandler) requestHandler;
		assertEquals(tester.getApplication().getHomePage(), handler.getPageClass());
		StringValue queryParam = handler.getPageParameters().get("namedKey1");
		assertEquals("namedValue1", queryParam.toOptionalString());
	}

	/**
	 * Tests that we do not allow unencrypted URLs to IRequestListeners on the home page, like: ?0-0.ILinkListener-link
	 */
	@Test
	public void homePageForceEncryptionOfRequestListener()
	{
		PageAndComponentProvider provider = new PageAndComponentProvider(tester.getApplication().getHomePage(), "some:link");
		IRequestHandler requestHandler = new BookmarkableListenerInterfaceRequestHandler(provider, ILinkListener.INTERFACE);
		Url plainUrl = mapper.getDelegateMapper().mapHandler(requestHandler);
		assertTrue("Plain URL for home page has segments: " + plainUrl.toString(), plainUrl.getSegments().isEmpty());
		assertNull(mapper.mapRequest(getRequest(plainUrl)));
	}

	/**
	 * Tests that URLs for bookmarkable pages are encrypted.
	 */
	@Test
	public void bookmarkablePageEncrypt()
	{
		IRequestHandler renderPage2BookmarkableHandler = new RenderPageRequestHandler(new PageProvider(
			Page2.class, new PageParameters()));

		Url plainTextUrl = mapper.getDelegateMapper().mapHandler(renderPage2BookmarkableHandler);

		assertEquals(PLAIN_BOOKMARKABLE_URL, plainTextUrl.toString());

		Url encryptedUrl = mapper.mapHandler(renderPage2BookmarkableHandler);
		assertEquals(ENCRYPTED_BOOKMARKABLE_URL, encryptedUrl.toString());
	}

	/**
	 * Tests that encrypted URLs for bookmarkable pages are decrypted and passed to the wrapped mapper.
	 */
	@Test
	public void bookmarkablePageDecrypt()
	{
		Request request = getRequest(Url.parse(ENCRYPTED_BOOKMARKABLE_URL));
		IRequestHandler requestHandler = mapper.mapRequest(request);

		assertNotNull(requestHandler);
		requestHandler = unwrapRequestHandlerDelegate(requestHandler);

		assertThat(requestHandler, instanceOf(RenderPageRequestHandler.class));

		RenderPageRequestHandler handler = (RenderPageRequestHandler) requestHandler;
		assertEquals(Page2.class, handler.getPageClass());
	}

	/**
	 * Tests that encrypted URLs for bookmarkable pages are decrypted and passed to the wrapped mapper when there is more than
	 * one cryptomapper installed.
	 */
	@Test
	public void bookmarkablePageDecryptMultipleCryptoMapper()
	{
		Request request = getRequest(Url.parse(ENCRYPTED_BOOKMARKABLE_URL));

		IRequestHandler requestHandler = new CryptoMapper(mapper, tester.getApplication())
			.mapRequest(request);

		assertNotNull(requestHandler);
		requestHandler = unwrapRequestHandlerDelegate(requestHandler);

		assertThat(requestHandler, instanceOf(RenderPageRequestHandler.class));

		RenderPageRequestHandler handler = (RenderPageRequestHandler) requestHandler;
		assertEquals(Page2.class, handler.getPageClass());
	}

	/**
	 * Tests that plain text URLs to bookmarkable pages are not mapped.
	 */
	@Test
	public void bookmarkablePageForceEncryption()
	{
		IRequestHandler requestHandler = mapper.mapRequest(getRequest(Url.parse(PLAIN_BOOKMARKABLE_URL)));
		assertNull(requestHandler);
	}

	/**
	 * Tests that we do not allow unencrypted URLs to IRequestListeners on bookmarkable pages, like:
	 * wicket/bookmarkable/my.package.page?0-0.ILinkListener-link
	 */
	@Test
	public void bookmarkablePageForceEncryptionOfRequestListener()
	{
		PageAndComponentProvider provider = new PageAndComponentProvider(Page2.class, "some:link");
		IRequestHandler requestHandler = new BookmarkableListenerInterfaceRequestHandler(provider, ILinkListener.INTERFACE);
		Url plainUrl = mapper.getDelegateMapper().mapHandler(requestHandler);
		assertTrue("Plain text request listener URL for bookmarkable page does not start with: "
			+ PLAIN_BOOKMARKABLE_URL + ": " + plainUrl.toString(),
			plainUrl.toString().startsWith(PLAIN_BOOKMARKABLE_URL));
		assertNull(mapper.mapRequest(getRequest(plainUrl)));
	}

	/**
	 * Tests that URLs for page instances are encrypted (/wicket/page?5)
	 */
	@Test
	public void pageInstanceEncrypt()
	{
		MockPage page = new MockPage(5);
		IRequestHandler requestHandler = new RenderPageRequestHandler(new PageProvider(page));

		assertEquals(PLAIN_PAGE_INSTANCE_URL, mapper.getDelegateMapper().mapHandler(requestHandler).toString());
		assertEquals(ENCRYPTED_PAGE_INSTANCE_URL, mapper.mapHandler(requestHandler).toString());
	}

	/**
	 * Make sure that encrypted page instance URLs are decrypted and the correct handler resolved.
	 */
	@Test
	public void pageInstanceDecrypt()
	{
		IRequestHandler requestHandler = mapper.mapRequest(getRequest(Url.parse(ENCRYPTED_PAGE_INSTANCE_URL)));

		assertNotNull(requestHandler);
		requestHandler = unwrapRequestHandlerDelegate(requestHandler);

		assertThat(requestHandler, instanceOf(RenderPageRequestHandler.class));
		RenderPageRequestHandler handler = (RenderPageRequestHandler) requestHandler;
		assertEquals(5, handler.getPageId().intValue());
	}

	/**
	 * Make sure that encrypted page instance URLs are decrypted and the correct handler resolved.
	 */
	@Test
	public void pageInstanceDecryptMultipleCryptoMapper()
	{
		IRequestHandler requestHandler = new CryptoMapper(mapper, tester.getApplication())
			.mapRequest(getRequest(Url.parse(ENCRYPTED_PAGE_INSTANCE_URL)));

		assertNotNull(requestHandler);
		requestHandler = unwrapRequestHandlerDelegate(requestHandler);

		assertThat(requestHandler, instanceOf(RenderPageRequestHandler.class));
		RenderPageRequestHandler handler = (RenderPageRequestHandler) requestHandler;
		assertEquals(5, handler.getPageId().intValue());
	}

	/**
	 * Tests that plain text requests to a page instance URL are not mapped.
	 */
	@Test
	public void pageInstanceForceEncryption()
	{
		assertNull(mapper.mapRequest(getRequest(Url.parse(PLAIN_PAGE_INSTANCE_URL))));
	}

	/**
	 * Tests that mounted pages are still accessible through their mounted URL.
	 */
	@Test
	public void mountedPage()
	{
		IRequestHandler requestHandler = new RenderPageRequestHandler(new PageProvider(Page1.class));

		assertEquals(MOUNTED_URL, mapper.mapHandler(requestHandler).toString());

		requestHandler = mapper.mapRequest(getRequest(Url.parse(MOUNTED_URL)));

		assertNotNull(requestHandler);

		requestHandler = unwrapRequestHandlerDelegate(requestHandler);

		assertThat(requestHandler, instanceOf(RenderPageRequestHandler.class));

		assertEquals(Page1.class, ((RenderPageRequestHandler) requestHandler).getPageClass());
	}

	/**
	 * Tests that PageComponentInfo parameters are encrypted on Mounted pages
	 */
	@Test
	public void mountedPageRequestListenerParameter()
	{
		final String componentPath = "some:path:to:link";

		PageAndComponentProvider provider = new PageAndComponentProvider(Page1.class, componentPath);
		IRequestHandler requestHandler = new ListenerInterfaceRequestHandler(provider, ILinkListener.INTERFACE);

		Url plainUrl = mapper.getDelegateMapper().mapHandler(requestHandler);
		assertTrue(plainUrl.toString().startsWith(MOUNTED_URL));

		/*
		 * Do not allow unencrypted request listener urls to mounted pages.
		 */
		assertNull(mapper.mapRequest(getRequest(plainUrl)));

		/*
		 * Test encryption of request listener parameter.
		 */
		Url encryptedUrl = mapper.mapHandler(requestHandler);

		assertEquals(Url.parse(MOUNTED_URL).getSegments(), encryptedUrl.getSegments());
		assertTrue(encryptedUrl.getQueryParameters().size() > 0);

		for (Url.QueryParameter qp : encryptedUrl.getQueryParameters())
		{
			if (Strings.isEmpty(qp.getValue()))
			{
				PageComponentInfo pci = PageComponentInfo.parse(qp.getName());
				assertNull("PageComponentInfo query parameter not encrypted", pci);
			}
		}

		requestHandler = mapper.mapRequest(getRequest(encryptedUrl));

		assertNotNull(requestHandler);

		requestHandler = unwrapRequestHandlerDelegate(requestHandler);

		assertThat(requestHandler, instanceOf(ListenerInterfaceRequestHandler.class));

		ListenerInterfaceRequestHandler handler = (ListenerInterfaceRequestHandler) requestHandler;
		assertEquals(componentPath, handler.getComponentPath());
		assertEquals(Page1.class, handler.getPageClass());

		/*
		 * We anticipate that sometimes multiple cryptomappers will be used. It should still work in these situations.
		 */
		requestHandler = new CryptoMapper(mapper, tester.getApplication())
			.mapRequest(getRequest(encryptedUrl));

		assertNotNull(requestHandler);

		requestHandler = unwrapRequestHandlerDelegate(requestHandler);

		assertThat(requestHandler, instanceOf(ListenerInterfaceRequestHandler.class));

		handler = (ListenerInterfaceRequestHandler) requestHandler;
		assertEquals(componentPath, handler.getComponentPath());
		assertEquals(Page1.class, handler.getPageClass());
	}

	/**
	 * Tests that the compatability score is correctly calculated from wrapped mapper.
	 */
	@Test
	public void compatabilityScore()
	{
		int delegateHomePageScore = mapper.getDelegateMapper().getCompatibilityScore(
			getRequest(Url.parse("")));
		int cryptoHomePageScore = mapper.getCompatibilityScore(
			getRequest(Url.parse("")));
		assertEquals(delegateHomePageScore, cryptoHomePageScore);

		int delegateBookmarkableScore = mapper.getDelegateMapper().getCompatibilityScore(
			getRequest(Url.parse(PLAIN_BOOKMARKABLE_URL)));
		int cryptoBookmarkableScore = mapper.getCompatibilityScore(
			getRequest(Url.parse(ENCRYPTED_BOOKMARKABLE_URL)));
		assertEquals(delegateBookmarkableScore, cryptoBookmarkableScore);

		int delegatePageInstanceScore = mapper.getDelegateMapper().getCompatibilityScore(
			getRequest(Url.parse(PLAIN_PAGE_INSTANCE_URL)));
		int cryptoPageInstanceScore = mapper.getCompatibilityScore(
			getRequest(Url.parse(ENCRYPTED_PAGE_INSTANCE_URL)));
		assertEquals(delegatePageInstanceScore, cryptoPageInstanceScore);
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
		String expectedEncrypted = "L7ExSNbPC4sb6TPJDblCAopL53TWmZP5y7BQEaJSJAC05HXod5M5U7gT2yNT0lK5L6L09ZAOoZkGyUhseyPrC4S5tqUUrV6zipc4_Ni877FDOOoE5C_Cd7YCyK1xSScpVhno6LeBz2wiu5oWyf7hB1RKcv6zkhEBmbx8vU7K7-e4xe1_LO8Y3fhEjMSQyU9BVh7Uz4HKzkR2OxFo5LaDzQ/L7E59/yPr6a/5L6ae/OxF2c";

		PageParameters expectedParameters = new PageParameters();
		expectedParameters.add("namedKey1", "namedValue1");
		expectedParameters.add("namedKey2", "namedValue2");
		expectedParameters.set(0, "indexedValue1");
		expectedParameters.set(1, "indexedValue2");
		RenderPageRequestHandler renderPageRequestHandler = new RenderPageRequestHandler(
			new PageProvider(Page2.class, expectedParameters));
		Url url = mapper.mapHandler(renderPageRequestHandler);
		assertEquals(expectedEncrypted, url.toString());

		Request request = getRequest(url);
		IRequestHandler requestHandler = mapper.mapRequest(request);
		assertNotNull(requestHandler);
		requestHandler = unwrapRequestHandlerDelegate(requestHandler);
		assertThat(requestHandler, instanceOf(RenderPageRequestHandler.class));

		RenderPageRequestHandler handler = (RenderPageRequestHandler) requestHandler;
		assertEquals(Page2.class, handler.getPageClass());
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

		assertNotNull(requestHandler);
		requestHandler = unwrapRequestHandlerDelegate(requestHandler);
		assertThat(requestHandler, instanceOf(ResourceReferenceRequestHandler.class));
		ResourceReferenceRequestHandler handler = (ResourceReferenceRequestHandler) requestHandler;

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

		assertNotNull(requestHandler);
		requestHandler = unwrapRequestHandlerDelegate(requestHandler);
		assertThat(requestHandler, instanceOf(ResourceReferenceRequestHandler.class));
		ResourceReferenceRequestHandler handler = (ResourceReferenceRequestHandler) requestHandler;

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

		assertNotNull(requestHandler);
		requestHandler = unwrapRequestHandlerDelegate(requestHandler);
		ResourceReferenceRequestHandler handler = (ResourceReferenceRequestHandler) requestHandler;

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

		assertNotNull(requestHandler);
		requestHandler = unwrapRequestHandlerDelegate(requestHandler);
		assertThat(requestHandler, instanceOf(ResourceReferenceRequestHandler.class));
		ResourceReferenceRequestHandler handler = (ResourceReferenceRequestHandler) requestHandler;

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

		assertEquals("foo", ((RequestSettingRequestHandler) requestHandler).getRequest()
			.getUrl()
			.getQueryParameterValue("q")
			.toString());
	}

	@Test
	public void markedEncryptedUrlDecrypt()
	{
		mapper.setMarkEncryptedUrls(true);
		Request request = getRequest(Url.parse("crypt." + ENCRYPTED_BOOKMARKABLE_URL));
		IRequestHandler requestHandler = mapper.mapRequest(request);

		assertNotNull(requestHandler);
		requestHandler = unwrapRequestHandlerDelegate(requestHandler);

		assertTrue(requestHandler instanceof RenderPageRequestHandler);

		RenderPageRequestHandler handler = (RenderPageRequestHandler) requestHandler;
		assertEquals(Page2.class, handler.getPageClass());
	}

	@Test(expected = PageExpiredException.class)
	public void expiredMarkedEncryptedUrlThrowsPageExpiredException()
	{
		mapper.setMarkEncryptedUrls(true);
		Url encryptedUrl = mapper.mapHandler(new RenderPageRequestHandler(new PageProvider(Page2.class)));
		assertTrue(encryptedUrl.getSegments().get(0).startsWith("crypt."));
		encryptedUrl.getSegments().remove(0);
		encryptedUrl.getSegments().add(0, "crypt.no decryptable");
		mapper.mapRequest(getRequest(encryptedUrl));
	}

	private static IRequestHandler unwrapRequestHandlerDelegate(IRequestHandler handler)
	{
		while (handler instanceof IRequestHandlerDelegate)
		{
			handler = ((IRequestHandlerDelegate) handler).getDelegateHandler();
		}

		return handler;
	}

	/**
	 * Page that is mounted
	 */
	public static class Page1 extends WebPage
	{
		@Override
		public IMarkupFragment getMarkup()
		{
			return Markup.of("<html><body></body></html>");
		}
	}

	/**
	 * Page that is not mounted
	 */
	public static class Page2 extends WebPage
	{
		@Override
		public IMarkupFragment getMarkup()
		{
			return Markup.of("<html><body></body></html>");
		}
	}
}

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

import java.io.Serializable;
import java.util.Locale;

import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache.wicket.request.mapper.parameter.INamedParameters;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.mapper.parameter.PageParametersEncoder;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.request.resource.caching.FilenameWithVersionResourceCachingStrategy;
import org.apache.wicket.request.resource.caching.IResourceCachingStrategy;
import org.apache.wicket.request.resource.caching.IStaticCacheableResource;
import org.apache.wicket.request.resource.caching.NoOpResourceCachingStrategy;
import org.apache.wicket.request.resource.caching.ResourceUrl;
import org.apache.wicket.request.resource.caching.version.StaticResourceVersion;
import org.apache.wicket.util.IProvider;
import org.apache.wicket.util.ValueProvider;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.junit.Test;

/**
 * @author Matej Knopp
 */
public class BasicResourceReferenceMapperTest extends AbstractResourceReferenceMapperTest
{
	private static final IProvider<IResourceCachingStrategy> NO_CACHING = new ValueProvider<IResourceCachingStrategy>(
		NoOpResourceCachingStrategy.INSTANCE);

	private final BasicResourceReferenceMapper encoder = new BasicResourceReferenceMapper(
		new PageParametersEncoder(), NO_CACHING)
	{
		@Override
		protected IMapperContext getContext()
		{
			return context;
		}
	};

	/**
	 *
	 */
	@Test
	public void decode1()
	{
		Url url = Url.parse("wicket/resource/" + CLASS_NAME + "/reference1");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));
		assertTrue(handler instanceof ResourceReferenceRequestHandler);
		ResourceReferenceRequestHandler h = (ResourceReferenceRequestHandler)handler;
		assertEquals(resource1, h.getResource());
		assertEquals(0, h.getPageParameters().getIndexedCount());
		assertEquals(0, h.getPageParameters().getNamedKeys().size());
	}

	/**
	 *
	 */
	@Test
	public void decode1A()
	{
		Url url = Url.parse("wicket/resource/" + CLASS_NAME + "/reference1?en");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));
		assertTrue(handler instanceof ResourceReferenceRequestHandler);
		ResourceReferenceRequestHandler h = (ResourceReferenceRequestHandler)handler;
		assertEquals(resource1, h.getResource());
		assertEquals(Locale.ENGLISH, h.getLocale());
		assertEquals(null, h.getStyle());
		assertEquals(null, h.getVariation());
		assertEquals(0, h.getPageParameters().getIndexedCount());
		assertEquals(0, h.getPageParameters().getNamedKeys().size());
	}

	/**
	 *
	 */
	@Test
	public void decode2()
	{
		Url url = Url.parse("wicket/resource/" + CLASS_NAME + "/reference1?p1=v1&p2=v2");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));
		assertTrue(handler instanceof ResourceReferenceRequestHandler);
		ResourceReferenceRequestHandler h = (ResourceReferenceRequestHandler)handler;
		assertEquals(resource1, h.getResource());
		assertEquals(null, h.getLocale());
		assertEquals(null, h.getStyle());
		assertEquals(null, h.getVariation());
		assertEquals(0, h.getPageParameters().getIndexedCount());
		assertEquals("v1", h.getPageParameters().get("p1").toString());
		assertEquals("v2", h.getPageParameters().get("p2").toString());
	}

	/**
	 *
	 */
	@Test
	public void decode2A()
	{
		Url url = Url.parse("wicket/resource/" + CLASS_NAME + "/reference1?-style&p1=v1&p2=v2");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));
		assertTrue(handler instanceof ResourceReferenceRequestHandler);
		ResourceReferenceRequestHandler h = (ResourceReferenceRequestHandler)handler;
		assertEquals(resource1, h.getResource());
		assertEquals(null, h.getLocale());
		assertEquals("style", h.getStyle());
		assertEquals(null, h.getVariation());
		assertEquals(0, h.getPageParameters().getIndexedCount());
		assertEquals("v1", h.getPageParameters().get("p1").toString());
		assertEquals("v2", h.getPageParameters().get("p2").toString());
	}

	/**
	 *
	 */
	@Test
	public void decode3()
	{
		Url url = Url.parse("wicket/resource/" + CLASS_NAME + "/reference2/name2?en_EN");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));
		assertTrue(handler instanceof ResourceReferenceRequestHandler);
		ResourceReferenceRequestHandler h = (ResourceReferenceRequestHandler)handler;
		assertEquals(resource2, h.getResource());
		assertEquals(new Locale("en", "en"), h.getLocale());
		assertEquals(null, h.getStyle());
		assertEquals(null, h.getVariation());
		assertEquals(0, h.getPageParameters().getIndexedCount());
		assertEquals(0, h.getPageParameters().getNamedKeys().size());
	}

	/**
	 *
	 */
	@Test
	public void decode3A()
	{
		Url url = Url.parse("wicket/resource/" + CLASS_NAME + "/reference2/name2?en_EN-style");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));
		assertTrue(handler instanceof ResourceReferenceRequestHandler);
		ResourceReferenceRequestHandler h = (ResourceReferenceRequestHandler)handler;
		assertEquals(resource2, h.getResource());
		assertEquals(new Locale("en", "en"), h.getLocale());
		assertEquals("style", h.getStyle());
		assertEquals(null, h.getVariation());
		assertEquals(0, h.getPageParameters().getIndexedCount());
		assertEquals(0, h.getPageParameters().getNamedKeys().size());
	}

	/**
	 *
	 */
	@Test
	public void decode3B()
	{
		Url url = Url.parse("wicket/resource/" + CLASS_NAME + "/reference2/name2");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));
		assertNull(handler);
	}

	/**
	 *
	 */
	@Test
	public void decode4()
	{
		Url url = Url.parse("wicket/resource/" + CLASS_NAME + "/reference2/name2?en_EN&p1=v1&p2=v2");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));
		assertTrue(handler instanceof ResourceReferenceRequestHandler);
		ResourceReferenceRequestHandler h = (ResourceReferenceRequestHandler)handler;
		assertEquals(resource2, h.getResource());
		assertEquals(new Locale("en", "en"), h.getLocale());
		assertEquals(null, h.getStyle());
		assertEquals(null, h.getVariation());
		assertEquals("v1", h.getPageParameters().get("p1").toString());
		assertEquals("v2", h.getPageParameters().get("p2").toString());
	}

	/**
	 *
	 */
	@Test
	public void decode5()
	{
		Url url = Url.parse("wicket/resource/" + CLASS_NAME + "/reference3?-style");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));
		assertTrue(handler instanceof ResourceReferenceRequestHandler);
		ResourceReferenceRequestHandler h = (ResourceReferenceRequestHandler)handler;
		assertEquals(resource3, h.getResource());
		assertEquals(null, h.getLocale());
		assertEquals("style", h.getStyle());
		assertEquals(null, h.getVariation());
		assertEquals(0, h.getPageParameters().getIndexedCount());
		assertEquals(0, h.getPageParameters().getNamedKeys().size());
	}

	/**
	 *
	 */
	@Test
	public void decode6()
	{
		Url url = Url.parse("wicket/resource/" + CLASS_NAME + "/reference3?-style&p1=v1&p2=v2");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));
		assertTrue(handler instanceof ResourceReferenceRequestHandler);
		ResourceReferenceRequestHandler h = (ResourceReferenceRequestHandler)handler;
		assertEquals(resource3, h.getResource());
		assertEquals(null, h.getLocale());
		assertEquals("style", h.getStyle());
		assertEquals(null, h.getVariation());
		assertEquals(0, h.getPageParameters().getIndexedCount());
		assertEquals("v1", h.getPageParameters().get("p1").toString());
		assertEquals("v2", h.getPageParameters().get("p2").toString());
	}


	/**
	 *
	 */
	@Test
	public void decode7()
	{
		Url url = Url.parse("wicket/resource/" + CLASS_NAME + "/reference4?en-style");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));
		assertTrue(handler instanceof ResourceReferenceRequestHandler);
		ResourceReferenceRequestHandler h = (ResourceReferenceRequestHandler)handler;
		assertEquals(resource4, h.getResource());
		assertEquals(Locale.ENGLISH, h.getLocale());
		assertEquals("style", h.getStyle());
		assertEquals(null, h.getVariation());
		assertEquals(0, h.getPageParameters().getIndexedCount());
		assertEquals(0, h.getPageParameters().getNamedKeys().size());
	}

	/**
	 *
	 */
	@Test
	public void decode7A()
	{
		Url url = Url.parse("wicket/resource/" + CLASS_NAME + "/reference4?sk");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));
		assertNull(handler);
	}

	/**
	 *
	 */
	@Test
	public void decode8()
	{
		Url url = Url.parse("wicket/resource/" + CLASS_NAME + "/reference4?en-style&p1=v1&p2=v2");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));
		assertTrue(handler instanceof ResourceReferenceRequestHandler);
		ResourceReferenceRequestHandler h = (ResourceReferenceRequestHandler)handler;
		assertEquals(resource4, h.getResource());
		assertEquals(Locale.ENGLISH, h.getLocale());
		assertEquals("style", h.getStyle());
		assertEquals(null, h.getVariation());
		assertEquals(0, h.getPageParameters().getIndexedCount());
		assertEquals("v1", h.getPageParameters().get("p1").toString());
		assertEquals("v2", h.getPageParameters().get("p2").toString());
	}

	/**
	 *
	 */
	@Test
	public void decode9()
	{
		Url url = Url.parse("wicket/resource/" + CLASS_NAME +
			"/reference5?en--variation&p1=v1&p2=v2");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));
		assertTrue(handler instanceof ResourceReferenceRequestHandler);
		ResourceReferenceRequestHandler h = (ResourceReferenceRequestHandler)handler;
		assertEquals(resource5, h.getResource());
		assertEquals(Locale.ENGLISH, h.getLocale());
		assertEquals(null, h.getStyle());
		assertEquals("variation", h.getVariation());
		assertEquals(0, h.getPageParameters().getIndexedCount());
		assertEquals("v1", h.getPageParameters().get("p1").toString());
		assertEquals("v2", h.getPageParameters().get("p2").toString());
	}

	/**
	 *
	 */
	@Test
	public void decode10()
	{
		Url url = Url.parse("wicket/resource/" + CLASS_NAME +
			"/reference6?en-style-variation&p1=v1&p2=v2");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));
		assertTrue(handler instanceof ResourceReferenceRequestHandler);
		ResourceReferenceRequestHandler h = (ResourceReferenceRequestHandler)handler;
		assertEquals(resource6, h.getResource());
		assertEquals(Locale.ENGLISH, h.getLocale());
		assertEquals("style", h.getStyle());
		assertEquals("variation", h.getVariation());
		assertEquals(0, h.getPageParameters().getIndexedCount());
		assertEquals("v1", h.getPageParameters().get("p1").toString());
		assertEquals("v2", h.getPageParameters().get("p2").toString());
	}

	/**
	 *
	 */
	@Test
	public void encode1()
	{
		ResourceReferenceRequestHandler handler = new ResourceReferenceRequestHandler(reference1,
			null);
		Url url = encoder.mapHandler(handler);
		assertEquals("wicket/resource/" + CLASS_NAME + "/reference1", url.toString());
	}

	/**
	 *
	 */
	@Test
	public void encode2()
	{
		PageParameters parameters = new PageParameters();
		parameters.set(0, "X");
		parameters.add("p1", "v1");
		parameters.add("p2", "v2");
		ResourceReferenceRequestHandler handler = new ResourceReferenceRequestHandler(reference1,
			parameters);

		Url url = encoder.mapHandler(handler);
		assertEquals("wicket/resource/" + CLASS_NAME + "/reference1?p1=v1&p2=v2", url.toString());
	}

	/**
	 *
	 */
	@Test
	public void encode3()
	{
		ResourceReferenceRequestHandler handler = new ResourceReferenceRequestHandler(reference2,
			null);
		Url url = encoder.mapHandler(handler);
		assertEquals("wicket/resource/" + CLASS_NAME + "/reference2/name2?en_EN", url.toString());
	}

	/**
	 *
	 */
	@Test
	public void encode4()
	{
		PageParameters parameters = new PageParameters();
		parameters.set(0, "X");
		parameters.add("p1", "v1");
		parameters.add("p2", "v2");
		ResourceReferenceRequestHandler handler = new ResourceReferenceRequestHandler(reference2,
			parameters);

		Url url = encoder.mapHandler(handler);
		assertEquals("wicket/resource/" + CLASS_NAME + "/reference2/name2?en_EN&p1=v1&p2=v2",
				url.toString());
	}

	/**
	 *
	 */
	@Test
	public void encode5()
	{
		ResourceReferenceRequestHandler handler = new ResourceReferenceRequestHandler(reference3,
			null);
		Url url = encoder.mapHandler(handler);
		assertEquals("wicket/resource/" + CLASS_NAME + "/reference3?-style", url.toString());
	}

	/**
	 *
	 */
	@Test
	public void encode6()
	{
		PageParameters parameters = new PageParameters();
		parameters.set(0, "X");
		parameters.add("p1", "v1");
		parameters.add("p2", "v2");
		ResourceReferenceRequestHandler handler = new ResourceReferenceRequestHandler(reference3,
			parameters);

		Url url = encoder.mapHandler(handler);
		assertEquals("wicket/resource/" + CLASS_NAME + "/reference3?-style&p1=v1&p2=v2",
				url.toString());
	}

	/**
	 *
	 */
	@Test
	public void encode7()
	{
		ResourceReferenceRequestHandler handler = new ResourceReferenceRequestHandler(reference4,
			null);
		Url url = encoder.mapHandler(handler);
		assertEquals("wicket/resource/" + CLASS_NAME + "/reference4?en-style", url.toString());
	}

	/**
	 *
	 */
	@Test
	public void encode8()
	{
		PageParameters parameters = new PageParameters();
		parameters.set(0, "X");
		parameters.add("p1", "v1");
		parameters.add("p2", "v2");
		ResourceReferenceRequestHandler handler = new ResourceReferenceRequestHandler(reference4,
			parameters);

		Url url = encoder.mapHandler(handler);
		assertEquals("wicket/resource/" + CLASS_NAME + "/reference4?en-style&p1=v1&p2=v2",
				url.toString());
	}

	/**
	 * Tests request to url encoding when style is null but variation is not
	 */
	@Test
	public void encode9()
	{
		ResourceReferenceRequestHandler handler = new ResourceReferenceRequestHandler(reference5,
			null);

		Url url = encoder.mapHandler(handler);
		assertEquals("wicket/resource/" + CLASS_NAME + "/reference5?en--variation", url.toString());
	}

	/**
	 *
	 */
	@Test
	public void versionStringInResourceFilename()
	{
		final IStaticCacheableResource resource = new IStaticCacheableResource()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public Serializable getCacheKey()
			{
				return null;
			}

			@Override
			public IResourceStream getCacheableResourceStream()
			{
				return new StringResourceStream("foo-bar");
			}

			@Override
			public void respond(Attributes attributes)
			{
			}

			@Override
			public boolean isCachingEnabled()
			{
				return true;
			}
		};

		final ResourceReference reference = new ResourceReference(getClass(), "versioned",
			Locale.ENGLISH, "style", null)
		{
			private static final long serialVersionUID = 1L;

			@Override
			public IResource getResource()
			{
				return resource;
			}
		};

		IResourceCachingStrategy strategy = new FilenameWithVersionResourceCachingStrategy(
			"-version-", new StaticResourceVersion("foobar"));

		INamedParameters params = new PageParameters();
		ResourceUrl url = new ResourceUrl("test.js", params);
		strategy.decorateUrl(url, resource);
		assertEquals("test-version-foobar.js", url.getFileName());
		strategy.undecorateUrl(url);
		assertEquals("test.js", url.getFileName());

		url = new ResourceUrl("test", params);
		strategy.decorateUrl(url, resource);
		assertEquals("test-version-foobar", url.getFileName());
		strategy.undecorateUrl(url);
		assertEquals("test", url.getFileName());

		// this behavior is o.k. since a browser could request an
		// previous version of the resource. for example we
		// could first have 'test-alpha.txt' which would be later replaced
		// by 'test-beta.txt' but in any case will point to
		// internal resource 'test.txt'
		url = new ResourceUrl("test-version-older.txt", params);
		strategy.undecorateUrl(url);
		assertEquals("test.txt", url.getFileName());

		// weird but valid
		url = new ResourceUrl("test-version-.txt", params);
		strategy.undecorateUrl(url);
		assertEquals("test.txt", url.getFileName());

		// weird but valid
		url = new ResourceUrl("test-version--------", params);
		strategy.undecorateUrl(url);
		assertEquals("test", url.getFileName());

		// weird but valid
		url = new ResourceUrl("test-version-1.0.3-alpha.txt", params);
		strategy.undecorateUrl(url);
		assertEquals("test.txt", url.getFileName());

		// check a version that contains a dot which also marks the filename extension
		strategy = new FilenameWithVersionResourceCachingStrategy("-version-",
			new StaticResourceVersion("1.0.4-beta"));
		url = new ResourceUrl("test.txt", params);
		strategy.decorateUrl(url, resource);
		assertEquals("test-version-1.0.4-beta.txt", url.getFileName());
	}

	/**
	 *
	 */
	@Test
	public void requestWithEmptyFilename()
	{
		Url url = Url.parse("wicket/resource/" + CLASS_NAME + "/");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));
		assertNull(handler);
	}

	/**
	 * Tests <a href="https://issues.apache.org/jira/browse/WICKET-3918">WICKET-3918</a>.
	 */
	@Test
	public void wicket3918()
	{
		Url url = Url.parse("wicket/resource/org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow/res/");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));
		assertNull(handler);
	}
}

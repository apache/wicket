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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.Serializable;
import java.util.Locale;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache.wicket.request.mapper.parameter.INamedParameters;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.mapper.parameter.PageParametersEncoder;
import org.apache.wicket.request.resource.caching.FilenameWithVersionResourceCachingStrategy;
import org.apache.wicket.request.resource.caching.IResourceCachingStrategy;
import org.apache.wicket.request.resource.caching.IStaticCacheableResource;
import org.apache.wicket.request.resource.caching.NoOpResourceCachingStrategy;
import org.apache.wicket.request.resource.caching.ResourceUrl;
import org.apache.wicket.request.resource.caching.version.IResourceVersion;
import org.apache.wicket.request.resource.caching.version.StaticResourceVersion;
import org.apache.wicket.util.ValueProvider;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.junit.jupiter.api.Test;

/**
 * @author Matej Knopp
 */
class BasicResourceReferenceMapperTest extends AbstractResourceReferenceMapperTest
{
	private static final Supplier<IResourceCachingStrategy> NO_CACHING = new ValueProvider<>(
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
	void decode1()
	{
		Url url = Url.parse("wicket/resource/" + CLASS_NAME + "/reference1");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));
		assertThat(handler).isInstanceOf(ResourceReferenceRequestHandler.class);
		ResourceReferenceRequestHandler h = (ResourceReferenceRequestHandler)handler;
		assertEquals(resource1, h.getResource());
		assertEquals(0, h.getPageParameters().getIndexedCount());
		assertEquals(0, h.getPageParameters().getNamedKeys().size());
	}

	/**
	 *
	 */
	@Test
	void decode1A()
	{
		Url url = Url.parse("wicket/resource/" + CLASS_NAME + "/reference1?2~ennullnull");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));
		// assertThat(handler).isInstanceOf(ResourceReferenceRequestHandler.class); // TODO use hamcrest or assertj
		ResourceReferenceRequestHandler h = (ResourceReferenceRequestHandler)handler;
		assertEquals(resource1, h.getResource());
		assertEquals(Locale.ENGLISH, h.getLocale());
		assertNull(h.getStyle());
		assertNull(h.getVariation());
		assertEquals(0, h.getPageParameters().getIndexedCount());
		assertEquals(0, h.getPageParameters().getNamedKeys().size());
	}

	/**
	 *
	 */
	@Test
	void decode2()
	{
		Url url = Url.parse("wicket/resource/" + CLASS_NAME + "/reference1?p1=v1&p2=v2");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));
		assertThat(handler).isInstanceOf(ResourceReferenceRequestHandler.class);
		ResourceReferenceRequestHandler h = (ResourceReferenceRequestHandler)handler;
		assertEquals(resource1, h.getResource());
		assertNull(h.getLocale());
		assertNull(h.getStyle());
		assertNull(h.getVariation());
		assertEquals(0, h.getPageParameters().getIndexedCount());
		assertEquals("v1", h.getPageParameters().get("p1").toString());
		assertEquals("v2", h.getPageParameters().get("p2").toString());
	}

	/**
	 *
	 */
	@Test
	void decode2A()
	{
		Url url = Url.parse("wicket/resource/" + CLASS_NAME + "/reference1?null5~stylenull&p1=v1&p2=v2");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));
		assertThat(handler).isInstanceOf(ResourceReferenceRequestHandler.class);
		ResourceReferenceRequestHandler h = (ResourceReferenceRequestHandler)handler;
		assertEquals(resource1, h.getResource());
		assertNull(h.getLocale());
		assertEquals("style", h.getStyle());
		assertNull(h.getVariation());
		assertEquals(0, h.getPageParameters().getIndexedCount());
		assertEquals("v1", h.getPageParameters().get("p1").toString());
		assertEquals("v2", h.getPageParameters().get("p2").toString());
	}

	/**
	 *
	 */
	@Test
	void decode3()
	{
		Url url = Url.parse("wicket/resource/" + CLASS_NAME + "/reference2/name2?5~en_ENnullnull");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));
		assertThat(handler).isInstanceOf(ResourceReferenceRequestHandler.class);
		assertThat(handler).isInstanceOf(ResourceReferenceRequestHandler.class);
		ResourceReferenceRequestHandler h = (ResourceReferenceRequestHandler)handler;
		assertEquals(resource2, h.getResource());
		assertEquals(new Locale("en", "en"), h.getLocale());
		assertNull(h.getStyle());
		assertNull(h.getVariation());
		assertEquals(0, h.getPageParameters().getIndexedCount());
		assertEquals(0, h.getPageParameters().getNamedKeys().size());
	}

	/**
	 *
	 */
	@Test
	void decode3A()
	{
		Url url = Url.parse("wicket/resource/" + CLASS_NAME + "/reference2/name2?5~en_EN5~stylenull");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));
		assertThat(handler).isInstanceOf(ResourceReferenceRequestHandler.class);
		ResourceReferenceRequestHandler h = (ResourceReferenceRequestHandler)handler;
		assertEquals(resource2, h.getResource());
		assertEquals(new Locale("en", "en"), h.getLocale());
		assertEquals("style", h.getStyle());
		assertNull(h.getVariation());
		assertEquals(0, h.getPageParameters().getIndexedCount());
		assertEquals(0, h.getPageParameters().getNamedKeys().size());
	}

	/**
	 *
	 */
	@Test
	void decode3B()
	{
		Url url = Url.parse("wicket/resource/" + CLASS_NAME + "/reference2/name2");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));
		assertNull(handler);
	}

	/**
	 *
	 */
	@Test
	void decode4()
	{
		Url url = Url
			.parse("wicket/resource/" + CLASS_NAME + "/reference2/name2?5~en_ENnullnull&p1=v1&p2=v2");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));
		assertThat(handler).isInstanceOf(ResourceReferenceRequestHandler.class);
		ResourceReferenceRequestHandler h = (ResourceReferenceRequestHandler)handler;
		assertEquals(resource2, h.getResource());
		assertEquals(new Locale("en", "en"), h.getLocale());
		assertNull(h.getStyle());
		assertNull(h.getVariation());
		assertEquals("v1", h.getPageParameters().get("p1").toString());
		assertEquals("v2", h.getPageParameters().get("p2").toString());
	}

	/**
	 *
	 */
	@Test
	void decode5()
	{
		Url url = Url.parse("wicket/resource/" + CLASS_NAME + "/reference3?null5~stylenull");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));
		assertThat(handler).isInstanceOf(ResourceReferenceRequestHandler.class);
		ResourceReferenceRequestHandler h = (ResourceReferenceRequestHandler)handler;
		assertEquals(resource3, h.getResource());
		assertNull(h.getLocale());
		assertEquals("style", h.getStyle());
		assertNull(h.getVariation());
		assertEquals(0, h.getPageParameters().getIndexedCount());
		assertEquals(0, h.getPageParameters().getNamedKeys().size());
	}

	/**
	 *
	 */
	@Test
	void decode6()
	{
		Url url = Url.parse("wicket/resource/" + CLASS_NAME + "/reference3?null5~stylenull&p1=v1&p2=v2");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));
		assertThat(handler).isInstanceOf(ResourceReferenceRequestHandler.class);
		ResourceReferenceRequestHandler h = (ResourceReferenceRequestHandler)handler;
		assertEquals(resource3, h.getResource());
		assertNull(h.getLocale());
		assertEquals("style", h.getStyle());
		assertNull(h.getVariation());
		assertEquals(0, h.getPageParameters().getIndexedCount());
		assertEquals("v1", h.getPageParameters().get("p1").toString());
		assertEquals("v2", h.getPageParameters().get("p2").toString());
	}


	/**
	 *
	 */
	@Test
	void decode7()
	{
		Url url = Url.parse("wicket/resource/" + CLASS_NAME + "/reference4?2~en5~stylenull");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));
		assertThat(handler).isInstanceOf(ResourceReferenceRequestHandler.class);
		ResourceReferenceRequestHandler h = (ResourceReferenceRequestHandler)handler;
		assertEquals(resource4, h.getResource());
		assertEquals(Locale.ENGLISH, h.getLocale());
		assertEquals("style", h.getStyle());
		assertNull(h.getVariation());
		assertEquals(0, h.getPageParameters().getIndexedCount());
		assertEquals(0, h.getPageParameters().getNamedKeys().size());
	}

	/**
	 *
	 */
	@Test
	void decode7A()
	{
		Url url = Url.parse("wicket/resource/" + CLASS_NAME + "/reference4?2~sknullnull");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));
		assertNull(handler);
	}

	/**
	 *
	 */
	@Test
	void decode8()
	{
		Url url = Url.parse("wicket/resource/" + CLASS_NAME + "/reference4?2~en5~stylenull&p1=v1&p2=v2");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));
		assertThat(handler).isInstanceOf(ResourceReferenceRequestHandler.class);
		ResourceReferenceRequestHandler h = (ResourceReferenceRequestHandler)handler;
		assertEquals(resource4, h.getResource());
		assertEquals(Locale.ENGLISH, h.getLocale());
		assertEquals("style", h.getStyle());
		assertNull(h.getVariation());
		assertEquals(0, h.getPageParameters().getIndexedCount());
		assertEquals("v1", h.getPageParameters().get("p1").toString());
		assertEquals("v2", h.getPageParameters().get("p2").toString());
	}

	/**
	 *
	 */
	@Test
	void decode9()
	{
		Url url = Url.parse("wicket/resource/" + CLASS_NAME
			+ "/reference5?2~ennull9~variation&p1=v1&p2=v2");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));
		assertThat(handler).isInstanceOf(ResourceReferenceRequestHandler.class);
		ResourceReferenceRequestHandler h = (ResourceReferenceRequestHandler)handler;
		assertEquals(resource5, h.getResource());
		assertEquals(Locale.ENGLISH, h.getLocale());
		assertNull(h.getStyle());
		assertEquals("variation", h.getVariation());
		assertEquals(0, h.getPageParameters().getIndexedCount());
		assertEquals("v1", h.getPageParameters().get("p1").toString());
		assertEquals("v2", h.getPageParameters().get("p2").toString());
	}

	/**
	 *
	 */
	@Test
	void decode10()
	{
		Url url = Url.parse("wicket/resource/" + CLASS_NAME
			+ "/reference6?2~en5~style9~variation&p1=v1&p2=v2");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));
		assertThat(handler).isInstanceOf(ResourceReferenceRequestHandler.class);
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
	 * https://issues.apache.org/jira/browse/WICKET-5673
	 */
	@Test
	void decode11()
	{
		Url url = Url.parse("wicket/resource/com.example.Scope/");
		int score = encoder.getCompatibilityScore(getRequest(url));
		assertEquals(-1, score);
	}

	/**
	 *
	 */
	@Test
	void encode1()
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
	void encode2()
	{
		PageParameters parameters = new PageParameters();
		parameters.set(0, "X");
		parameters.add("p1", "v1", INamedParameters.Type.QUERY_STRING);
		parameters.add("p2", "v2", INamedParameters.Type.QUERY_STRING);
		ResourceReferenceRequestHandler handler = new ResourceReferenceRequestHandler(reference1,
			parameters);

		Url url = encoder.mapHandler(handler);
		assertEquals("wicket/resource/" + CLASS_NAME + "/reference1?p1=v1&p2=v2", url.toString());
	}

	/**
	 *
	 */
	@Test
	void encode3()
	{
		ResourceReferenceRequestHandler handler = new ResourceReferenceRequestHandler(reference2,
			null);
		Url url = encoder.mapHandler(handler);
		assertEquals("wicket/resource/" + CLASS_NAME + "/reference2/name2?5~en_ENnullnull", url.toString());
	}

	/**
	 *
	 */
	@Test
	void encode4()
	{
		PageParameters parameters = new PageParameters();
		parameters.set(0, "X");
		parameters.add("p1", "v1", INamedParameters.Type.QUERY_STRING);
		parameters.add("p2", "v2", INamedParameters.Type.QUERY_STRING);
		ResourceReferenceRequestHandler handler = new ResourceReferenceRequestHandler(reference2,
			parameters);

		Url url = encoder.mapHandler(handler);
		assertEquals("wicket/resource/" + CLASS_NAME + "/reference2/name2?5~en_ENnullnull&p1=v1&p2=v2",
			url.toString());
	}

	/**
	 *
	 */
	@Test
	void encode5()
	{
		ResourceReferenceRequestHandler handler = new ResourceReferenceRequestHandler(reference3,
			null);
		Url url = encoder.mapHandler(handler);
		assertEquals("wicket/resource/" + CLASS_NAME + "/reference3?null5~stylenull", url.toString());
	}

	/**
	 *
	 */
	@Test
	void encode6()
	{
		PageParameters parameters = new PageParameters();
		parameters.set(0, "X");
		parameters.add("p1", "v1", INamedParameters.Type.QUERY_STRING);
		parameters.add("p2", "v2", INamedParameters.Type.QUERY_STRING);
		ResourceReferenceRequestHandler handler = new ResourceReferenceRequestHandler(reference3,
			parameters);

		Url url = encoder.mapHandler(handler);
		assertEquals("wicket/resource/" + CLASS_NAME + "/reference3?null5~stylenull&p1=v1&p2=v2",
			url.toString());
	}

	/**
	 *
	 */
	@Test
	void encode7()
	{
		ResourceReferenceRequestHandler handler = new ResourceReferenceRequestHandler(reference4,
			null);
		Url url = encoder.mapHandler(handler);
		assertEquals("wicket/resource/" + CLASS_NAME + "/reference4?2~en5~stylenull", url.toString());
	}

	/**
	 *
	 */
	@Test
	void encode8()
	{
		PageParameters parameters = new PageParameters();
		parameters.set(0, "X");
		parameters.add("p1", "v1", INamedParameters.Type.QUERY_STRING);
		parameters.add("p2", "v2", INamedParameters.Type.QUERY_STRING);
		ResourceReferenceRequestHandler handler = new ResourceReferenceRequestHandler(reference4,
			parameters);

		Url url = encoder.mapHandler(handler);
		assertEquals("wicket/resource/" + CLASS_NAME + "/reference4?2~en5~stylenull&p1=v1&p2=v2",
			url.toString());
	}

	/**
	 * Tests request to url encoding when style is null but variation is not
	 */
	@Test
	void encode9()
	{
		ResourceReferenceRequestHandler handler = new ResourceReferenceRequestHandler(reference5,
			null);

		Url url = encoder.mapHandler(handler);
		assertEquals("wicket/resource/" + CLASS_NAME + "/reference5?2~ennull9~variation", url.toString());
	}

	/**
	 *
	 */
	@Test
	void versionStringInResourceFilename()
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
			public IResourceStream getResourceStream()
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

		IResourceCachingStrategy strategy = new FilenameWithVersionResourceCachingStrategy(
			"-version-", new AlphaDigitResourceVersion("foobar"));

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

		// check a version that contains a dot which also marks the filename
		// extension
		strategy = new FilenameWithVersionResourceCachingStrategy("-version-",
			new StaticResourceVersion("1.0.4-beta"));
		url = new ResourceUrl("test.txt", params);
		strategy.decorateUrl(url, resource);
		assertEquals("test-version-1.0.4-beta.txt", url.getFileName());
	}

	/**
	 * A resource version that allows any of: alpha, digit, dash and dot charcters
	 */
	private static class AlphaDigitResourceVersion implements IResourceVersion
	{
		private static final Pattern pattern = Pattern.compile("[0-9a-z-\\.]*");

		private final String version;

		/**
		 * create static version provider
		 *
		 * @param version
		 *             static version string to deliver for all queries resources
		 */
		AlphaDigitResourceVersion(String version)
		{
			this.version = Args.notNull(version, "version");
		}

		@Override
		public String getVersion(IStaticCacheableResource resource)
		{
			return version;
		}

		@Override
		public Pattern getVersionPattern()
		{
			return pattern;
		}
	}

	/**
	 *
	 */
	@Test
	void requestWithEmptyFilename()
	{
		Url url = Url.parse("wicket/resource/" + CLASS_NAME + "/");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));
		assertNull(handler);
	}

	/**
	 * Tests <a href="https://issues.apache.org/jira/browse/WICKET-3918">WICKET-3918</a>.
	 */
	@Test
	void wicket3918()
	{
		Url url = Url
			.parse("wicket/resource/org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow/res/");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));
		assertNull(handler);
	}
}

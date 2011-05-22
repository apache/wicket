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

import java.util.Locale;

import org.apache.wicket.ThreadContext;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.mapper.parameter.PageParametersEncoder;
import org.apache.wicket.request.resource.caching.FilenameWithTimestampResourceCachingStrategy;
import org.apache.wicket.request.resource.caching.IResourceCachingStrategy;
import org.apache.wicket.request.resource.caching.NoOpResourceCachingStrategy;
import org.apache.wicket.util.IProvider;
import org.apache.wicket.util.ValueProvider;
import org.apache.wicket.util.tester.WicketTester;
import org.apache.wicket.util.time.Time;
import org.mockito.Mockito;

/**
 * @author Matej Knopp
 */
public class BasicResourceReferenceMapperTest extends AbstractResourceReferenceMapperTest
{
	private static final IProvider<IResourceCachingStrategy> NO_CACHING = new ValueProvider<IResourceCachingStrategy>(
		NoOpResourceCachingStrategy.INSTANCE);

	private static final IProvider<FilenameWithTimestampResourceCachingStrategy> CACHE_FILENAME_WITH_TIMESTAMP = new ValueProvider<FilenameWithTimestampResourceCachingStrategy>(
		new FilenameWithTimestampResourceCachingStrategy());

	/**
	 * Construct.
	 */
	public BasicResourceReferenceMapperTest()
	{
	}

	private final BasicResourceReferenceMapper encoder = new BasicResourceReferenceMapper(
		new PageParametersEncoder(), NO_CACHING)
	{
		@Override
		protected IMapperContext getContext()
		{
			return context;
		}
	};

	private final BasicResourceReferenceMapper encoderWithTimestamps = new BasicResourceReferenceMapper(
		new PageParametersEncoder(), CACHE_FILENAME_WITH_TIMESTAMP)
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
	public void testDecode1()
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
	public void testDecode1A()
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
	public void testDecode2()
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
	public void testDecode2A()
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
	public void testDecode3()
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
	public void testDecode3A()
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
	public void testDecode3B()
	{
		Url url = Url.parse("wicket/resource/" + CLASS_NAME + "/reference2/name2");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));
		assertNull(handler);
	}

	/**
	 * 
	 */
	public void testDecode4()
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
	public void testDecode5()
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
	public void testDecode6()
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
	public void testDecode7()
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
	public void testDecode7A()
	{
		Url url = Url.parse("wicket/resource/" + CLASS_NAME + "/reference4?sk");
		IRequestHandler handler = encoder.mapRequest(getRequest(url));
		assertNull(handler);
	}

	/**
	 * 
	 */
	public void testDecode8()
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
	public void testDecode9()
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
	public void testDecode10()
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
	public void testEncode1()
	{
		ResourceReferenceRequestHandler handler = new ResourceReferenceRequestHandler(reference1,
			null);
		Url url = encoder.mapHandler(handler);
		assertEquals("wicket/resource/" + CLASS_NAME + "/reference1", url.toString());
	}

	/**
	 * 
	 */
	public void testEncode2()
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
	public void testEncode3()
	{
		ResourceReferenceRequestHandler handler = new ResourceReferenceRequestHandler(reference2,
			null);
		Url url = encoder.mapHandler(handler);
		assertEquals("wicket/resource/" + CLASS_NAME + "/reference2/name2?en_EN", url.toString());
	}

	/**
	 * 
	 */
	public void testEncode4()
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
	public void testEncode5()
	{
		ResourceReferenceRequestHandler handler = new ResourceReferenceRequestHandler(reference3,
			null);
		Url url = encoder.mapHandler(handler);
		assertEquals("wicket/resource/" + CLASS_NAME + "/reference3?-style", url.toString());
	}

	/**
	 * 
	 */
	public void testEncode6()
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
	public void testEncode7()
	{
		ResourceReferenceRequestHandler handler = new ResourceReferenceRequestHandler(reference4,
			null);
		Url url = encoder.mapHandler(handler);
		assertEquals("wicket/resource/" + CLASS_NAME + "/reference4?en-style", url.toString());
	}

	/**
	 * 
	 */
	public void testEncode8()
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
	public void testEncode9()
	{
		ResourceReferenceRequestHandler handler = new ResourceReferenceRequestHandler(reference5,
			null);

		Url url = encoder.mapHandler(handler);
		assertEquals("wicket/resource/" + CLASS_NAME + "/reference5?en--variation", url.toString());
	}

	/**
	 * 
	 */
	public void testLastModifiedTimestampIsPartOfUrl()
	{
		long millis = 12345678L;
		final ResourceReferenceWithTimestamp reference = new ResourceReferenceWithTimestamp(
			Time.millis(millis));
		final IRequestHandler handler = new ResourceReferenceRequestHandler(reference, null);

		// request url with timestamp
		Url url = encoderWithTimestamps.mapHandler(handler);

		// check that url contains timestamp
		String timestampPart = CACHE_FILENAME_WITH_TIMESTAMP.get().getTimestampPrefix() +
			Long.toString(millis) + "?";
		assertTrue(url.toString().contains(timestampPart));
	}

	/**
	 * 
	 */
	public void testLastModifiedTimestampCache()
	{
		long millis = 87654321L;
		final ResourceReferenceWithTimestamp reference = new ResourceReferenceWithTimestamp(
			Time.millis(millis));
		final IRequestHandler handler = new ResourceReferenceRequestHandler(reference, null);

		WicketTester tester = new WicketTester();

		// setup mock request cycle
		RequestCycle cycle = Mockito.mock(RequestCycle.class);
		ThreadContext.setRequestCycle(cycle);

		// request url with timestamp
		Url url1 = encoderWithTimestamps.mapHandler(handler);
		assertNotNull(url1);
		assertEquals(1, reference.lastModifiedInvocationCount);

		// subsequent request should take timestamp from request cycle scoped cache
		Url url2 = encoderWithTimestamps.mapHandler(handler);
		assertNotNull(url2);

		Url url3 = encoderWithTimestamps.mapHandler(handler);
		assertNotNull(url3);

		assertEquals(1, reference.lastModifiedInvocationCount);

		// urls should be equal
		assertEquals(url1, url2);
		assertEquals(url1, url3);
	}
}

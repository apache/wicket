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

import java.util.Locale;

import org.apache._wicket.PageParameters;
import org.apache._wicket.request.RequestHandler;
import org.apache._wicket.request.Url;
import org.apache._wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache._wicket.request.handler.resource.ResourceRequestHandler;

/**
 * @author Matej Knopp
 */
public class ResourceReferenceEncoderTest extends AbstractResourceReferenceEncoderTest
{
	/**
	 * Construct.
	 */
	public ResourceReferenceEncoderTest()
	{
	}
	
	private final ResourceReferenceEncoder encoder = new ResourceReferenceEncoder()
	{
		@Override
		protected EncoderContext getContext()
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
		RequestHandler handler = encoder.decode(getRequest(url));
		assertTrue(handler instanceof ResourceRequestHandler);
		ResourceRequestHandler h = (ResourceRequestHandler) handler;
		assertEquals(resource1, h.getResource());
		assertEquals(null, h.getLocale());
		assertEquals(null, h.getStyle());
		assertEquals(0, h.getPageParameters().getIndexedParamsCount());
		assertEquals(0, h.getPageParameters().getNamedParameterKeys().size());
	}
	
	/**
	 * 
	 */	
	public void testDecode1A()
	{
		Url url = Url.parse("wicket/resource/" + CLASS_NAME + "/reference1?en");
		RequestHandler handler = encoder.decode(getRequest(url));
		assertTrue(handler instanceof ResourceRequestHandler);
		ResourceRequestHandler h = (ResourceRequestHandler) handler;
		assertEquals(resource1, h.getResource());
		assertEquals(Locale.ENGLISH, h.getLocale());
		assertEquals(null, h.getStyle());
		assertEquals(0, h.getPageParameters().getIndexedParamsCount());
		assertEquals(0, h.getPageParameters().getNamedParameterKeys().size());
	}
	
	/**
	 * 
	 */
	public void testDecode2()
	{
		Url url = Url.parse("wicket/resource/" + CLASS_NAME + "/reference1?p1=v1&p2=v2");
		RequestHandler handler = encoder.decode(getRequest(url));
		assertTrue(handler instanceof ResourceRequestHandler);
		ResourceRequestHandler h = (ResourceRequestHandler) handler;
		assertEquals(resource1, h.getResource());
		assertEquals(null, h.getLocale());
		assertEquals(null, h.getStyle());
		assertEquals(0, h.getPageParameters().getIndexedParamsCount());
		assertEquals("v1", h.getPageParameters().getNamedParameter("p1").toString());
		assertEquals("v2", h.getPageParameters().getNamedParameter("p2").toString());
	}	
	
	/**
	 * 
	 */
	public void testDecode2A()
	{
		Url url = Url.parse("wicket/resource/" + CLASS_NAME + "/reference1?-style&p1=v1&p2=v2");
		RequestHandler handler = encoder.decode(getRequest(url));
		assertTrue(handler instanceof ResourceRequestHandler);
		ResourceRequestHandler h = (ResourceRequestHandler) handler;
		assertEquals(resource1, h.getResource());
		assertEquals(null, h.getLocale());
		assertEquals("style", h.getStyle());
		assertEquals(0, h.getPageParameters().getIndexedParamsCount());
		assertEquals("v1", h.getPageParameters().getNamedParameter("p1").toString());
		assertEquals("v2", h.getPageParameters().getNamedParameter("p2").toString());
	}
	
	/**
	 * 
	 */
	public void testDecode3()
	{
		Url url = Url.parse("wicket/resource/" + CLASS_NAME + "/reference2/name2?en_EN");
		RequestHandler handler = encoder.decode(getRequest(url));
		assertTrue(handler instanceof ResourceRequestHandler);
		ResourceRequestHandler h = (ResourceRequestHandler) handler;
		assertEquals(resource2, h.getResource());
		assertEquals(new Locale("en", "en"), h.getLocale());
		assertEquals(null, h.getStyle());
		assertEquals(0, h.getPageParameters().getIndexedParamsCount());
		assertEquals(0, h.getPageParameters().getNamedParameterKeys().size());
	}
	
	/**
	 * 
	 */
	public void testDecode3A()
	{
		Url url = Url.parse("wicket/resource/" + CLASS_NAME + "/reference2/name2?en_EN-style");
		RequestHandler handler = encoder.decode(getRequest(url));
		assertTrue(handler instanceof ResourceRequestHandler);
		ResourceRequestHandler h = (ResourceRequestHandler) handler;
		assertEquals(resource2, h.getResource());
		assertEquals(new Locale("en", "en"), h.getLocale());
		assertEquals("style", h.getStyle());
		assertEquals(0, h.getPageParameters().getIndexedParamsCount());
		assertEquals(0, h.getPageParameters().getNamedParameterKeys().size());
	}
	
	/**
	 * 
	 */
	public void testDecode3B()
	{
		Url url = Url.parse("wicket/resource/" + CLASS_NAME + "/reference2/name2");
		RequestHandler handler = encoder.decode(getRequest(url));
		assertNull(handler);
	}
	
	/**
	 * 
	 */
	public void testDecode4()
	{
		Url url = Url.parse("wicket/resource/" + CLASS_NAME + "/reference2/name2?en_EN&p1=v1&p2=v2");
		RequestHandler handler = encoder.decode(getRequest(url));
		assertTrue(handler instanceof ResourceRequestHandler);
		ResourceRequestHandler h = (ResourceRequestHandler) handler;
		assertEquals(resource2, h.getResource());
		assertEquals(new Locale("en", "en"), h.getLocale());
		assertEquals(null, h.getStyle());
		assertEquals("v1", h.getPageParameters().getNamedParameter("p1").toString());
		assertEquals("v2", h.getPageParameters().getNamedParameter("p2").toString());
	}
	
	/**
	 * 
	 */
	public void testDecode5()
	{
		Url url = Url.parse("wicket/resource/" + CLASS_NAME + "/reference3?-style");
		RequestHandler handler = encoder.decode(getRequest(url));
		assertTrue(handler instanceof ResourceRequestHandler);
		ResourceRequestHandler h = (ResourceRequestHandler) handler;
		assertEquals(resource3, h.getResource());
		assertEquals(null, h.getLocale());
		assertEquals("style", h.getStyle());
		assertEquals(0, h.getPageParameters().getIndexedParamsCount());
		assertEquals(0, h.getPageParameters().getNamedParameterKeys().size());
	}
	
	/**
	 * 
	 */
	public void testDecode6()
	{
		Url url = Url.parse("wicket/resource/" + CLASS_NAME + "/reference3?-style&p1=v1&p2=v2");
		RequestHandler handler = encoder.decode(getRequest(url));
		assertTrue(handler instanceof ResourceRequestHandler);
		ResourceRequestHandler h = (ResourceRequestHandler) handler;
		assertEquals(resource3, h.getResource());
		assertEquals(null, h.getLocale());
		assertEquals("style", h.getStyle());
		assertEquals(0, h.getPageParameters().getIndexedParamsCount());
		assertEquals("v1", h.getPageParameters().getNamedParameter("p1").toString());
		assertEquals("v2", h.getPageParameters().getNamedParameter("p2").toString());
	}	
	
	
	/**
	 * 
	 */
	public void testDecode7()
	{
		Url url = Url.parse("wicket/resource/" + CLASS_NAME + "/reference4?en-style");
		RequestHandler handler = encoder.decode(getRequest(url));
		assertTrue(handler instanceof ResourceRequestHandler);
		ResourceRequestHandler h = (ResourceRequestHandler) handler;
		assertEquals(resource4, h.getResource());
		assertEquals(Locale.ENGLISH, h.getLocale());
		assertEquals("style", h.getStyle());
		assertEquals(0, h.getPageParameters().getIndexedParamsCount());
		assertEquals(0, h.getPageParameters().getNamedParameterKeys().size());
	}
	
	/**
	 * 
	 */
	public void testDecode7A()
	{
		Url url = Url.parse("wicket/resource/" + CLASS_NAME + "/reference4?sk");
		RequestHandler handler = encoder.decode(getRequest(url));
		assertNull(handler);
	}
	
	/**
	 * 
	 */
	public void testDecode8()
	{
		Url url = Url.parse("wicket/resource/" + CLASS_NAME + "/reference4?en-style&p1=v1&p2=v2");
		RequestHandler handler = encoder.decode(getRequest(url));
		assertTrue(handler instanceof ResourceRequestHandler);
		ResourceRequestHandler h = (ResourceRequestHandler) handler;
		assertEquals(resource4, h.getResource());
		assertEquals(Locale.ENGLISH, h.getLocale());
		assertEquals("style", h.getStyle());
		assertEquals(0, h.getPageParameters().getIndexedParamsCount());
		assertEquals("v1", h.getPageParameters().getNamedParameter("p1").toString());
		assertEquals("v2", h.getPageParameters().getNamedParameter("p2").toString());
	}	
	
	/**
	 * 
	 */
	public void testEncode1()
	{
		ResourceReferenceRequestHandler handler = new ResourceReferenceRequestHandler(reference1, null);
		Url url = encoder.encode(handler);
		assertEquals("wicket/resource/" + CLASS_NAME + "/reference1", url.toString());
	}
	
	/**
	 * 
	 */
	public void testEncode2()
	{
		PageParameters parameters = new PageParameters();
		parameters.setIndexedParameter(0, "X");
		parameters.addNamedParameter("p1", "v1");
		parameters.addNamedParameter("p2", "v2");
		ResourceReferenceRequestHandler handler = new ResourceReferenceRequestHandler(reference1, parameters);
		
		Url url = encoder.encode(handler);
		assertEquals("wicket/resource/" + CLASS_NAME + "/reference1?p1=v1&p2=v2", url.toString());
	}
	
	/**
	 * 
	 */
	public void testEncode3()
	{
		ResourceReferenceRequestHandler handler = new ResourceReferenceRequestHandler(reference2, null);
		Url url = encoder.encode(handler);
		assertEquals("wicket/resource/" + CLASS_NAME + "/reference2/name2?en_EN", url.toString());
	}
	
	/**
	 * 
	 */
	public void testEncode4()
	{
		PageParameters parameters = new PageParameters();
		parameters.setIndexedParameter(0, "X");
		parameters.addNamedParameter("p1", "v1");
		parameters.addNamedParameter("p2", "v2");
		ResourceReferenceRequestHandler handler = new ResourceReferenceRequestHandler(reference2, parameters);
		
		Url url = encoder.encode(handler);
		assertEquals("wicket/resource/" + CLASS_NAME + "/reference2/name2?en_EN&p1=v1&p2=v2", url.toString());
	}
	
	/**
	 * 
	 */
	public void testEncode5()
	{
		ResourceReferenceRequestHandler handler = new ResourceReferenceRequestHandler(reference3, null);
		Url url = encoder.encode(handler);
		assertEquals("wicket/resource/" + CLASS_NAME + "/reference3?-style", url.toString());
	}
	
	/**
	 * 
	 */
	public void testEncode6()
	{
		PageParameters parameters = new PageParameters();
		parameters.setIndexedParameter(0, "X");
		parameters.addNamedParameter("p1", "v1");
		parameters.addNamedParameter("p2", "v2");
		ResourceReferenceRequestHandler handler = new ResourceReferenceRequestHandler(reference3, parameters);
		
		Url url = encoder.encode(handler);
		assertEquals("wicket/resource/" + CLASS_NAME + "/reference3?-style&p1=v1&p2=v2", url.toString());
	}
	
	/**
	 * 
	 */
	public void testEncode7()
	{
		ResourceReferenceRequestHandler handler = new ResourceReferenceRequestHandler(reference4, null);
		Url url = encoder.encode(handler);
		assertEquals("wicket/resource/" + CLASS_NAME + "/reference4?en-style", url.toString());
	}
	
	/**
	 * 
	 */
	public void testEncode8()
	{
		PageParameters parameters = new PageParameters();
		parameters.setIndexedParameter(0, "X");
		parameters.addNamedParameter("p1", "v1");
		parameters.addNamedParameter("p2", "v2");
		ResourceReferenceRequestHandler handler = new ResourceReferenceRequestHandler(reference4, parameters);
		
		Url url = encoder.encode(handler);
		assertEquals("wicket/resource/" + CLASS_NAME + "/reference4?en-style&p1=v1&p2=v2", url.toString());
	}
}

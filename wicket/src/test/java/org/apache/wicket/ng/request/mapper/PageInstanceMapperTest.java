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
package org.apache.wicket.ng.request.mapper;

import org.apache.wicket.ng.MockPage;
import org.apache.wicket.ng.markup.html.link.ILinkListener;
import org.apache.wicket.ng.request.IRequestHandler;
import org.apache.wicket.ng.request.Url;
import org.apache.wicket.ng.request.component.IRequestableComponent;
import org.apache.wicket.ng.request.handler.PageAndComponentProvider;
import org.apache.wicket.ng.request.handler.DefaultPageProvider;
import org.apache.wicket.ng.request.handler.IPageProvider;
import org.apache.wicket.ng.request.handler.IPageRequestHandler;
import org.apache.wicket.ng.request.handler.impl.ListenerInterfaceRequestHandler;
import org.apache.wicket.ng.request.handler.impl.RenderPageRequestHandler;

/**
 * 
 * @author Matej Knopp
 */
public class PageInstanceMapperTest extends AbstractEncoderTest
{

    /**
     * 
     * Construct.
     */
    public PageInstanceMapperTest()
    {
    }

    private PageInstanceMapper encoder = new PageInstanceMapper()
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
        Url url = Url.parse("wicket/page?4");

        IRequestHandler handler = encoder.mapRequest(getRequest(url));
        assertTrue(handler instanceof RenderPageRequestHandler);

        RenderPageRequestHandler h = (RenderPageRequestHandler)handler;
        checkPage(h.getPage(), 4);
    }

    /**
	 * 
	 */
    public void testDecode2()
    {
        Url url = Url.parse("wicket/page/ingore/me?4&a=3&b=3");

        IRequestHandler handler = encoder.mapRequest(getRequest(url));
        assertTrue(handler instanceof RenderPageRequestHandler);

        RenderPageRequestHandler h = (RenderPageRequestHandler)handler;
        checkPage(h.getPage(), 4);
    }

    /**
	 * 
	 */
    public void testDecode3()
    {
        Url url = Url.parse("wicket/page?4-ILinkListener-a-b-c");

        IRequestHandler handler = encoder.mapRequest(getRequest(url));
        assertTrue(handler instanceof ListenerInterfaceRequestHandler);

        ListenerInterfaceRequestHandler h = (ListenerInterfaceRequestHandler)handler;
        checkPage(h.getPage(), 4);
        assertEquals(h.getComponent().getPath(), "a:b:c");
        assertEquals(ILinkListener.INTERFACE, h.getListenerInterface());
        assertNull(h.getBehaviorIndex());
    }

    /**
	 * 
	 */
    public void testDecode4()
    {
        Url url = Url.parse("wickett/pagee?4-ILinkListener-a:b-c");

        IRequestHandler handler = encoder.mapRequest(getRequest(url));
        assertNull(handler);
    }

    /**
	 * 
	 */
    public void testDecode5()
    {
        Url url = Url.parse("wicket/page?abc");

        IRequestHandler handler = encoder.mapRequest(getRequest(url));
        assertNull(handler);
    }

    /**
	 * 
	 */
    public void testDecode6()
    {
        Url url = Url.parse("wicket/page?4-ILinkListener.5-a-b-c");

        IRequestHandler handler = encoder.mapRequest(getRequest(url));
        assertTrue(handler instanceof ListenerInterfaceRequestHandler);

        ListenerInterfaceRequestHandler h = (ListenerInterfaceRequestHandler)handler;
        checkPage(h.getPage(), 4);
        assertEquals(h.getComponent().getPath(), "a:b:c");
        assertEquals(ILinkListener.INTERFACE, h.getListenerInterface());
        assertEquals((Object)5, h.getBehaviorIndex());
    }

    /**
	 * 
	 */
    public void testDecode7()
    {
        Url url = Url.parse("wicket/page?4-6.ILinkListener.5-a-b-c");

        context.setNextPageRenderCount(6);

        IRequestHandler handler = encoder.mapRequest(getRequest(url));
        assertTrue(handler instanceof ListenerInterfaceRequestHandler);

        ListenerInterfaceRequestHandler h = (ListenerInterfaceRequestHandler)handler;
        assertEquals(6, h.getPage().getRenderCount());
    }

    /**
	 * 
	 */
    public void testDecode8()
    {
        Url url = Url.parse("wicket/page?4-6.ILinkListener.5-a-b-c");

        context.setNextPageRenderCount(8);

        try
        {
            IRequestHandler handler = encoder.mapRequest(getRequest(url));

            ((IPageRequestHandler)handler).getPage();

            // should never get here
            assertFalse(true);
        }
        catch (StalePageException e)
        {

        }
    }

    /**
	 * 
	 */
    public void testEncode1()
    {
        MockPage page = new MockPage(15);
        IPageProvider provider = new DefaultPageProvider(page);
        IRequestHandler handler = new RenderPageRequestHandler(provider);

        Url url = encoder.mapHandler(handler);
        assertEquals("wicket/page?15", url.toString());
    }


    /**
	 * 
	 */
    public void testEncode2()
    {
        MockPage page = new MockPage(15);
        page.setRenderCount(5);

        IRequestableComponent c = page.get("a:b:c");

        PageAndComponentProvider provider = new PageAndComponentProvider(page, c);
        IRequestHandler handler = new ListenerInterfaceRequestHandler(provider,
                ILinkListener.INTERFACE);

        Url url = encoder.mapHandler(handler);
        assertEquals("wicket/page?15-5.ILinkListener-a-b-c", url.toString());
    }

}

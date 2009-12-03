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
import org.apache.wicket.ng.request.RequestHandler;
import org.apache.wicket.ng.request.Url;
import org.apache.wicket.ng.request.component.PageParametersNg;
import org.apache.wicket.ng.request.component.RequestableComponent;
import org.apache.wicket.ng.request.component.RequestablePage;
import org.apache.wicket.ng.request.handler.PageAndComponentProvider;
import org.apache.wicket.ng.request.handler.DefaultPageProvider;
import org.apache.wicket.ng.request.handler.PageProvider;
import org.apache.wicket.ng.request.handler.PageRequestHandler;
import org.apache.wicket.ng.request.handler.impl.BookmarkableListenerInterfaceRequestHandler;
import org.apache.wicket.ng.request.handler.impl.BookmarkablePageRequestHandler;
import org.apache.wicket.ng.request.handler.impl.ListenerInterfaceRequestHandler;
import org.apache.wicket.ng.request.handler.impl.RenderPageRequestHandler;

/**
 * @author Matej Knopp
 */
public class BookmarkableEncoderTest extends AbstractEncoderTest
{

    /**
     * Construct.
     */
    public BookmarkableEncoderTest()
    {
    }

    private BookmarkableMapper encoder = new BookmarkableMapper()
    {
        @Override
        protected MapperContext getContext()
        {
            return context;
        }
    };

    private static final String PAGE_CLASS_NAME = MockPage.class.getName();

    /**
	 * 
	 */
    public void testDecode1()
    {
        Url url = Url.parse("wicket/bookmarkable/" + PAGE_CLASS_NAME);
        RequestHandler handler = encoder.mapRequest(getRequest(url));

        assertTrue(handler instanceof RenderPageRequestHandler);
        RequestablePage page = ((RenderPageRequestHandler)handler).getPage();
        assertEquals(PAGE_CLASS_NAME, page.getClass().getName());
        assertEquals(0, page.getPageParametersNg().getIndexedParamsCount());
        assertTrue(page.getPageParametersNg().getNamedParameterKeys().isEmpty());
    }

    /**
	 * 
	 */
    public void testDecode2()
    {
        Url url = Url.parse("wicket/bookmarkable/" + PAGE_CLASS_NAME + "/indexed1?a=b&b=c");
        RequestHandler handler = encoder.mapRequest(getRequest(url));

        assertTrue(handler instanceof RenderPageRequestHandler);
        RequestablePage page = ((RenderPageRequestHandler)handler).getPage();
        assertEquals(PAGE_CLASS_NAME, page.getClass().getName());

        PageParametersNg p = page.getPageParametersNg();
        assertEquals(1, p.getIndexedParamsCount());
        assertEquals("indexed1", p.getIndexedParameter(0).toString());

        assertEquals(2, p.getNamedParameterKeys().size());
        assertEquals("b", p.getNamedParameter("a").toString());
        assertEquals("c", p.getNamedParameter("b").toString());
    }

    /**
	 * 
	 */
    public void testDecode3()
    {
        Url url = Url.parse("wicket/bookmarkable/" + PAGE_CLASS_NAME + "?15");
        RequestHandler handler = encoder.mapRequest(getRequest(url));

        assertTrue(handler instanceof RenderPageRequestHandler);
        RequestablePage page = ((RenderPageRequestHandler)handler).getPage();
        checkPage(page, 15);
    }

    /**
	 * 
	 */
    public void testDecode4()
    {
        Url url = Url.parse("wicket/bookmarkable/" + PAGE_CLASS_NAME + "/i1/i2?15&a=b&b=c");
        RequestHandler handler = encoder.mapRequest(getRequest(url));

        assertTrue(handler instanceof RenderPageRequestHandler);
        RequestablePage page = ((RenderPageRequestHandler)handler).getPage();
        checkPage(page, 15);

        PageParametersNg p = page.getPageParametersNg();
        assertEquals(2, p.getIndexedParamsCount());
        assertEquals("i1", p.getIndexedParameter(0).toString());
        assertEquals("i2", p.getIndexedParameter(1).toString());

        assertEquals(2, p.getNamedParameterKeys().size());
        assertEquals("b", p.getNamedParameter("a").toString());
        assertEquals("c", p.getNamedParameter("b").toString());
    }

    /**
	 * 
	 */
    public void testDecode5()
    {
        Url url = Url.parse("wicket/bookmarkable/" + PAGE_CLASS_NAME + "?15-ILinkListener-foo-bar");
        RequestHandler handler = encoder.mapRequest(getRequest(url));

        assertTrue(handler instanceof ListenerInterfaceRequestHandler);

        ListenerInterfaceRequestHandler h = (ListenerInterfaceRequestHandler)handler;

        RequestablePage page = h.getPage();
        checkPage(page, 15);

        assertEquals(ILinkListener.INTERFACE, h.getListenerInterface());
        assertEquals("foo:bar", h.getComponent().getPath());
        assertNull(h.getBehaviorIndex());
    }

    /**
	 * 
	 */
    public void testDecode6()
    {
        Url url = Url.parse("wicket/bookmarkable/" + PAGE_CLASS_NAME +
                "/i1/i2?15-ILinkListener-foo-bar&a=b&b=c");
        RequestHandler handler = encoder.mapRequest(getRequest(url));

        assertTrue(handler instanceof ListenerInterfaceRequestHandler);
        ListenerInterfaceRequestHandler h = (ListenerInterfaceRequestHandler)handler;

        RequestablePage page = h.getPage();
        checkPage(page, 15);

        assertEquals(ILinkListener.INTERFACE, h.getListenerInterface());
        assertEquals("foo:bar", h.getComponent().getPath());

        PageParametersNg p = page.getPageParametersNg();
        assertEquals(2, p.getIndexedParamsCount());
        assertEquals("i1", p.getIndexedParameter(0).toString());
        assertEquals("i2", p.getIndexedParameter(1).toString());

        assertEquals(2, p.getNamedParameterKeys().size());
        assertEquals("b", p.getNamedParameter("a").toString());
        assertEquals("c", p.getNamedParameter("b").toString());
    }

    /**
	 * 
	 */
    public void testDecode7()
    {
        Url url = Url.parse("wicket/bookmarkable/" + PAGE_CLASS_NAME +
                "?15-ILinkListener.4-foo-bar");
        RequestHandler handler = encoder.mapRequest(getRequest(url));

        assertTrue(handler instanceof ListenerInterfaceRequestHandler);

        ListenerInterfaceRequestHandler h = (ListenerInterfaceRequestHandler)handler;

        RequestablePage page = h.getPage();
        checkPage(page, 15);

        assertEquals(ILinkListener.INTERFACE, h.getListenerInterface());
        assertEquals("foo:bar", h.getComponent().getPath());
        assertEquals((Object)4, h.getBehaviorIndex());
    }

    /**
	 * 
	 */
    public void testDecode8()
    {
        Url url = Url.parse("wicket/bookmarkable/" + PAGE_CLASS_NAME +
                "/i1/i2?15-5.ILinkListener-foo-bar&a=b&b=c");

        context.setNextPageRenderCount(5);

        RequestHandler handler = encoder.mapRequest(getRequest(url));

        assertTrue(handler instanceof ListenerInterfaceRequestHandler);
        ListenerInterfaceRequestHandler h = (ListenerInterfaceRequestHandler)handler;

        RequestablePage page = h.getPage();
        assertEquals(page.getRenderCount(), 5);
    }

    /**
	 * 
	 */
    public void testDecode9()
    {
        Url url = Url.parse("wicket/bookmarkable/" + PAGE_CLASS_NAME +
                "/i1/i2?15-5.ILinkListener-foo-bar&a=b&b=c");

        context.setNextPageRenderCount(6);

        try
        {
            RequestHandler handler = encoder.mapRequest(getRequest(url));

            ((PageRequestHandler)handler).getPage();

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
        DefaultPageProvider provider = new DefaultPageProvider(MockPage.class, new PageParametersNg());
        provider.setPageSource(context);
        RequestHandler handler = new BookmarkablePageRequestHandler(provider);
        Url url = encoder.mapHandler(handler);
        assertEquals("wicket/bookmarkable/" + PAGE_CLASS_NAME, url.toString());
    }

    /**
	 * 
	 */
    public void testEncode2()
    {
        PageParametersNg parameters = new PageParametersNg();
        parameters.setIndexedParameter(0, "i1");
        parameters.setIndexedParameter(1, "i2");
        parameters.setNamedParameter("a", "b");
        parameters.setNamedParameter("b", "c");
        DefaultPageProvider provider = new DefaultPageProvider(MockPage.class, parameters);
        provider.setPageSource(context);
        RequestHandler handler = new BookmarkablePageRequestHandler(provider);
        Url url = encoder.mapHandler(handler);
        assertEquals("wicket/bookmarkable/" + PAGE_CLASS_NAME + "/i1/i2?a=b&b=c", url.toString());
    }

    /**
	 * 
	 */
    public void testEncode3()
    {
        PageParametersNg parameters = new PageParametersNg();
        parameters.setIndexedParameter(0, "i1");
        parameters.setIndexedParameter(1, "i2");
        parameters.setNamedParameter("a", "b");
        parameters.setNamedParameter("b", "c");

        DefaultPageProvider provider = new DefaultPageProvider(MockPage.class, parameters);
        provider.setPageSource(context);
        RequestHandler handler = new BookmarkablePageRequestHandler(provider);
        Url url = encoder.mapHandler(handler);

        assertEquals("wicket/bookmarkable/" + PAGE_CLASS_NAME + "/i1/i2?a=b&b=c", url.toString());
    }

    /**
	 * 
	 */
    public void testEncode4()
    {
        MockPage page = new MockPage(15);
        page.getPageParametersNg().setIndexedParameter(0, "i1");
        page.getPageParametersNg().setIndexedParameter(1, "i2");
        page.getPageParametersNg().setNamedParameter("a", "b");
        page.getPageParametersNg().setNamedParameter("b", "c");
        page.setCreatedBookmarkable(true);

        PageProvider provider = new DefaultPageProvider(page);
        RequestHandler handler = new RenderPageRequestHandler(provider);
        Url url = encoder.mapHandler(handler);

        assertEquals("wicket/bookmarkable/" + PAGE_CLASS_NAME + "/i1/i2?15&a=b&b=c", url.toString());
    }

    /**
	 * 
	 */
    public void testEncode5()
    {
        MockPage page = new MockPage(15);
        page.getPageParametersNg().setIndexedParameter(0, "i1");
        page.getPageParametersNg().setIndexedParameter(1, "i2");
        page.getPageParametersNg().setNamedParameter("a", "b");
        page.getPageParametersNg().setNamedParameter("b", "c");

        page.setCreatedBookmarkable(false);

        PageProvider provider = new DefaultPageProvider(page);
        RequestHandler handler = new RenderPageRequestHandler(provider);
        Url url = encoder.mapHandler(handler);

        // never allow bookmarkable render url for page that has not been created by bookmarkable
        // URL

        assertNull(url);
    }

    /**
	 * 
	 */
    public void testEncode6()
    {
        MockPage page = new MockPage(15);
        page.getPageParametersNg().setIndexedParameter(0, "i1");
        page.getPageParametersNg().setIndexedParameter(1, "i2");
        page.getPageParametersNg().setNamedParameter("a", "b");
        page.getPageParametersNg().setNamedParameter("b", "c");

        // shouldn't make any difference for BookmarkableListenerInterfaceRequestHandler,
        // as this explicitely says the url must be bookmarkable
        page.setCreatedBookmarkable(false);

        RequestableComponent c = page.get("foo:bar");

        PageAndComponentProvider provider = new PageAndComponentProvider(page, c);
        RequestHandler handler = new BookmarkableListenerInterfaceRequestHandler(provider,
                ILinkListener.INTERFACE);

        Url url = encoder.mapHandler(handler);

        assertEquals("wicket/bookmarkable/" + PAGE_CLASS_NAME +
                "/i1/i2?15-0.ILinkListener-foo-bar&a=b&b=c", url.toString());
    }

    /**
	 * 
	 */
    public void testEncode7()
    {
        MockPage page = new MockPage(15);
        page.getPageParametersNg().setIndexedParameter(0, "i1");
        page.getPageParametersNg().setIndexedParameter(1, "i2");
        page.getPageParametersNg().setNamedParameter("a", "b");
        page.getPageParametersNg().setNamedParameter("b", "c");

        // shouldn't make any difference for BookmarkableListenerInterfaceRequestHandler,
        // as this explicitely says the url must be bookmarkable
        page.setCreatedBookmarkable(false);

        RequestableComponent c = page.get("foo:bar");

        PageAndComponentProvider provider = new PageAndComponentProvider(page, c);
        RequestHandler handler = new BookmarkableListenerInterfaceRequestHandler(provider,
                ILinkListener.INTERFACE, 4);

        Url url = encoder.mapHandler(handler);

        assertEquals("wicket/bookmarkable/" + PAGE_CLASS_NAME +
                "/i1/i2?15-0.ILinkListener.4-foo-bar&a=b&b=c", url.toString());
    }

    /**
	 * 
	 */
    public void testEncode8()
    {
        MockPage page = new MockPage(15);
        page.setBookmarkable(true);
        page.setCreatedBookmarkable(true);
        page.setPageStateless(true);

        PageProvider provider = new DefaultPageProvider(page);
        RequestHandler handler = new RenderPageRequestHandler(provider);

        Url url = encoder.mapHandler(handler);

        assertEquals("wicket/bookmarkable/" + PAGE_CLASS_NAME, url.toString());
    }
}

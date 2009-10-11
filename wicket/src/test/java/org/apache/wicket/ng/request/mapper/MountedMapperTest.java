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
import org.apache.wicket.ng.request.RequestMapper;
import org.apache.wicket.ng.request.Url;
import org.apache.wicket.ng.request.component.PageParameters;
import org.apache.wicket.ng.request.component.RequestableComponent;
import org.apache.wicket.ng.request.component.RequestablePage;
import org.apache.wicket.ng.request.handler.PageAndComponentProvider;
import org.apache.wicket.ng.request.handler.PageProvider;
import org.apache.wicket.ng.request.handler.PageRequestHandler;
import org.apache.wicket.ng.request.handler.impl.BookmarkableListenerInterfaceRequestHandler;
import org.apache.wicket.ng.request.handler.impl.BookmarkablePageRequestHandler;
import org.apache.wicket.ng.request.handler.impl.ListenerInterfaceRequestHandler;
import org.apache.wicket.ng.request.handler.impl.RenderPageRequestHandler;

/**
 * @author Matej Knopp
 */
public class MountedMapperTest extends AbstractEncoderTest
{

    /**
     * Construct.
     */
    public MountedMapperTest()
    {
    }

    private MountedMapper encoder = new MountedMapper("/some/mount/path", MockPage.class)
    {
        @Override
        protected MapperContext getContext()
        {
            return context;
        }
    };

    private MountedMapper placeholderEncoder = new MountedMapper("/some/${param1}/path/${param2}",
            MockPage.class)
    {
        @Override
        protected MapperContext getContext()
        {
            return context;
        }
    };

    /**
	 * 
	 */
    public void testDecode1()
    {
        Url url = Url.parse("some/mount/path");
        RequestHandler handler = encoder.mapRequest(getRequest(url));

        assertTrue(handler instanceof RenderPageRequestHandler);
        RequestablePage page = ((RenderPageRequestHandler)handler).getPage();

        assertEquals(0, page.getPageParameters().getIndexedParamsCount());
        assertTrue(page.getPageParameters().getNamedParameterKeys().isEmpty());
    }

    /**
	 * 
	 */
    public void testDecode2()
    {
        Url url = Url.parse("some/mount/path/indexed1?a=b&b=c");
        RequestHandler handler = encoder.mapRequest(getRequest(url));

        assertTrue(handler instanceof RenderPageRequestHandler);
        RequestablePage page = ((RenderPageRequestHandler)handler).getPage();

        PageParameters p = page.getPageParameters();
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
        Url url = Url.parse("some/mount/path?15");
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
        Url url = Url.parse("some/mount/path/i1/i2?15&a=b&b=c");
        RequestHandler handler = encoder.mapRequest(getRequest(url));

        assertTrue(handler instanceof RenderPageRequestHandler);
        RequestablePage page = ((RenderPageRequestHandler)handler).getPage();
        checkPage(page, 15);

        PageParameters p = page.getPageParameters();
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
        Url url = Url.parse("some/mount/path?15-ILinkListener-foo-bar");
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
        Url url = Url.parse("some/mount/path/i1/i2?15-ILinkListener-foo-bar&a=b&b=c");
        RequestHandler handler = encoder.mapRequest(getRequest(url));

        assertTrue(handler instanceof ListenerInterfaceRequestHandler);
        ListenerInterfaceRequestHandler h = (ListenerInterfaceRequestHandler)handler;

        RequestablePage page = h.getPage();
        checkPage(page, 15);

        assertEquals(ILinkListener.INTERFACE, h.getListenerInterface());
        assertEquals("foo:bar", h.getComponent().getPath());

        PageParameters p = page.getPageParameters();
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
        Url url = Url.parse("some/mount/path?15-ILinkListener.4-foo-bar");
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
        Url url = Url.parse("some/mmount/path?15-ILinkListener.4-foo-bar");
        RequestHandler handler = encoder.mapRequest(getRequest(url));

        assertNull(handler);
    }

    /**
	 * 
	 */
    public void testDecode9()
    {
        // capture the home page
        Url url = Url.parse("");
        RequestHandler handler = encoder.mapRequest(getRequest(url));
        assertTrue(handler instanceof RenderPageRequestHandler);
    }

    /**
	 * 
	 */
    public void testDecode10()
    {
        Url url = Url.parse("some/mount/path?15-5.ILinkListener.4-foo-bar");
        context.setNextPageRenderCount(5);
        RequestHandler handler = encoder.mapRequest(getRequest(url));

        assertTrue(handler instanceof ListenerInterfaceRequestHandler);

        ListenerInterfaceRequestHandler h = (ListenerInterfaceRequestHandler)handler;

        RequestablePage page = h.getPage();
        assertEquals(5, page.getRenderCount());
    }

    /**
	 * 
	 */
    public void testDecode11()
    {
        Url url = Url.parse("some/mount/path?15-5.ILinkListener.4-foo-bar");
        context.setNextPageRenderCount(7);

        try
        {
            RequestHandler handler = encoder.mapRequest(getRequest(url));

            ((PageRequestHandler)handler).getPage();

            // should never get here
            assertTrue(false);
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
        PageProvider provider = new PageProvider(MockPage.class, new PageParameters());
        provider.setPageSource(context);
        RequestHandler handler = new BookmarkablePageRequestHandler(provider);
        Url url = encoder.mapHandler(handler);
        assertEquals("some/mount/path", url.toString());
    }

    /**
	 * 
	 */
    public void testEncode2()
    {
        PageParameters parameters = new PageParameters();
        parameters.setIndexedParameter(0, "i1");
        parameters.setIndexedParameter(1, "i2");
        parameters.setNamedParameter("a", "b");
        parameters.setNamedParameter("b", "c");
        PageProvider provider = new PageProvider(MockPage.class, parameters);
        provider.setPageSource(context);
        RequestHandler handler = new BookmarkablePageRequestHandler(provider);
        Url url = encoder.mapHandler(handler);
        assertEquals("some/mount/path/i1/i2?a=b&b=c", url.toString());
    }

    /**
	 * 
	 */
    public void testEncode3()
    {
        PageParameters parameters = new PageParameters();
        parameters.setIndexedParameter(0, "i1");
        parameters.setIndexedParameter(1, "i2");
        parameters.setNamedParameter("a", "b");
        parameters.setNamedParameter("b", "c");

        PageProvider provider = new PageProvider(MockPage.class, parameters);
        provider.setPageSource(context);
        RequestHandler handler = new BookmarkablePageRequestHandler(provider);
        Url url = encoder.mapHandler(handler);

        assertEquals("some/mount/path/i1/i2?a=b&b=c", url.toString());
    }

    /**
	 * 
	 */
    public void testEncode4()
    {
        MockPage page = new MockPage(15);
        page.getPageParameters().setIndexedParameter(0, "i1");
        page.getPageParameters().setIndexedParameter(1, "i2");
        page.getPageParameters().setNamedParameter("a", "b");
        page.getPageParameters().setNamedParameter("b", "c");
        page.setCreatedBookmarkable(true);

        PageProvider provider = new PageProvider(page);
        RequestHandler handler = new RenderPageRequestHandler(provider);
        Url url = encoder.mapHandler(handler);

        assertEquals("some/mount/path/i1/i2?15&a=b&b=c", url.toString());
    }

    /**
	 * 
	 */
    public void testEncode5()
    {
        MockPage page = new MockPage(15);
        page.getPageParameters().setIndexedParameter(0, "i1");
        page.getPageParameters().setIndexedParameter(1, "i2");
        page.getPageParameters().setNamedParameter("a", "b");
        page.getPageParameters().setNamedParameter("b", "c");

        page.setCreatedBookmarkable(false);

        PageProvider provider = new PageProvider(page);
        RequestHandler handler = new RenderPageRequestHandler(provider);
        Url url = encoder.mapHandler(handler);

        // mounted pages must render mounted url even for page that has not been created by
        // bookmarkable
        // URL

        assertEquals("some/mount/path/i1/i2?15&a=b&b=c", url.toString());
    }

    /**
	 * 
	 */
    public void testEncode6()
    {
        MockPage page = new MockPage(15);
        page.getPageParameters().setIndexedParameter(0, "i1");
        page.getPageParameters().setIndexedParameter(1, "i2");
        page.getPageParameters().setNamedParameter("a", "b");
        page.getPageParameters().setNamedParameter("b", "c");
        page.setRenderCount(4);

        // shouldn't make any difference for BookmarkableListenerInterfaceRequestHandler,
        // as this explicitely says the url must be bookmarkable
        page.setCreatedBookmarkable(false);

        RequestableComponent c = page.get("foo:bar");

        PageAndComponentProvider provider = new PageAndComponentProvider(page, c);
        RequestHandler handler = new BookmarkableListenerInterfaceRequestHandler(provider,
                ILinkListener.INTERFACE);

        Url url = encoder.mapHandler(handler);

        assertEquals("some/mount/path/i1/i2?15-4.ILinkListener-foo-bar&a=b&b=c", url.toString());
    }

    /**
	 * 
	 */
    public void testEncode7()
    {
        MockPage page = new MockPage(15);
        page.getPageParameters().setIndexedParameter(0, "i1");
        page.getPageParameters().setIndexedParameter(1, "i2");
        page.getPageParameters().setNamedParameter("a", "b");
        page.getPageParameters().setNamedParameter("b", "c");
        page.setRenderCount(5);

        // shouldn't make any difference for BookmarkableListenerInterfaceRequestHandler,
        // as this explicitely says the url must be bookmarkable
        page.setCreatedBookmarkable(false);

        RequestableComponent c = page.get("foo:bar");

        PageAndComponentProvider provider = new PageAndComponentProvider(page, c);
        RequestHandler handler = new BookmarkableListenerInterfaceRequestHandler(provider,
                ILinkListener.INTERFACE, 4);

        Url url = encoder.mapHandler(handler);

        assertEquals("some/mount/path/i1/i2?15-5.ILinkListener.4-foo-bar&a=b&b=c", url.toString());
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

        PageProvider provider = new PageProvider(page);
        RequestHandler handler = new RenderPageRequestHandler(provider);

        Url url = encoder.mapHandler(handler);

        assertEquals("some/mount/path", url.toString());
    }

    /**
	 * 
	 */
    public void testConstruct1()
    {
        try
        {
            @SuppressWarnings("unused")
            RequestMapper e = new MountedMapper("", MockPage.class);

            // should never get here
            assertFalse(true);
        }
        catch (IllegalArgumentException e)
        {
            // ok
        }
    }

    /**
	 * 
	 */
    public void testConstruct2()
    {
        try
        {
            @SuppressWarnings("unused")
            RequestMapper e = new MountedMapper("/", MockPage.class);

            // should never get here
            assertFalse(true);
        }
        catch (IllegalArgumentException e)
        {
            // ok
        }
    }

    /**
	 * 
	 */
    public void testPlaceholderDecode1()
    {
        Url url = Url.parse("some/p1/path/p2");
        RequestHandler handler = placeholderEncoder.mapRequest(getRequest(url));

        assertTrue(handler instanceof RenderPageRequestHandler);
        RequestablePage page = ((RenderPageRequestHandler)handler).getPage();

        assertEquals(0, page.getPageParameters().getIndexedParamsCount());
        assertTrue(page.getPageParameters().getNamedParameterKeys().size() == 2);
        assertEquals("p1", page.getPageParameters().getNamedParameter("param1").toString());
        assertEquals("p2", page.getPageParameters().getNamedParameter("param2").toString());
    }

    /**
	 * 
	 */
    public void testPlaceholderDecode2()
    {
        Url url = Url.parse("some/p1/path/p2/indexed1?a=b&b=c");
        RequestHandler handler = placeholderEncoder.mapRequest(getRequest(url));

        assertTrue(handler instanceof RenderPageRequestHandler);
        RequestablePage page = ((RenderPageRequestHandler)handler).getPage();

        PageParameters p = page.getPageParameters();
        assertEquals(1, p.getIndexedParamsCount());
        assertEquals("indexed1", p.getIndexedParameter(0).toString());

        assertEquals(4, p.getNamedParameterKeys().size());
        assertEquals("b", p.getNamedParameter("a").toString());
        assertEquals("c", p.getNamedParameter("b").toString());
        assertEquals("p1", page.getPageParameters().getNamedParameter("param1").toString());
        assertEquals("p2", page.getPageParameters().getNamedParameter("param2").toString());
    }

    /**
	 * 
	 */
    public void testPlaceholderEncode2()
    {
        PageParameters parameters = new PageParameters();
        parameters.setIndexedParameter(0, "i1");
        parameters.setIndexedParameter(1, "i2");
        parameters.setNamedParameter("a", "b");
        parameters.setNamedParameter("b", "c");
        parameters.setNamedParameter("param1", "p1");
        parameters.setNamedParameter("param2", "p2");


        PageProvider provider = new PageProvider(MockPage.class, parameters);
        provider.setPageSource(context);
        RequestHandler handler = new BookmarkablePageRequestHandler(provider);
        Url url = placeholderEncoder.mapHandler(handler);
        assertEquals("some/p1/path/p2/i1/i2?a=b&b=c", url.toString());
    }
}

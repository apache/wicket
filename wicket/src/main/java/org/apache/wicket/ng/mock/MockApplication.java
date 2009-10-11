package org.apache.wicket.ng.mock;

import org.apache.wicket.ng.Application;
import org.apache.wicket.ng.page.PageManager;
import org.apache.wicket.ng.request.component.RequestablePage;
import org.apache.wicket.ng.request.cycle.RequestCycleContext;
import org.apache.wicket.ng.request.handler.impl.RenderPageRequestHandler;
import org.apache.wicket.ng.request.handler.impl.render.RenderPageRequestHandlerDelegate;
import org.apache.wicket.ng.request.mapper.BookmarkableMapper;
import org.apache.wicket.ng.request.mapper.PageInstanceMapper;
import org.apache.wicket.ng.request.mapper.ResourceReferenceMapper;
import org.apache.wicket.ng.session.SessionStore;

public class MockApplication extends Application
{

    public MockApplication()
    {
    }

    @Override
    protected void registerDefaultEncoders()
    {
        registerEncoder(new PageInstanceMapper());
        registerEncoder(new BookmarkableMapper());
        registerEncoder(new ResourceReferenceMapper());
    }

    @Override
    public Class< ? extends RequestablePage> getHomePage()
    {
        return null;
    }

    @Override
    protected MockRequestCycle newRequestCycle(RequestCycleContext context)
    {
        return new MockRequestCycle(context);
    }

    @Override
    public RenderPageRequestHandlerDelegate getRenderPageRequestHandlerDelegate(
            RenderPageRequestHandler renderPageRequestHandler)
    {
        return new MockRenderPageRequestHandlerDelegate(renderPageRequestHandler);
    }

    @Override
    protected PageManager newPageManager()
    {
        return new MockPageManager();
    }

    @Override
    protected SessionStore newSessionStore()
    {
        return new MockSessionStore();
    }
}

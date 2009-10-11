package org.apache.wicket.ng.mock;

import org.apache.wicket.ng.request.RequestHandler;
import org.apache.wicket.ng.request.cycle.RequestCycle;
import org.apache.wicket.ng.request.cycle.RequestCycleContext;

public class MockRequestCycle extends RequestCycle
{

    public MockRequestCycle(RequestCycleContext context)
    {
        super(context);
    }

    private RequestHandler forcedRequestHandler;

    public void forceRequestHandler(RequestHandler requestHandler)
    {
        this.forcedRequestHandler = requestHandler;
    }

    @Override
    protected RequestHandler resolveRequestHandler()
    {
        if (forcedRequestHandler != null)
        {
            return forcedRequestHandler;
        }
        else
        {
            return super.resolveRequestHandler();
        }
    }
}

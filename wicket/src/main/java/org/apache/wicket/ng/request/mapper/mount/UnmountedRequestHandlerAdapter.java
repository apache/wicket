package org.apache.wicket.ng.request.mapper.mount;

import org.apache.wicket.ng.request.Request;
import org.apache.wicket.ng.request.RequestHandler;
import org.apache.wicket.ng.request.Url;

/**
 * Adapts a singleton {@link RequestHandler} instance to {@link MountedRequestMapper}
 * 
 * TODO javadoc
 * 
 * @author igor.vaynberg
 * 
 */
class UnmountedRequestHandlerAdapter implements MountedRequestMapper
{
    private final RequestHandler handler;

    public UnmountedRequestHandlerAdapter(RequestHandler handler)
    {
        this.handler = handler;
    }

    public int getCompatibilityScore(Request request)
    {
        return 0;
    }

    public Mount mapHandler(RequestHandler requestHandler)
    {
        if (requestHandler.equals(handler))
        {
            return new Mount(new Url());
        }
        return null;
    }

    public RequestHandler mapRequest(Request request, MountParameters mountParams)
    {
        return handler;
    }

}

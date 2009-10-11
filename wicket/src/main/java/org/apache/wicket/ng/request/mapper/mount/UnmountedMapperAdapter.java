package org.apache.wicket.ng.request.mapper.mount;

import org.apache.wicket.ng.request.Request;
import org.apache.wicket.ng.request.RequestHandler;
import org.apache.wicket.ng.request.RequestMapper;
import org.apache.wicket.ng.request.Url;

/**
 * Adapts a {@link RequestMapper} to be used as a {@link MountedRequestMapper}
 * 
 * TODO javadoc
 * 
 * @author igor.vaynberg
 * 
 */
class UnmountedMapperAdapter implements MountedRequestMapper
{
    private final RequestMapper mapper;

    public UnmountedMapperAdapter(RequestMapper mapper)
    {
        super();
        this.mapper = mapper;
    }

    public int getCompatibilityScore(Request request)
    {
        return mapper.getCompatibilityScore(request);
    }

    public Mount mapHandler(RequestHandler requestHandler)
    {
        Url url = mapper.mapHandler(requestHandler);
        if (url != null)
        {
            return new Mount(url);
        }
        return null;
    }

    public RequestHandler mapRequest(Request request, MountParameters mountParams)
    {
        return mapper.mapRequest(request);
    }
}

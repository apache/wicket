package org.apache.wicket.ng.request.mapper;

import org.apache.wicket.ng.request.Request;
import org.apache.wicket.ng.request.RequestHandler;
import org.apache.wicket.ng.request.RequestMapper;
import org.apache.wicket.ng.request.Url;

/**
 * Mapper that encapsulates mappers that are necessary for Wicket to function.
 * 
 * @author igor.vaynberg
 * 
 */
public class SystemMapper implements RequestMapper
{
    private ThreadsafeCompoundRequestMapper mapper = new ThreadsafeCompoundRequestMapper();

    /**
     * Constructor
     */
    public SystemMapper()
    {
        mapper.register(new PageInstanceMapper());
        mapper.register(new BookmarkableMapper());
        mapper.register(new ResourceReferenceMapper());
        mapper.register(new BufferedResponseMapper());
    }

    /** {@inheritDoc} */
    public int getCompatibilityScore(Request request)
    {
        return mapper.getCompatibilityScore(request);
    }

    /** {@inheritDoc} */
    public Url mapHandler(RequestHandler handler)
    {
        return mapper.mapHandler(handler);
    }

    /** {@inheritDoc} */
    public RequestHandler mapRequest(Request request)
    {
        return mapper.mapRequest(request);
    }


}

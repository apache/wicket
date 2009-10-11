package org.apache.wicket.ng.request.cycle;

import org.apache.wicket.ng.request.Request;
import org.apache.wicket.ng.request.RequestMapper;
import org.apache.wicket.ng.request.response.Response;

/**
 * Represents the context for the request cycle. This class is mainly a grouping parameter for the
 * {@link RequestCycle} constructor. It is only necesary to future-proof the API by making sure
 * method signatures do not change if further parameters are introduced at a later time.
 * <p>
 * NOTE: Once a {@link RequestCycle} is instantiated using an instance of this class, the setters
 * will have no further effect on the request cycle.
 * </p>
 * 
 * @author igor.vaynberg
 */
public final class RequestCycleContext
{
    private Request request;
    private Response response;
    private RequestMapper requestMapper;
    private ExceptionMapper exceptionMapper;

    public RequestCycleContext(Request request, Response response, RequestMapper requestMapper,
            ExceptionMapper exceptionMapper)
    {
        this.request = request;
        this.response = response;
        this.requestMapper = requestMapper;
        this.exceptionMapper = exceptionMapper;
    }

    public Request getRequest()
    {
        return request;
    }

    public Response getResponse()
    {
        return response;
    }


    public RequestMapper getRequestMapper()
    {
        return requestMapper;
    }


    public ExceptionMapper getExceptionMapper()
    {
        return exceptionMapper;
    }

    public void setRequest(Request request)
    {
        this.request = request;
    }

    public void setResponse(Response response)
    {
        this.response = response;
    }

    public void setRequestMapper(RequestMapper requestMapper)
    {
        this.requestMapper = requestMapper;
    }

    public void setExceptionMapper(ExceptionMapper exceptionMapper)
    {
        this.exceptionMapper = exceptionMapper;
    }


}

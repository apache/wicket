package org.apache.wicket.ng.request.cycle;

import org.apache.wicket.ng.request.RequestHandler;

public interface ExceptionMapper
{
    RequestHandler map(Exception e);
}

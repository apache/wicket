package org.apache.wicket.protocol.http;

import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;

/**
 * Test filter using Servlet 3.0 initialization.
 */
@WebFilter(value = "/web/*", initParams = {@WebInitParam(name = "applicationClassName",
value = "org.apache.wicket.mock.MockApplication")})
public class AnnotatedServlet3Filter extends WicketFilter
{
}

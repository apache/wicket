package org.apache.wicket.protocol.ws.api;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.request.http.WebRequest;

/**
 * Common interface for rejecting connections which are not meeting some of the security concerns.
 * One example might be when the connection 'Origin' header does not match the origin of the
 * application host
 * 
 * @author Gergely Nagy
 *
 */
public interface IWebSocketConnectionFilter {

    /**
     * Method for rejecting connections based on the current request
     * 
     * @param servletRequest
     *            The servlet request holding the request headers
     */
    public void doFilter(HttpServletRequest servletRequest);
}

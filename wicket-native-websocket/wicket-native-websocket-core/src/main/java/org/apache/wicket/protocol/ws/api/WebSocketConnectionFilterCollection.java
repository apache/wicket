package org.apache.wicket.protocol.ws.api;

import javax.servlet.http.HttpServletRequest;

public class WebSocketConnectionFilterCollection extends
		FilterCollection<IWebSocketConnectionFilter> implements
		IWebSocketConnectionFilter {

	private static final long serialVersionUID = 3953951891780895469L;

	@Override
	public void doFilter(HttpServletRequest servletRequest) {
		for (IWebSocketConnectionFilter filter : this) {
			filter.doFilter(servletRequest);
		}
	}

}

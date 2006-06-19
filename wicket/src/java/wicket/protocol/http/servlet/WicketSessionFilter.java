/*
 * $Id$
 * $Revision$
 * $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.protocol.http.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Session;

/**
 * <p>
 * This filter can be used to make the Wicket
 * {@link wicket.protocol.http.WebSession} instances available to non-wicket
 * servlets.
 * </p>
 * <p>
 * The following example displays how you can make the Wicket session object of
 * application SessionApplication, mapped on <code>/sessiontest/*</code>
 * available for servlet WicketSessionServlet, mapped under
 * <code>/servlet/sessiontest</code>:
 * 
 * <pre>
 *    &lt;filter&gt;
 *      &lt;filter-name&gt;WicketSessionFilter&lt;/filter-name&gt;
 *      &lt;filter-class&gt;wicket.protocol.http.servlet.WicketSessionFilter&lt;/filter-class&gt;
 *      &lt;init-param&gt;
 *        &lt;param-name&gt;servletPath&lt;/param-name&gt;
 *        &lt;param-value&gt;sessiontest&lt;/param-value&gt;
 *      &lt;/init-param&gt;
 *    &lt;/filter&gt;
 *   
 *    &lt;filter-mapping&gt;
 *      &lt;filter-name&gt;WicketSessionFilter&lt;/filter-name&gt;
 *      &lt;url-pattern&gt;/servlet/sessiontest&lt;/url-pattern&gt;
 *    &lt;/filter-mapping&gt;
 *   
 *    &lt;servlet&gt;
 *      &lt;servlet-name&gt;SessionApplication&lt;/servlet-name&gt;
 *      &lt;servlet-class&gt;wicket.protocol.http.WicketServlet&lt;/servlet-class&gt;
 *      &lt;init-param&gt;
 *        &lt;param-name&gt;applicationClassName&lt;/param-name&gt;
 *        &lt;param-value&gt;session.SessionApplication&lt;/param-value&gt;
 *      &lt;/init-param&gt;
 *      &lt;load-on-startup&gt;1&lt;/load-on-startup&gt;
 *    &lt;/servlet&gt;
 *   
 *    &lt;servlet&gt;
 *      &lt;servlet-name&gt;WicketSessionServlet&lt;/servlet-name&gt;
 *      &lt;servlet-class&gt;session.WicketSessionServlet&lt;/servlet-class&gt;
 *      &lt;load-on-startup&gt;1&lt;/load-on-startup&gt;
 *    &lt;/servlet&gt;
 *   
 *    &lt;servlet-mapping&gt;
 *      &lt;servlet-name&gt;SessionApplication&lt;/servlet-name&gt;
 *      &lt;url-pattern&gt;/sessiontest/*&lt;/url-pattern&gt;
 *    &lt;/servlet-mapping&gt;
 *   
 *    &lt;servlet-mapping&gt;
 *      &lt;servlet-name&gt;WicketSessionServlet&lt;/servlet-name&gt;
 *      &lt;url-pattern&gt;/servlet/sessiontest&lt;/url-pattern&gt;
 *    &lt;/servlet-mapping&gt;
 * </pre>
 * 
 * After that, you can get to the Wicket session in the usual fashion:
 * 
 * <pre>
 * wicket.Session wicketSession = wicket.Session.get();
 * </pre>
 * 
 * </p>
 * 
 * @author Eelco Hillenius
 */
public class WicketSessionFilter implements Filter
{
	/** log. */
	private static final Log log = LogFactory.getLog(WicketSessionFilter.class);

	/** the servlet path. */
	private String servletPath;

	/** the session key where the Wicket session should be stored. */
	private String sessionKey;

	/**
	 * Construct.
	 */
	public WicketSessionFilter()
	{
	}

	/**
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig filterConfig) throws ServletException
	{
		servletPath = filterConfig.getInitParameter("servletPath");

		if (servletPath == null)
		{
			throw new ServletException(
					"you must provide init parameter servlet-path if you want to use "
							+ getClass().getName());
		}

		if (servletPath.charAt(0) != '/')
		{
			servletPath = '/' + servletPath;
		}

		if (log.isDebugEnabled())
		{
			log.debug("servlet path set to " + servletPath);
		}

		sessionKey = "wicket:" + servletPath + ":" + Session.SESSION_ATTRIBUTE_NAME;

		if (log.isDebugEnabled())
		{
			log.debug("will use " + sessionKey + " as the session key to get the Wicket session");
		}
	}

	/**
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
	 *      javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException
	{
		HttpServletRequest httpServletRequest = ((HttpServletRequest)request);
		HttpSession httpSession = httpServletRequest.getSession(false);
		if (httpSession != null)
		{
			Session session = (Session)httpSession.getAttribute(sessionKey);
			if (session != null)
			{
				// set the session's threadlocal
				Session.set(session);

				if (log.isDebugEnabled())
				{
					log.debug("session " + session + " set as current for "
							+ httpServletRequest.getContextPath() + ","
							+ httpServletRequest.getServerName());
				}
			}
			else
			{
				if (log.isDebugEnabled())
				{
					log.debug("could not set Wicket session: key " + sessionKey
							+ " not found in http session for "
							+ httpServletRequest.getContextPath() + ","
							+ httpServletRequest.getServerName());
				}
			}
		}
		else
		{
			if (log.isDebugEnabled())
			{
				log.debug("could not set Wicket session: no http session was created yet for "
						+ httpServletRequest.getContextPath() + ","
						+ httpServletRequest.getServerName());
			}
		}

		// go on with processing
		chain.doFilter(request, response);
		
		// clean up
		Session.set(null);
	}

	/**
	 * @see javax.servlet.Filter#destroy()
	 */
	public void destroy()
	{
	}
}

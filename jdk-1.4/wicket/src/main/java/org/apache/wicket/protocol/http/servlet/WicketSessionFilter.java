/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.protocol.http.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.wicket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * This filter can be used to make the Wicket {@link org.apache.wicket.protocol.http.WebSession}
 * instances available to non-wicket servlets.
 * </p>
 * <p>
 * The following example shows how this filter is setup to for a servlet. You can find the example
 * in the wicket-examples project.
 * 
 * <pre>
 *  &lt;!-- The WicketSesionFilter can be used to provide thread local access to servlets/ JSPs/ etc --&gt;
 *  &lt;filter&gt;
 *    &lt;filter-name&gt;WicketSessionFilter&lt;/filter-name&gt;
 *    &lt;filter-class&gt;org.apache.wicket.protocol.http.servlet.WicketSessionFilter&lt;/filter-class&gt;
 *    &lt;init-param&gt;
 *      &lt;param-name&gt;filterName&lt;/param-name&gt;
 *      &lt;!-- expose the session of the input example app --&gt;
 *      &lt;param-value&gt;FormInputApplication&lt;/param-value&gt;
 *    &lt;/init-param&gt;
 *  &lt;/filter&gt;
 * 
 *  &lt;!-- couple the session filter to the helloworld servlet --&gt;
 *  &lt;filter-mapping&gt;
 *    &lt;filter-name&gt;WicketSessionFilter&lt;/filter-name&gt;
 *    &lt;url-pattern&gt;/helloworldservlet/*&lt;/url-pattern&gt;
 *  &lt;/filter-mapping&gt;
 *  ...
 * 
 *  &lt;servlet&gt;
 *    &lt;servlet-name&gt;HelloWorldServlet&lt;/servlet-name&gt;
 *    &lt;servlet-class&gt;org.apache.wicket.examples.HelloWorldServlet&lt;/servlet-class&gt;
 *  &lt;/servlet&gt;
 * 
 *  &lt;servlet-mapping&gt;
 *    &lt;servlet-name&gt;HelloWorldServlet&lt;/servlet-name&gt;
 *    &lt;url-pattern&gt;/helloworldservlet/*&lt;/url-pattern&gt;
 *  &lt;/servlet-mapping&gt;
 * </pre>
 * 
 * After that, you can get to the Wicket session in the usual fashion:
 * 
 * <pre>
 * Session wicketSession = Session.get();
 * </pre>
 * 
 * like the HelloWorldServlet does:
 * 
 * <pre>
 * public class HelloWorldServlet extends HttpServlet
 * {
 * 	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException,
 * 		IOException
 * 	{
 * 		res.setContentType(&quot;text/html&quot;);
 * 		PrintWriter out = res.getWriter();
 * 		String message = &quot;Hi. &quot; +
 * 			(Session.exists() ? &quot; I know Wicket session &quot; + Session.get() + &quot;.&quot;
 * 				: &quot; I can't find a Wicket session.&quot;);
 * 		out.println(message);
 * 		out.close();
 * 	}
 * }
 * </pre>
 * 
 * </p>
 * 
 * @author Eelco Hillenius
 */
public class WicketSessionFilter implements Filter
{
	/** log. */
	private static final Logger log = LoggerFactory.getLogger(WicketSessionFilter.class);

	private FilterConfig filterConfig;

	/** the filter name/ application key. */
	private String filterName;

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
		this.filterConfig = filterConfig;

		filterName = filterConfig.getInitParameter("filterName");

		if (filterName == null)
		{
			throw new ServletException(
				"you must provide init parameter 'filterName if you want to use " +
					getClass().getName());
		}

		if (log.isDebugEnabled())
		{
			log.debug("filterName/ application key set to " + filterName);
		}

		sessionKey = "wicket:" + filterName + ":" + Session.SESSION_ATTRIBUTE_NAME;

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
					log.debug("session " + session + " set as current for " +
						httpServletRequest.getContextPath() + "," +
						httpServletRequest.getServerName());
				}
			}
			else
			{
				if (log.isDebugEnabled())
				{
					log.debug("could not set Wicket session: key " + sessionKey +
						" not found in http session for " + httpServletRequest.getContextPath() +
						"," + httpServletRequest.getServerName());
				}
			}
		}
		else
		{
			if (log.isDebugEnabled())
			{
				log.debug("could not set Wicket session: no http session was created yet for " +
					httpServletRequest.getContextPath() + "," + httpServletRequest.getServerName());
			}
		}

		try
		{
			// go on with processing
			chain.doFilter(request, response);
		}
		finally
		{
			Session.unset();
		}
	}

	/**
	 * @see javax.servlet.Filter#destroy()
	 */
	public void destroy()
	{
	}
}

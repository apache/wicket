/*
 * $Id$ $Revision$
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
package wicket.protocol.http;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;

/**
 * The set character encoding filter provides a simple way
 * for application developers to set the character encoding
 * of their application. This only needs to be set if the application
 * wishes to use an encoding other than the default for the
 * platform on which the application server is running.
 * <p>
 * To change the character encoding for the Wicket application
 * the following entries need to be added to the <code>web.xml</code>
 * deployment descriptor:
 *
 * <pre>
 *          &lt;filter&gt;
 *              &lt;filter-name&gt;SetCharacterEncoding&lt;/filter-name&gt;
 *              &lt;filter-class&gt;wicket.protocol.http.SetCharacterEncodingFilter&lt;/filter-class&gt;
 *              &lt;init-param&gt;
 *                  &lt;param-name&gt;encoding&lt;/param-name&gt;
 *                  &lt;param-value&gt;UTF-8&lt;/param-value&gt;
 *              &lt;/init-param&gt;
 *          &lt;/filter&gt;
 *          &lt;filter-mapping&gt;
 *              &lt;filter-name&gt;SetCharacterEncoding&lt;/filter-name&gt;
 *              &lt;servlet-name &gt;WicketServletName&lt;/servlet-name&gt;
 *              OR
 *              &lt;url-pattern&gt;/apppath/*&lt;/url-pattern&gt;
 *          &lt;/filter-mapping&gt;
 * </pre>
 *
 * @author Chris Turner
 */
public class SetCharacterEncodingFilter implements Filter
{

	/**
	 * Default character encoding if the filter is not configured to
	 * support an alternative. By having a default value of
	 * <code>null</code> the use of the platforms standard character
	 * encoding is enforced.
	 */
	private String encoding = null;

	/**
	 * Initialises the filter.
	 *
	 * @param filterConfig The config object
	 */
	public final void init(final FilterConfig filterConfig) {
	    final String encodingValue = filterConfig.getInitParameter("encoding");
	    if ( encodingValue != null && encodingValue.length() > 0 )
	    {
	        encoding = encodingValue;
	    }
	}

	/**
	 * Destroys the filter.
	 */
	public final void destroy() {
	}

	/**
	 * Modifies the request to set the character encoding.
	 *
	 * @param servletRequest The servletRequest
	 * @param servletResponse The servletResponse
	 * @param filterChain The chain of filters being processed
	 * @throws java.io.IOException If an error occurs writing output
	 * @throws javax.servlet.ServletException If an error occurs during processing
	 */
	public final void doFilter(final ServletRequest servletRequest,
	                           final ServletResponse servletResponse,
	                           final FilterChain filterChain)
	        throws IOException, ServletException {
		if ( encoding != null )
		{
	        servletRequest.setCharacterEncoding(encoding);
		}
	    filterChain.doFilter(servletRequest, servletResponse);
	}
}

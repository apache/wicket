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
package wicket.spring;

import java.util.Map;

import javax.servlet.ServletContext;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import wicket.protocol.http.IWebApplicationFactory;
import wicket.protocol.http.WebApplication;
import wicket.protocol.http.WicketFilter;

/**
 * Implementation of IWebApplicationFactory that pulls the WebApplication object
 * out of spring application context.
 * 
 * Configuration example:
 * 
 * <pre>
 *    &lt;servlet&gt;
 *    &lt;servlet-name&gt;phonebook&lt;/servlet-name&gt;
 *    &lt;servlet-class&gt;wicket.protocol.http.WicketServlet&lt;/servlet-class&gt;
 *    &lt;init-param&gt;
 *    &lt;param-name&gt;applicationFactoryClassName&lt;/param-name&gt;
 *    &lt;param-value&gt;wicket.contrib.spring.SpringWebApplicationFactory&lt;/param-value&gt;
 *    &lt;/init-param&gt;
 *    &lt;load-on-startup&gt;1&lt;/load-on-startup&gt;
 *    &lt;/servlet&gt;
 * </pre>
 * 
 * beanName init parameter can be used if there are multiple WebApplications
 * defined on the spring application context.
 * 
 * Example:
 * 
 * <pre>
 *    &lt;servlet&gt;
 *    &lt;servlet-name&gt;phonebook&lt;/servlet-name&gt;
 *    &lt;servlet-class&gt;wicket.protocol.http.WicketServlet&lt;/servlet-class&gt;
 *    &lt;init-param&gt;
 *    &lt;param-name&gt;applicationFactoryClassName&lt;/param-name&gt;
 *    &lt;param-value&gt;wicket.contrib.spring.SpringWebApplicationFactory&lt;/param-value&gt;
 *    &lt;/init-param&gt;
 *    &lt;init-param&gt;
 *    &lt;param-name&gt;beanName&lt;/param-name&gt;
 *    &lt;param-value&gt;phonebookApplication&lt;/param-value&gt;
 *    &lt;/init-param&gt;
 *    &lt;load-on-startup&gt;1&lt;/load-on-startup&gt;
 *    &lt;/servlet&gt;
 * </pre>
 * 
 * 
 * @author Igor Vaynberg (ivaynberg)
 * @author Janne Hietam&auml;ki (jannehietamaki)
 * 
 */
public class SpringWebApplicationFactory implements IWebApplicationFactory
{
	/**
	 * @see IWebApplicationFactory#createApplication(WicketFilter)
	 */
	public WebApplication createApplication(WicketFilter filter)
	{
		ServletContext sc = filter.getFilterConfig().getServletContext();
		ApplicationContext ac = WebApplicationContextUtils
				.getRequiredWebApplicationContext(sc);

		String beanName = filter.getFilterConfig().getInitParameter("applicationBean");
		return createApplication(ac, beanName);
	}

	private WebApplication createApplication(ApplicationContext ac, String beanName)
	{
		if (beanName != null)
		{
			WebApplication application = (WebApplication) ac.getBean(beanName);
			if (application == null)
			{
				throw new IllegalArgumentException(
						"Unable to find WebApplication bean with name [" + beanName + "]");
			}
			return application;
		}
		else
		{
			Map beans = ac.getBeansOfType(WebApplication.class, false, false);
			if (beans.size() == 0)
			{
				throw new IllegalStateException("bean of type ["
						+ WebApplication.class.getName() + "] not found");
			}
			if (beans.size() > 1)
			{
				throw new IllegalStateException("more then one bean of type ["
						+ WebApplication.class.getName() + "] found, must have only one");
			}
			return (WebApplication) beans.values().iterator().next();
		}
	}
}
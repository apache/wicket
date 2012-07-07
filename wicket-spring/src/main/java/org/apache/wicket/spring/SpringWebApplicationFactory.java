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
package org.apache.wicket.spring;

import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.wicket.protocol.http.IWebApplicationFactory;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WicketFilter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * Implementation of IWebApplicationFactory that pulls the WebApplication object out of spring
 * application context.
 * 
 * Configuration example:
 * 
 * <pre>
 * &lt;filter&gt;
 *   &lt;filter-name&gt;MyApplication&lt;/filter-name&gt;
 *   &lt;filter-class>org.apache.wicket.protocol.http.WicketFilter&lt;/filter-class&gt;
 *   &lt;init-param&gt;
 *     &lt;param-name&gt;applicationFactoryClassName&lt;/param-name&gt;
 *     &lt;param-value&gt;org.apache.wicket.spring.SpringWebApplicationFactory&lt;/param-value&gt;
 *   &lt;/init-param&gt;
 * &lt;/filter&gt;
 * </pre>
 * 
 * <code>applicationBean</code> init parameter can be used if there are multiple WebApplications
 * defined on the spring application context.
 * 
 * Example:
 * 
 * <pre>
 * &lt;filter&gt;
 *   &lt;filter-name&gt;MyApplication&lt;/filter-name&gt;
 *   &lt;filter-class>org.apache.wicket.protocol.http.WicketFilter&lt;/filter-class&gt;
 *   &lt;init-param&gt;
 *     &lt;param-name&gt;applicationFactoryClassName&lt;/param-name&gt;
 *     &lt;param-value&gt;org.apache.wicket.spring.SpringWebApplicationFactory&lt;/param-value&gt;
 *   &lt;/init-param&gt;
 *   &lt;init-param&gt;
 *     &lt;param-name&gt;applicationBean&lt;/param-name&gt;
 *     &lt;param-value&gt;phonebookApplication&lt;/param-value&gt;
 *   &lt;/init-param&gt;
 * &lt;/filter&gt;
 * </pre>
 * 
 * <p>
 * This factory is also capable of creating an additional application context (path to which is
 * specified via the {@code contextConfigLocation} filter param) and chaining it to the global one
 * </p>
 * 
 * <pre>
 * &lt;filter&gt;
 *   &lt;filter-name&gt;MyApplication&lt;/filter-name&gt;
 *   &lt;filter-class>org.apache.wicket.protocol.http.WicketFilter&lt;/filter-class&gt;
 *   &lt;init-param&gt;
 *     &lt;param-name&gt;applicationFactoryClassName&lt;/param-name&gt;
 *     &lt;param-value&gt;org.apache.wicket.spring.SpringWebApplicationFactory&lt;/param-value&gt;
 *   &lt;/init-param&gt;
 *   &lt;init-param&gt;
 *     &lt;param-name&gt;contextConfigLocation&lt;/param-name&gt;
 *     &lt;param-value&gt;classpath:com/myapplication/customers-app/context.xml&lt;/param-value&gt;
 *   &lt;/init-param&gt;
 * &lt;/filter&gt;
 * </pre>
 * 
 * @author Igor Vaynberg (ivaynberg)
 * @author Janne Hietam&auml;ki (jannehietamaki)
 * 
 */
public class SpringWebApplicationFactory implements IWebApplicationFactory
{

	/** additional context created for this filter, if any */
	private ConfigurableWebApplicationContext additionalContext;

	/**
	 * Returns location of context config that will be used to create an additional application
	 * context for this application
	 * 
	 * @param filter
	 * @return location of context config
	 */
	protected final String getContextConfigLocation(final WicketFilter filter)
	{
		return filter.getFilterConfig().getInitParameter("contextConfigLocation");
	}

	/**
	 * Factory method used to create a new instance of the additional application context, by
	 * default an instance o {@link XmlWebApplicationContext} will be created.
	 * 
	 * @return application context instance
	 */
	protected ConfigurableWebApplicationContext newApplicationContext()
	{
		return new XmlWebApplicationContext();
	}

	/**
	 * @see IWebApplicationFactory#createApplication(WicketFilter)
	 */
	@Override
	public WebApplication createApplication(final WicketFilter filter)
	{
		ServletContext sc = filter.getFilterConfig().getServletContext();

		WebApplicationContext ac = WebApplicationContextUtils.getWebApplicationContext(sc);

		if (getContextConfigLocation(filter) != null)
		{
			additionalContext = createWebApplicationContext(ac, filter);
		}

		String beanName = filter.getFilterConfig().getInitParameter("applicationBean");
		return createApplication((additionalContext != null) ? additionalContext : ac, beanName);
	}

	private WebApplication createApplication(final ApplicationContext ac, final String beanName)
	{
		if (beanName != null)
		{
			WebApplication application = (WebApplication)ac.getBean(beanName);
			if (application == null)
			{
				throw new IllegalArgumentException(
					"Unable to find WebApplication bean with name [" + beanName + "]");
			}
			return application;
		}
		else
		{
			Map<?, ?> beans = BeanFactoryUtils.beansOfTypeIncludingAncestors(ac,
				WebApplication.class, false, false);
			if (beans.size() == 0)
			{
				throw new IllegalStateException("bean of type [" + WebApplication.class.getName() +
					"] not found");
			}
			if (beans.size() > 1)
			{
				throw new IllegalStateException("More than one bean of type [" +
					WebApplication.class.getName() + "] found, must have only one");
			}
			return (WebApplication)beans.values().iterator().next();
		}
	}

	/**
	 * Creates and initializes a new {@link WebApplicationContext}, with the given context as the
	 * parent. Based on the logic in {@link FrameworkServlet#createWebApplicationContext}
	 * 
	 * @param parent
	 *            parent application context
	 * @param filter
	 *            wicket filter
	 * @return instance of additional application context
	 * @throws BeansException
	 */
	protected final ConfigurableWebApplicationContext createWebApplicationContext(
		final WebApplicationContext parent, final WicketFilter filter) throws BeansException
	{
		ConfigurableWebApplicationContext wac = newApplicationContext();
		wac.setParent(parent);
		wac.setServletContext(filter.getFilterConfig().getServletContext());
		wac.setConfigLocation(getContextConfigLocation(filter));

		postProcessWebApplicationContext(wac, filter);
		wac.refresh();

		return wac;
	}

	/**
	 * This is a hook for potential subclasses to perform additional processing on the context.
	 * Based on the logic in {@link FrameworkServlet#postProcessWebApplicationContext}
	 * 
	 * @param wac
	 *            additional application context
	 * @param filter
	 *            wicket filter
	 */
	protected void postProcessWebApplicationContext(final ConfigurableWebApplicationContext wac,
		final WicketFilter filter)
	{
		// noop
	}

	/** {@inheritDoc} */
	@Override
	public void destroy(final WicketFilter filter)
	{
		if (additionalContext != null)
		{
			additionalContext.close();
		}
	}
}

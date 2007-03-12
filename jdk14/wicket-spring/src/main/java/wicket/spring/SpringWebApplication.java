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
package wicket.spring;

import javax.servlet.ServletContext;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.support.WebApplicationContextUtils;

import wicket.Application;
import wicket.protocol.http.WebApplication;
import wicket.proxy.LazyInitProxyFactory;

/**
 * Base class for spring aware wicket web application object. This class has
 * helper methods to create lazy init proxies based on spring beans, as well as
 * an implementation of {@link ISpringContextLocator}.
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public abstract class SpringWebApplication extends WebApplication implements
		ApplicationContextAware
{
	private ApplicationContext applicationContext;

	/**
	 * Singleton instance of spring application context locator
	 */
	private final static ISpringContextLocator contextLocator = new ISpringContextLocator()
	{

		public ApplicationContext getSpringContext()
		{
			Application app = Application.get();
			return ((SpringWebApplication) app).internalGetApplicationContext();
		}
	};

	/**
	 * Construct.
	 */
	public SpringWebApplication()
	{

	}

	/**
	 * @see wicket.Application#internalInit()
	 */
	protected void internalInit()
	{
		super.internalInit();

		if (applicationContext == null)
		{
			// this application was not created as a spring bean so we
			// locate the app context from servlet context

			ServletContext sc = getServletContext();
			applicationContext = WebApplicationContextUtils
					.getRequiredWebApplicationContext(sc);

		}
	}

	/**
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
	public final void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException
	{
		this.applicationContext = applicationContext;
	}

	/**
	 * Retrieves the spring application context associated with this application
	 * object
	 * 
	 * This method is protected and named internalGetApplicationContext so that
	 * the subclass can choose whether or not to add a public
	 * getApplicationContext() method
	 * 
	 * @return spring application context
	 */
	protected final ApplicationContext internalGetApplicationContext()
	{
		return applicationContext;
	}

	/**
	 * Retrieves the spring application context locator object
	 * 
	 * @return spring application context locator object
	 */
	public ISpringContextLocator getSpringContextLocator()
	{
		return contextLocator;
	}

	/**
	 * Creates a proxy for a spring bean that is safe to put into session and
	 * serialize
	 * 
	 * @param clazz
	 *            class of spring bean
	 * @param beanName
	 *            name of spring bean
	 * @return proxy representing the spring bean
	 */
	protected Object createSpringBeanProxy(Class clazz, String beanName)
	{
		return LazyInitProxyFactory.createProxy(clazz, new SpringBeanLocator(beanName,
				clazz, getSpringContextLocator()));
	}

	/**
	 * Creates a proxy for a spring bean that is safe to put into session and
	 * serialize
	 * 
	 * @param clazz
	 *            class of spring bean
	 * @return proxy representing the spring bean
	 */
	protected Object createSpringBeanProxy(Class clazz)
	{
		return LazyInitProxyFactory.createProxy(clazz, new SpringBeanLocator(clazz,
				getSpringContextLocator()));
	}

}

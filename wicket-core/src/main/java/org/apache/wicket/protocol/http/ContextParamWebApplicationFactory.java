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
package org.apache.wicket.protocol.http;

import java.lang.reflect.InvocationTargetException;

import org.apache.wicket.WicketRuntimeException;

/**
 * Factory that creates application objects based on the class name specified in the
 * {@link ContextParamWebApplicationFactory#APP_CLASS_PARAM} context variable.
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class ContextParamWebApplicationFactory implements IWebApplicationFactory
{
	/**
	 * context parameter name that must contain the class name of the application
	 */
	public static final String APP_CLASS_PARAM = "applicationClassName";

	/* @see IWebApplicationFactory#createApplication(WicketFilter) */
	@Override
	public WebApplication createApplication(WicketFilter filter)
	{
		final String applicationClassName = filter.getFilterConfig().getInitParameter(
			APP_CLASS_PARAM);

		if (applicationClassName == null)
		{
			throw new WicketRuntimeException(
				"servlet init param [" +
					APP_CLASS_PARAM +
					"] is missing. If you are trying to use your own implementation of IWebApplicationFactory and get this message then the servlet init param [" +
					WicketFilter.APP_FACT_PARAM + "] is missing");
		}

		return createApplication(applicationClassName);
	}

	/**
	 * Instantiates the application instance.
	 * 
	 * @param applicationClassName
	 *            the classname of the application to create
	 * @return the web application
	 */
	protected WebApplication createApplication(final String applicationClassName)
	{
		try
		{
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			if (loader == null)
			{
				loader = getClass().getClassLoader();
			}

			// see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6500212
			// final Class<?> applicationClass = loader.loadClass(applicationClassName);
			final Class<?> applicationClass = Class.forName(applicationClassName, false, loader);
			if (WebApplication.class.isAssignableFrom(applicationClass))
			{
				// Construct WebApplication subclass
				return (WebApplication)applicationClass.getDeclaredConstructor().newInstance();
			}
			else
			{
				throw new WicketRuntimeException("Application class " + applicationClassName +
					" must be a subclass of WebApplication");
			}
		}
		catch (ClassNotFoundException | InstantiationException | IllegalAccessException | SecurityException
				| NoSuchMethodException | InvocationTargetException e)
		{
			throw new WicketRuntimeException("Unable to create application of class " +
				applicationClassName, e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void destroy(WicketFilter filter)
	{
	}

}

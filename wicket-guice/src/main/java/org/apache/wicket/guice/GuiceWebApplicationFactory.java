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
package org.apache.wicket.guice;

import javax.servlet.ServletContext;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import org.apache.wicket.protocol.http.IWebApplicationFactory;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WicketFilter;
import org.apache.wicket.util.string.Strings;


/**
 * Implementation of IWebApplicationFactory that pulls the WebApplication object out of a Guice
 * Module.
 * 
 * Configuration example:
 * 
 * <pre>
 * &lt;filter&gt;
 *   &lt;filter-name&gt;MyApplication&lt;/filter-name&gt;
 *   &lt;filter-class&gt;org.apache.wicket.protocol.http.WicketFilter&lt;/filter-class&gt;
 *   &lt;init-param&gt;
 *     &lt;param-name&gt;applicationFactoryClassName&lt;/param-name&gt;
 *     &lt;param-value&gt;org.apache.wicket.guice.GuiceWebApplicationFactory&lt;/param-value&gt;
 *   &lt;/init-param&gt;
 *   &lt;init-param&gt;
 *     &lt;param-name&gt;module&lt;/param-name&gt;
 *     &lt;param-value&gt;com.company.MyGuiceModule,com.company.MyOtherGuiceModule&lt;/param-value&gt;
 *   &lt;/init-param&gt;
 *   &lt;init-param&gt;
 *     &lt;param-name&gt;wicket-guice.stage&lt;/param-name&gt;
 *     &lt;param-value&gt;DEVELOPMENT&lt;/param-value&gt;
 *   &lt;/init-param&gt;
 * &lt;/filter&gt;
 * </pre>
 * 
 * This factory will create an Injector configured using the Guice Module implementation you pass it
 * above. Multiple modules can be specified by naming multiple classes separated by a comma. The
 * Guice Module (MyGuiceModule in the example above) needs to bind WebApplication.class and provide
 * a concrete implementation of it.
 * 
 * The stage used when creating the Injector may be specified by the optional wicket-guice.stage
 * parameter. When this parameter is not present this factory does not use specify a Stage when
 * creating the Injector. This parameter can also be set as a context parameter to provide
 * configuration for all instances in the web application.
 * 
 * Alternatively, you can dig the Injector out of the ServletContext as an attribute, like so:
 * 
 * <pre>
 * &lt;filter&gt;
 *   &lt;filter-name&gt;MyApplication&lt;/filter-name&gt;
 *   &lt;filter-class&gt;org.apache.wicket.protocol.http.WicketFilter&lt;/filter-class&gt;
 *   &lt;init-param&gt;
 *     &lt;param-name&gt;applicationFactoryClassName&lt;/param-name&gt;
 *     &lt;param-value&gt;org.apache.wicket.guice.GuiceWebApplicationFactory&lt;/param-value&gt;
 *   &lt;/init-param&gt;
 *   &lt;init-param&gt;
 *     &lt;param-name&gt;injectorContextAttribute&lt;/param-name&gt;
 *     &lt;param-value&gt;GuiceInjector&lt;/param-value&gt;
 *   &lt;/init-param&gt;
 * &lt;/filter&gt;
 * </pre>
 * 
 * <b>NB: You no longer have to add a GuiceComponentInjector manually in your
 * {@link WebApplication#init()} method - this factory will do that for you automatically.</b>
 * 
 * @author Alastair Maw (almaw)
 * 
 */
public class GuiceWebApplicationFactory implements IWebApplicationFactory
{
	/** */
	public static final String STAGE_PARAMETER = "wicket-guice.stage";

	/**
	 * @see IWebApplicationFactory#createApplication(WicketFilter)
	 */
	public WebApplication createApplication(final WicketFilter filter)
	{
		Injector injector;

		String injectorContextAttribute = filter.getFilterConfig().getInitParameter(
			"injectorContextAttribute");

		Stage stage = null;

		String stageContextAttribute = filter.getFilterConfig().getInitParameter(STAGE_PARAMETER);
		if (stageContextAttribute == null)
		{
			stageContextAttribute = filter.getFilterConfig()
				.getServletContext()
				.getInitParameter(STAGE_PARAMETER);
		}
		if (stageContextAttribute != null)
		{
			stage = Stage.valueOf(stageContextAttribute.trim());
		}

		if (injectorContextAttribute != null)
		{
			ServletContext sc = filter.getFilterConfig().getServletContext();

			// Try to dig the Injector out of the ServletContext, for integration with context
			// listener-based instantiation of Guice.
			injector = (Injector)sc.getAttribute(injectorContextAttribute);
			if (injector == null)
			{
				throw new RuntimeException(
					"Could not find Guice Injector in the ServletContext under attribute: " +
						injectorContextAttribute);
			}
		}
		else if (filter.getFilterConfig().getInitParameter("module") != null)
		{
			String paramValue = filter.getFilterConfig().getInitParameter("module");
			String moduleNames[] = Strings.split(paramValue, ',');
			Module modules[] = new Module[moduleNames.length];
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			for (int i = 0; i < moduleNames.length; i++)
			{
				String moduleName = moduleNames[i].trim();
				try
				{
					// see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6500212
					Class<?> moduleClazz = Class.forName(moduleName, false, classLoader);
					Object moduleObject = moduleClazz.newInstance();
					modules[i] = (Module)moduleObject;
				}
				catch (InstantiationException e)
				{
					throw new RuntimeException(
						"Could not create new instance of Guice Module class " + moduleName, e);
				}
				catch (ClassNotFoundException e)
				{
					throw new RuntimeException(
						"Could not create new instance of Guice Module class " + moduleName, e);
				}
				catch (IllegalAccessException e)
				{
					throw new RuntimeException(
						"Could not create new instance of Guice Module class " + moduleName, e);
				}
			}
			if (stage != null)
			{
				injector = Guice.createInjector(stage, modules);
			}
			else
			{
				injector = Guice.createInjector(modules);
			}
		}
		else
		{
			throw new RuntimeException(
				"To use GuiceWebApplicationFactory, you must specify either an 'injectorContextAttribute' or a 'module' init-param.");
		}
		WebApplication app = injector.getInstance(WebApplication.class);
		app.getComponentInstantiationListeners().add(new GuiceComponentInjector(app, injector));
		return app;
	}

	/** {@inheritDoc} */
	public void destroy(final WicketFilter filter)
	{
	}
}

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

import org.apache.wicket.protocol.http.IWebApplicationFactory;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WicketFilter;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;


/**
 * Implementation of IWebApplicationFactory that pulls the WebApplication object out of a Guice
 * Module.
 * 
 * Configuration example:
 * 
 * <pre>
 *    &lt;servlet&gt;
 *      &lt;servlet-name&gt;myApp&lt;/servlet-name&gt;
 *      &lt;servlet-class&gt;org.apache.wicket.protocol.http.WicketServlet&lt;/servlet-class&gt;
 *      &lt;init-param&gt;
 *        &lt;param-name&gt;applicationFactoryClassName&lt;/param-name&gt;
 *        &lt;param-value&gt;org.apache.wicket.guice.GuiceWebApplicationFactory&lt;/param-value&gt;
 *      &lt;/init-param&gt;
 *      &lt;init-param&gt;
 *        &lt;param-name&gt;module&lt;/param-name&gt;
 *        &lt;param-value&gt;com.company.MyGuiceModule&lt;/param-value&gt;
 *      &lt;/init-param&gt;
 *      &lt;load-on-startup&gt;1&lt;/load-on-startup&gt;
 *    &lt;/servlet&gt;
 * </pre>
 * 
 * This factory will create an Injector configured using the Guice Module implementation you pass it
 * above. The Guice Module (MyGuiceModule in the example above) needs to bind WebApplication.class
 * and provide a concrete implementation of it.
 * 
 * Alternatively, you can dig the Injector out of the ServletContext as an attribute, like so:
 * 
 * <pre>
 *    &lt;servlet&gt;
 *      &lt;servlet-name&gt;myApp&lt;/servlet-name&gt;
 *      &lt;servlet-class&gt;org.apache.wicket.protocol.http.WicketServlet&lt;/servlet-class&gt;
 *      &lt;init-param&gt;
 *        &lt;param-name&gt;applicationFactoryClassName&lt;/param-name&gt;
 *        &lt;param-value&gt;org.apache.wicket.guice.GuiceWebApplicationFactory&lt;/param-value&gt;
 *      &lt;/init-param&gt;
 *      &lt;init-param&gt;
 *        &lt;param-name&gt;injectorContextAttribute&lt;/param-name&gt;
 *        &lt;param-value&gt;GuiceInjector&lt;/param-value&gt;
 *      &lt;/init-param&gt;
 *      &lt;load-on-startup&gt;1&lt;/load-on-startup&gt;
 *    &lt;/servlet&gt;
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
	/**
	 * @see IWebApplicationFactory#createApplication(WicketFilter)
	 */
	public WebApplication createApplication(WicketFilter filter)
	{
		Injector injector;

		String injectorContextAttribute = filter.getFilterConfig().getInitParameter(
				"injectorContextAttribute");
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
			String moduleName = filter.getFilterConfig().getInitParameter("module");
			try
			{
				Class< ? > moduleClazz = Class.forName(moduleName);
				Object moduleObject = moduleClazz.newInstance();
				Module module = (Module)moduleObject;
				injector = Guice.createInjector(module);
			}
			catch (InstantiationException e)
			{
				throw new RuntimeException("Could not create new instance of Guice Module class " +
						moduleName, e);
			}
			catch (ClassNotFoundException e)
			{
				throw new RuntimeException("Could not create new instance of Guice Module class " +
						moduleName, e);
			}
			catch (IllegalAccessException e)
			{
				throw new RuntimeException("Could not create new instance of Guice Module class " +
						moduleName, e);
			}
		}
		else
		{
			throw new RuntimeException(
					"To use GuiceWebApplicationFactory, you must specify either an 'injectorContextAttribute' or a 'module' init-param.");
		}
		WebApplication app = injector.getInstance(WebApplication.class);
		app.addComponentInstantiationListener(new GuiceComponentInjector(app, injector));
		return app;
	}
}
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

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

import org.apache.wicket.application.ReloadingClassLoader;
import org.apache.wicket.util.listener.IChangeListener;


/**
 * Custom WicketFilter that reloads the web applications when classes are
 * modified. In order to reload your own classes, use include and exclude
 * patterns using wildcards. And in web.xml, point to your custom reloading
 * org.apache.wicket filter instead of the original org.apache.wicket filter.
 * 
 * <p>
 * The built-in patterns are:
 * </p>
 * 
 * <pre>
 * ReloadingClassLoader.excludePattern(&quot;org.apache.wicket.*&quot;);
 * ReloadingClassLoader.includePattern(&quot;org.apache.wicket.examples.*&quot;);
 * </pre>
 * 
 * <p>
 * <b>Example. </b> Defining custom patterns
 * </p>
 * 
 * <pre>
 * public class MyReloadingFilter extends ReloadingWicketFilter
 * {
 * 	static
 * 	{
 * 		ReloadingClassLoader.includePattern(&quot;com.company.*&quot;);
 * 		ReloadingClassLoader.excludePattern(&quot;com.company.spring.beans.*&quot;);
 * 		ReloadingClassLoader.includePattern(&quot;some.other.package.with.wicket.components.*&quot;);
 * 	}
 * }
 * </pre>
 * 
 * <p>
 * <b>Note. </b> The order of including and excluding patterns is significant.
 * </p>
 * 
 * <p>
 * Be sure to carefully read the following information if you also use Spring:
 * </p>
 * 
 * <p>
 * When using Spring, the application must not be a Spring bean itself,
 * otherwise the reloading mechanism won't be able to reload the application. In
 * particular, make sure <b>not</b> to use
 * org.apache.wicket.spring.SpringWebApplicationFactory in web.xml. To inject dependencies
 * in your application, use SpringComponentInjector or
 * DefaultListableBeanFactory.autowireBeanProperties() in the init() method.
 * </p>
 * 
 * <p>
 * <b>WARNING. </b> Be careful that when using Spring or other component
 * managers, you will get <tt>ClassCastException</tt> if a given class is
 * loaded two times, one time by the normal classloader, and another time by the
 * reloading classloader. You need to ensure that your Spring beans are properly
 * excluded from the reloading class loader and only keep the Wicket components
 * included. When getting a cryptic error with regard to class loading, class
 * instantiation or class comparison, first <b>disable the reloading class
 * loader</b> to rule out the possibility of a classloader conflict. Please
 * keep in mind that two classes are equal if and only if they have the same
 * name <b>and are loaded in the same classloader</b>. Same goes for errors
 * like <tt>LinkageError: Class FooBar violates loader constraints</tt>,
 * better be safe and disable the reloading feature.
 * </p>
 * 
 * 
 * <p>
 * It is also possible to add an extra URL to watch for changes using
 * <tt>ReloadingClassLoader.addLocation()</tt> . By default, all the URL
 * returned by the provided class loader are registered.
 * </p>
 * 
 * @see WicketFilter
 * 
 * @author <a href="mailto:jbq@apache.org">Jean-Baptiste Quenot</a>
 */
public class ReloadingWicketFilter extends WicketFilter
{
	private ReloadingClassLoader reloadingClassLoader;

	/**
	 * Instantiate the reloading class loader
	 */
	public ReloadingWicketFilter()
	{
		// Create a reloading classloader
		reloadingClassLoader = new ReloadingClassLoader(getClass().getClassLoader());
	}

	/**
	 * @see org.apache.wicket.protocol.http.WicketFilter#getClassLoader()
	 */
	protected ClassLoader getClassLoader()
	{
		return reloadingClassLoader;
	}

	/**
	 * @see org.apache.wicket.protocol.http.WicketFilter#init(javax.servlet.FilterConfig)
	 */
	public void init(final FilterConfig filterConfig) throws ServletException
	{
		reloadingClassLoader.setListener(new IChangeListener()
		{
			public void onChange()
			{
				reloadingClassLoader = new ReloadingClassLoader(getClass().getClassLoader());
				try
				{
					init(filterConfig);
				}
				catch (ServletException e)
				{
					throw new RuntimeException(e);
				}
			}
		});

		super.init(filterConfig);
	}
}

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

import java.net.URL;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

import org.apache.wicket.Session;
import org.apache.wicket.application.ReloadingClassLoader;
import org.apache.wicket.util.listener.IChangeListener;


/**
 * Custom {@link WicketFilter} that reloads the web applications when classes are modified. In order
 * to monitor changes to your own classes, subclass {@link ReloadingWicketFilter} and use include
 * and exclude patterns using wildcards. And in web.xml, point to your custom
 * {@link ReloadingWicketFilter} instead of the original {@link WicketFilter}.
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
 * <b>NOTE. </b> If you wish to reload <tt>com.company.search.Form</tt>, you have to make sure to
 * include all classes that <b>reference</b> <tt>com.company.search.Form</tt>. In particular, if it
 * is referenced in com.company.Application, you will also have to include the latter. And this is
 * viral, as for every class you include, you must check that all classes that reference it are also
 * included.
 * </p>
 * 
 * <p>
 * It is also possible to add an extra URL to watch for changes using
 * <tt>ReloadingClassLoader.addLocation()</tt> . By default, all the URLs returned by the parent
 * class loader (ie all {@link URL} returned by {@link ClassLoader#getResources(String)} with empty
 * {@link String}) are registered.
 * </p>
 * <hr>
 * <p>
 * <b>Important. </b> It can be quite tricky to setup the reloading mechanism correctly. Here are
 * the general guidelines:
 * </p>
 * <ul>
 * <li>The order of include and exclude patterns is significant, the patterns are processed
 * sequentially in the order they are defined</li>
 * <li>Don't forget that inner classes are named after YourClass$1, so take that into account when
 * setting up the patterns, eg include <tt>YourClass*</tt>, not just <tt>YourClass</tt></li>
 * <li>To enable back-button support for the reloading mechanism, be sure to put
 * <tt>Objects.setObjectStreamFactory(new WicketObjectStreamFactory());</tt> in your application's
 * {@link WebApplication#init()} method. <b>Native JDK object serialization will break the reloading
 * mechanism when navigating in the browser's history.</b></li>
 * <li>It is advisable to <b>exclude</b> subclasses of {@link Session} from the the reloading
 * classloader, because this is the only object that remains across application restarts</li>
 * <li>Last but not least, make sure to clear your session cookie before reloading the application
 * home page (or any other bookmarkable page) to get rid of old pages stored in session</li>
 * </ul>
 * 
 * <p>
 * <b>Be sure to carefully read the following information if you also use Spring:</b>
 * </p>
 * 
 * <p>
 * When using org.apache.wicket.spring.SpringWebApplicationFactory, the application must be a bean
 * with "prototype" scope and the "applicationBean" init parameter must be explicitly set, otherwise
 * the reloading mechanism won't be able to recreate the application.
 * </p>
 * 
 * <p>
 * <b>WARNING. </b> Be careful that when using Spring or other component managers, you will get
 * <tt>ClassCastException</tt> if a given class is loaded two times, one time by the normal
 * classloader, and another time by the reloading classloader. You need to ensure that your Spring
 * beans are properly excluded from the reloading class loader and that only the Wicket components
 * are included. When getting a cryptic error with regard to class loading, class instantiation or
 * class comparison, first <b>disable the reloading class loader</b> to rule out the possibility of
 * a classloader conflict. Please keep in mind that two classes are equal if and only if they have
 * the same name <b>and are loaded in the same classloader</b>. Same goes for errors like
 * <tt>LinkageError: Class FooBar violates loader constraints</tt>, better be safe and disable the
 * reloading feature.
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
	@Override
	protected ClassLoader getClassLoader()
	{
		return reloadingClassLoader;
	}

	/**
	 * @see org.apache.wicket.protocol.http.WicketFilter#init(boolean, javax.servlet.FilterConfig)
	 */
	@Override
	public void init(final boolean isServlet, final FilterConfig filterConfig)
		throws ServletException
	{
		reloadingClassLoader.setListener(new IChangeListener()
		{
			@Override
			public void onChange()
			{
				destroy();

				// Remove the ModificationWatcher from the current reloading class loader
				reloadingClassLoader.destroy();

				/*
				 * Create a new classloader, as there is no way to clear a ClassLoader's cache. This
				 * supposes that we don't share objects across application instances, this is almost
				 * true, except for Wicket's Session object.
				 */
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

		super.init(isServlet, filterConfig);
	}
}

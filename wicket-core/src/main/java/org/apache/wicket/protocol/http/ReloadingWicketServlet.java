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


/**
 * Custom WicketServlet that reloads the web applications when classes are modified. In order to
 * reload your own classes, use include and exclude patterns using wildcards. And in web.xml, point
 * to the reloading wicket servlet instead of the original one.
 * 
 * <p>
 * <b>Example denoting the built-in patterns:</b>
 * 
 * <pre>
 * public class MyServlet extends ReloadingWicketServlet
 * {
 * 	static
 * 	{
 * 		ReloadingClassLoader.excludePattern(&quot;org.apache.wicket.*&quot;);
 * 		ReloadingClassLoader.includePattern(&quot;org.apache.wicket.examples.*&quot;);
 * 	}
 * }
 * </pre>
 * 
 * @see ReloadingWicketFilter for complete documentation, and for proper integration with Spring
 * 
 * @author <a href="mailto:jbq@apache.org">Jean-Baptiste Quenot</a>
 */
public class ReloadingWicketServlet extends WicketServlet
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected WicketFilter newWicketFilter()
	{
		return new ReloadingWicketFilter();
	}
}

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
 * A factory interface used by wicket filter to create application objects. Configure your
 * webapplication to use this factory in web.xml like:
 * 
 * <pre>
 * &lt;filter&gt;
 *   &lt;filter-name&gt;MyApplication&lt;/filter-name&gt;
 *   &lt;filter-class>org.apache.wicket.protocol.http.WicketFilter&lt;/filter-class&gt;
 *   &lt;init-param&gt;
 *     &lt;param-name&gt;applicationFactoryClassName&lt;/param-name&gt;
 *     &lt;param-value&gt;org.mycompany.MyWebApplicationFactory&lt;/param-value&gt;
 *   &lt;/init-param&gt;
 * &lt;/filter&gt;
 * </pre>
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public interface IWebApplicationFactory
{
	/**
	 * Create application object
	 * 
	 * @param filter
	 *            the wicket filter
	 * @return application object instance
	 */
	WebApplication createApplication(WicketFilter filter);

	/**
	 * Called when the filter instance that used this factory is destroyed
	 * 
	 * @param filter
	 *            the wicket filter
	 */
	public void destroy(WicketFilter filter);

}

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
package org.apache.wicket;

/**
 * client-Side redirect to wicket page.
 * <p/>
 * sends http status '302 Moved' with response header 'Location: {my-page-url}' 
 * to the client to enforce a client-side redirect. The client will request the 
 * new page with another HTTP GET request and the url in the address bar of 
 * the browser will be updated. 
 *
 * @author Peter Ertl
 */
public class RedirectException extends RestartResponseException
{
	private static final long serialVersionUID = 1L;

	/**
	 * redirect to page
	 *
	 * @param pageClass
	 *           page class
	 */
	public RedirectException(Class<? extends Page> pageClass)
	{
		super(pageClass);
		enforceRedirect();
	}

	/**
	 *  redirect to page
	 *
	 * @param pageClass
	 *           page class
	 * @param params
	 *           page parameters
	 */
	public RedirectException(Class<? extends Page> pageClass, PageParameters params)
	{
		super(pageClass, params);
		enforceRedirect();
	}

	private void enforceRedirect()
	{
		final RequestCycle cycle = RequestCycle.get();

		if (cycle == null)
		{
			throw new IllegalStateException("no current request cycle available");
		}

		cycle.setRedirect(true);
	}
}

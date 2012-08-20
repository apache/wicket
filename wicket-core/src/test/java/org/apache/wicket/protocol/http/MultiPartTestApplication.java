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

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.Page;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.util.tester.DummyHomePage;

/**
 * An extension of WebApplication just to make its #createWebRequest() public
 */
public class MultiPartTestApplication extends WebApplication
{
	/**
	 * Extend #createWebRequest() just to make it public
	 * @param servletRequest
	 *            the current HTTP Sservlet request
	 * @param filterPath
	 *            the filter mapping read from web.xml
	 * @return
	 */
	@Override
	public WebRequest createWebRequest(HttpServletRequest servletRequest, String filterPath)
	{
		return super.createWebRequest(servletRequest, filterPath);
	}

	@Override
	public Class<? extends Page> getHomePage()
	{
		return DummyHomePage.class;
	}
};
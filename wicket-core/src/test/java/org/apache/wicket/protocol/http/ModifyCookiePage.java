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

import javax.servlet.http.Cookie;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.http.WebResponse;

/**
 */
public class ModifyCookiePage extends WebPage
{
	private static final long serialVersionUID = 3884508803470168634L;

	/**	 */
	public static final String CREATE_COOKIE_ID = "createCookie";
	/**	 */
	public static final String COOKIE_NAME = "wicketTest";
	/**	 */
	public static final String COOKIE_VALUE = "1";

	/**
	 * Construct.
	 */
	public ModifyCookiePage()
	{
		add(new Link<Void>(CREATE_COOKIE_ID)
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick()
			{
				getWebResponse().addCookie(new Cookie(COOKIE_NAME, COOKIE_VALUE));
			}
		});
	}
}
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
package org.apache.wicket.util.tester;

import junit.framework.Assert;

import org.apache.wicket.util.cookies.CookieUtils;

/**
 * A test page for https://issues.apache.org/jira/browse/WICKET-4289
 */
public class CookiePage extends DummyHomePage
{
	private static final long serialVersionUID = 1L;

	private final String cookieName;
	private final String cookieValue;

	/**
	 * Constructor.
	 * 
	 * @param name
	 *            the cookie name
	 * @param value
	 *            the cookie value
	 */
	public CookiePage(String name, String value)
	{
		cookieName = name;
		cookieValue = value;

		doAssert();
	}

	private void doAssert()
	{
		CookieUtils utils = new CookieUtils();
		String v = utils.load(cookieName);
		Assert.assertEquals(cookieValue, v);
	}

	@Override
	protected void onConfigure()
	{
		super.onConfigure();

		doAssert();
	}


}

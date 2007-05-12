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
package org.apache.wicket.ajax.form;

import java.util.Locale;

import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.apache.wicket.Session;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.tester.WicketTester;
import org.apache.wicket.util.tester.WicketTester.DummyWebApplication;

/**
 * @author Janne Hietam&auml;ki (janne)
 */
public class OnChangeAjaxBehaviorTest extends WicketTestCase
{
	/**
	 * @throws Exception
	 */
	public void testRendering() throws Exception
	{
		WebApplication myApplication = new DummyWebApplication()
		{
			/**
			 * @see org.apache.wicket.protocol.http.WebApplication#newSession(org.apache.wicket.Request, org.apache.wicket.Response)
			 */
			public Session newSession(Request request, Response response)
			{
				Session session = super.newSession(request, response);
				session.setLocale(Locale.ENGLISH);
				return session;
			}
		};
		
		tester = new WicketTester(myApplication);
		
		executeTest(OnChangeAjaxBehaviorTestPage.class,
				"OnChangeAjaxBehaviorTestPage_expected.html");
	}

}

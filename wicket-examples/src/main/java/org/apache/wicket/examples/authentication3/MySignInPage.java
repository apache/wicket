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
package org.apache.wicket.examples.authentication3;

import org.apache.wicket.authroles.authentication.panel.SignInPanel;
import org.apache.wicket.examples.WicketExamplePage;


/**
 * Simple example of a sign in page.
 * 
 * @author Jonathan Locke
 */
public final class MySignInPage extends WicketExamplePage
{
	/**
	 * Constructor
	 */
	public MySignInPage()
	{
		// That is all you need to add a logon panel to your application with rememberMe
		// functionality based on Cookies. Meaning username and password are persisted in a Cookie.
		// Please see ISecuritySettings#getAuthenticationStrategy() for details.
		add(new SignInPanel("signInPanel"));
	}
}

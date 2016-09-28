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
package org.apache.wicket.examples.authentication2;

import org.apache.wicket.authroles.authentication.panel.SignInPanel;
import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.request.mapper.parameter.PageParameters;


/**
 * Simple example of a sign in page. It is based on auth-role's SignInPanel which already provides
 * all what is necessary.
 * 
 * @author Jonathan Locke
 */
public final class SignIn2 extends WicketExamplePage
{
	/**
	 * Constructor
	 * 
	 * @param parameters
	 *            The page parameters
	 */
	public SignIn2(final PageParameters parameters)
	{
		super(parameters);

		// Take our standard Logon Panel from the auth-role module and add it to the Page. That is
		// all what is necessary.
		add(new SignInPanel("signInPanel", false));
	}
}

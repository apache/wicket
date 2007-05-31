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
package org.apache.wicket.stateless.pages;

import org.apache.wicket.markup.html.WebPage;

/**
 * 
 * @author marrink
 */
public class LoginPage extends WebPage
{

	private static final long serialVersionUID = 1L;


	/**
	 * Constructor.
	 */
	public LoginPage()
	{
		setStatelessHint(true);
		String panelId = "signInPanel";
		newUserPasswordSignInPanel(panelId);
	}

	/**
	 * Creeert een sign in panel voor instellingen die hun authenticatie enkel baseren op
	 * username/wachtwoord.
	 * @param panelId
	 * @param info
	 */
	private void newUserPasswordSignInPanel(String panelId)
	{
		add(new UsernamePasswordSignInPanel(panelId));
	}
}

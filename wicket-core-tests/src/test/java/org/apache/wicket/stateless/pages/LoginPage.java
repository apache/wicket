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

	private boolean pageInitialized = false;
	private boolean panelInitialized = false;


	/**
	 * Constructor.
	 */
	public LoginPage()
	{
		setStatelessHint(true);
		String panelId = "signInPanel";
		newUserPasswordSignInPanel(panelId);
	}

	@Override
	protected void onInitialize()
	{
		super.onInitialize();
		pageInitialized = true;
	}

	/**
	 * @return pageInitialized
	 */
	public boolean isPageInitialized()
	{
		return pageInitialized;
	}

	/**
	 * @return panelInitialized
	 */
	public boolean isPanelInitialized()
	{
		return panelInitialized;
	}

	/**
	 * Creates a sign in panel on institutions that rely solely on their authentication username /
	 * password.
	 * 
	 * @param panelId
	 */
	private void newUserPasswordSignInPanel(String panelId)
	{
		add(new UsernamePasswordSignInPanel(panelId)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onInitialize()
			{
				super.onInitialize();
				panelInitialized = true;

			}
		});
	}
}

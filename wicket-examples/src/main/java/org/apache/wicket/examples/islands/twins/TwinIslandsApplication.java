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
package org.apache.wicket.examples.islands.twins;

import org.apache.wicket.Page;
import org.apache.wicket.csp.CSPDirective;
import org.apache.wicket.examples.WicketExampleApplication;
import org.apache.wicket.examples.islands.treasurehunt.TreasureHuntApplication;
import org.apache.wicket.islands.IslandSettings;

/**
 * The Twin Islands example: one poll, rendered side by side by a React and a Svelte component.
 * <p>
 * To work on the components with hot reload, start the Vite dev server in
 * <code>src/main/frontend</code> (<code>npm run dev</code>) and run the examples in development
 * mode with <code>-Dwicket.islands.devServer=http://localhost:5173</code>.
 */
public class TwinIslandsApplication extends WicketExampleApplication
{
	@Override
	public Class<? extends Page> getHomePage()
	{
		return TwinIslandsPage.class;
	}

	@Override
	protected void init()
	{
		super.init();

		// the policy Svelte creates for the templates of its compiled components
		getCspSettings().blocking().add(CSPDirective.TRUSTED_TYPES, "svelte-trusted-html");

		IslandSettings islands = IslandSettings.install(this);

		String devServer = System.getProperty(TreasureHuntApplication.DEV_SERVER_PROPERTY);
		if (devServer != null && usesDevelopmentConfig())
		{
			islands.setDevServerUrl(devServer);
			getCspSettings().blocking()
				.add(CSPDirective.CONNECT_SRC, devServer, devServer.replaceFirst("^http", "ws"));
		}
	}
}

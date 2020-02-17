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
package org.apache.wicket.examples;

import org.apache.wicket.csp.CSPDirective;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.cycle.IRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.resource.CssUrlReplacer;
import org.apache.wicket.settings.SecuritySettings;
import org.apache.wicket.util.crypt.ClassCryptFactory;
import org.apache.wicket.util.crypt.NoCrypt;


/**
 * Wicket Example Application class.
 * 
 * @author Jonathan Locke
 */
public abstract class WicketExampleApplication extends WebApplication
{
	/**
	 * prevent wicket from launching a java application window on the desktop <br/>
	 * once someone uses awt-specific classes java will automatically do so and allocate a window
	 * unless you tell java to run in 'headless-mode'
	 */
	static
	{
		System.setProperty("java.awt.headless", "true");
	}

	@Override
	protected void init()
	{
		super.init();
		
		// WARNING: DO NOT do this on a real world application unless
		// you really want your app's passwords all passed around and
		// stored in unencrypted browser cookies (BAD IDEA!)!!!

		// The NoCrypt class is being used here because not everyone
		// has the java security classes required by Crypt installed
		// and we want them to be able to run the examples out of the
		// box.
		getSecuritySettings().setCryptFactory(
			new ClassCryptFactory(NoCrypt.class, SecuritySettings.DEFAULT_ENCRYPTION_KEY));

		getDebugSettings().setDevelopmentUtilitiesEnabled(true);
		
		getResourceSettings().setCssCompressor(new CssUrlReplacer());
		getCsp().blocking().strict().reportBack()
				.add(CSPDirective.STYLE_SRC,
						"https://maxcdn.bootstrapcdn.com/font-awesome/4.3.0/css/font-awesome.min.css")
				.add(CSPDirective.FONT_SRC, "https://maxcdn.bootstrapcdn.com");
		
		getRequestCycleListeners().add(new IRequestCycleListener()
		{
			@Override
			public void onEndRequest(RequestCycle cycle)
			{
				((WebResponse) cycle.getResponse()).addHeader("Server-Timing",
					"server;desc=\"Wicket rendering time\";dur="
						+ (System.currentTimeMillis() - cycle.getStartTime()));
			}
		});
	}
}

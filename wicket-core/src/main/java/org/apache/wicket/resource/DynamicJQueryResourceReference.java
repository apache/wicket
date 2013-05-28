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
package org.apache.wicket.resource;

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.ClientProperties;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.settings.IJavaScriptLibrarySettings;


/**
 * A resource reference that calculates which version of jQuery JavaScript library to use depending
 * on the user agent. For IE 6/7/8 jQuery ver. 1.x will be used, for any other browser - ver. 2.x.
 *
 * To use this resource reference do:
 * <code>
 * app.getJavaScriptLibrarySettings().setJQueryReference(DynamicJQueryResourceReference.INSTANCE)
 * </code>
 *
 * @since 7.0.0
 */
public class DynamicJQueryResourceReference extends JavaScriptResourceReference
{
	private static final long serialVersionUID = 1L;

	/**
	 * The key for the metadata that is used as a cache to calculate the name
	 * only once
	 */
	private static final MetaDataKey<String> KEY = new MetaDataKey<String>()
	{
	};

	/**
	 * jQuery ver. 1.x - works on modern browsers and IE 6/7/8
	 */
	private static final String VERSION_1 = "jquery/jquery-1.10.0.js";

	/**
	 * jQuery ver. 2.x - works only on modern browsers
	 */
	private static final String VERSION_2 = "jquery/jquery-2.0.1.js";

	private static final DynamicJQueryResourceReference INSTANCE = new DynamicJQueryResourceReference();

	/**
	 * Normally you should not use this method, but use
	 * {@link IJavaScriptLibrarySettings#getJQueryReference()} to prevent version conflicts.
	 * 
	 * @return the single instance of the resource reference
	 */
	public static DynamicJQueryResourceReference get()
	{
		return INSTANCE;
	}

	private DynamicJQueryResourceReference()
	{
		super(DynamicJQueryResourceReference.class, VERSION_1);
	}

	@Override
	public String getName()
	{
		RequestCycle requestCycle = RequestCycle.get();
		String name = requestCycle.getMetaData(KEY);
		if (name == null)
		{
			WebClientInfo clientInfo;
			name = VERSION_2;
			if (Session.exists())
			{
				WebSession session = WebSession.get();
				clientInfo = session.getClientInfo();
			}
			else
			{
				clientInfo = new WebClientInfo(requestCycle);
			}
			ClientProperties clientProperties = clientInfo.getProperties();
			if (clientProperties.isBrowserInternetExplorer() && clientProperties.getBrowserVersionMajor() < 9)
			{
				name = VERSION_1;
			}

			requestCycle.setMetaData(KEY, name);
		}
		return name;
	}
}

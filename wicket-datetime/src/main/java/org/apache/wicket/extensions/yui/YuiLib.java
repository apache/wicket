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
package org.apache.wicket.extensions.yui;

import org.apache.wicket.Application;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

/**
 * Use the {@link #load(org.apache.wicket.markup.head.IHeaderResponse)} method to initialize the YUI library using the YUI loader.
 * It is OK to call this multiple times.
 * 
 * By default the resource stream gets gzipped. You may disable it via
 * Application.get().getResourceSettings().getDisableGZipCompression()
 * 
 * @author eelcohillenius
 */
public final class YuiLib
{
	private static ResourceReference YUILOADER;

	/**
	 * Load the YUI loader script. After that, you can declare YUI dependencies using
	 * YAHOO.util.YUILoader.
	 * 
	 * @param response
	 *            header response
	 */
	public static void load(IHeaderResponse response)
	{
		response.render(JavaScriptHeaderItem.forReference(getYuiLoader()));
	}

	private static ResourceReference getYuiLoader()
	{
		if (YUILOADER == null)
		{
			StringBuilder sb = new StringBuilder("yuiloader/yuiloader");
			if (Application.get().usesDeploymentConfig())
			{
				sb.append("-min");
			}
			sb.append(".js");
			YUILOADER = new PackageResourceReference(YuiLib.class, sb.toString());
		}
		return YUILOADER;
	}

	/**
	 * Prevent construction.
	 */
	private YuiLib()
	{
	}
}

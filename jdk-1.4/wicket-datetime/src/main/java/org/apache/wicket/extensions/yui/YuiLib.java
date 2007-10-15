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

import org.apache.wicket.IClusterable;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.resources.JavascriptResourceReference;

/**
 * Use the {@link #load(IHeaderResponse, boolean)} method to initialize the YUI library using the
 * YUI loader. It is OK to call this multiple times.
 * 
 * @author eelcohillenius
 */
public final class YuiLib implements IClusterable
{
	private static final long serialVersionUID = 1L;

	/**
	 * Load the YUI loader script. After that, you can declare YUI dependencies using
	 * YAHOO.util.YUILoader.
	 * 
	 * @param response
	 *            header response
	 */
	// TODO see http://tech.groups.yahoo.com/group/ydn-javascript/message/16209
	public static void load(IHeaderResponse response)
	{
		response.renderJavascriptReference(new JavascriptResourceReference(YuiLib.class,
				"yahoo/yahoo.js"));
		response.renderJavascriptReference(new JavascriptResourceReference(YuiLib.class,
				"yuiloader-beta.js"));
	}

	/**
	 * Prevent construction.
	 */
	private YuiLib()
	{
	}
}

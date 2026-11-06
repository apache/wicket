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
package org.apache.wicket.islands.modules;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.JavaScriptReferenceType;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.Strings;

/**
 * Loads island modules from a running Vite dev server, for hot module replacement during
 * development.
 * <p>
 * Renders Vite's client and, for each island type, the module the Wicket Islands Vite plugin
 * serves at <code>/@wicket-islands/&lt;type&gt;</code>. The style elements Vite injects take the
 * nonce of a Content Security Policy from the <code>csp-nonce</code> meta tag every island renders.
 * The dev server's origin must be allowed for connections, for Vite's hot module replacement.
 *
 * @since 12.0.0
 */
public class ViteDevServerModuleLoader implements IIslandModuleLoader
{
	private final String devServerUrl;

	/**
	 * @param devServerUrl
	 *            the dev server's URL, e.g. <code>http://localhost:5173</code>
	 */
	public ViteDevServerModuleLoader(String devServerUrl)
	{
		this.devServerUrl = Strings.stripEnding(Args.notEmpty(devServerUrl, "devServerUrl"), "/");
	}

	@Override
	public void renderHead(String islandType, IHeaderResponse response)
	{
		response.render(JavaScriptHeaderItem.forUrl(devServerUrl + "/@vite/client")
			.setType(JavaScriptReferenceType.MODULE));
		response.render(JavaScriptHeaderItem.forUrl(devServerUrl + "/@wicket-islands/" + islandType)
			.setType(JavaScriptReferenceType.MODULE));
	}
}

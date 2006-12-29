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
package wicket.extensions.explorerpngfix;

import wicket.RequestCycle;
import wicket.ResourceReference;
import wicket.behavior.AbstractBehavior;
import wicket.markup.html.IHeaderContributor;
import wicket.markup.html.IHeaderResponse;
import wicket.protocol.http.ClientProperties;
import wicket.protocol.http.WebRequestCycle;
import wicket.protocol.http.request.WebClientInfo;

/**
 * A behavior that adds the necessary javascript to the page to make ie < 7.0
 * properly work with png transparency.
 * 
 * @author ivaynberg
 * 
 */
public class ExplorerPngFix extends AbstractBehavior implements IHeaderContributor
{

	private static final long serialVersionUID = 1L;

	private static final ResourceReference ref = new ResourceReference(ExplorerPngFix.class,
			"explorerPngFix.js");		

	/**
	 * @see wicket.markup.html.IHeaderContributor#renderHead(wicket.markup.html.IHeaderResponse)
	 */
	public void renderHead(IHeaderResponse response)
	{		
		if (response.wasRendered(ref) == false)
		{
			WebClientInfo info = ((WebRequestCycle)RequestCycle.get()).getClientInfo();
			ClientProperties properties = info.getProperties();

			if (properties.isBrowserInternetExplorer() && properties.getBrowserVersionMajor() < 7)
			{
				response.getResponse().write("<!--[if lt IE 7.]> <script defer type=\"text/javascript\" src=\"");
				response.getResponse().write(RequestCycle.get().urlFor(ref));
				response.getResponse().write("\"></script> <![endif]-->");
				
				response.markRendered(ref);
			}
		}
	}
}

/*
 * $Id$
 * $Revision$
 * $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.spring.common.web;

import wicket.markup.html.link.Link;
import wicket.spring.direct.web.DirectPage;
import wicket.spring.proxy.web.ProxyPage;

/**
 * Home Page
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class HomePage extends BasePage {
	public HomePage() {
		add(new Link("direct-link") {

			public void onClick() {
				setResponsePage(new DirectPage());
			}

		});

		add(new Link("proxy-link") {

			public void onClick() {
				setResponsePage(new ProxyPage());
			}

		});

	}
}

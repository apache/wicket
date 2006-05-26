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

import wicket.markup.html.WebPage;
import wicket.markup.html.link.BookmarkablePageLink;

/**
 * Base page class. This is mainly here to provide some consistent look and
 * feel.
 * 
 * Notice that this page extends InjectableWebPage instead of WebPage so that
 * its subclasses can be injected automatically.
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class BasePage extends WebPage {
	public BasePage() {
		new BookmarkablePageLink(this,"home-link", HomePage.class);
	}
}

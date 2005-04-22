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
package wicket.markup.html.pages;

import wicket.AttributeModifier;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.WebPage;
import wicket.model.Model;

/**
 * Page that let the browser redirect. Use this if you want to direct the browser
 * to some external URL, like Google etc.
 *
 * @author Eelco Hillenius
 */
public class RedirectPage extends WebPage
{
	/**
	 * Constructor.
	 * @param url the url to redirect to
	 */
	public RedirectPage(String url)
	{
		this(url, 0);
	}

	/**
	 * Constructor.
	 * @param url the url to redirect to
	 * @param waitBeforeRedirect the number of seconds the browser should wait before redirecting
	 */
	public RedirectPage(String url, int waitBeforeRedirect)
	{
		WebMarkupContainer redirect = new WebMarkupContainer("redirect");
		String content = waitBeforeRedirect + "; " + url;
		redirect.add(new AttributeModifier("content", new Model(content)));
		add(redirect);
	}
}
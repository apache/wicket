/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.examples.cdapp;

import wicket.PageParameters;
import wicket.markup.html.link.PageLink;

/**
 * Home page for the cd app example. It displays what this example is all about.
 *
 * @author Eelco Hillenius
 */
public class Home extends CdAppBasePage
{
	/**
	 * Create the home page.
	 * @param parameters The parameters for the page (not used)
	 */
	public Home(final PageParameters parameters)
	{
		add(new PageLink("start", SearchPage.class));
	}
}
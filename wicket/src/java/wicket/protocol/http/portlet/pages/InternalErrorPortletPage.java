/*
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
package wicket.protocol.http.portlet.pages;

import wicket.markup.html.link.BookmarkablePageLink;
import wicket.protocol.http.portlet.PortletPage;

/**
 * Internal error portlet page
 * 
 * @author Janne Hietam&auml;ki
 */
public class InternalErrorPortletPage extends PortletPage
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 */
	public InternalErrorPortletPage()
	{
		new BookmarkablePageLink(this,"homePageLink",getApplication().getHomePage());
	}

	/**
	 * @see wicket.markup.html.WebPage#configureResponse()
	 */
	protected void configureResponse()
	{
		super.configureResponse();
	}
	/**
	 * @see wicket.Component#isVersioned()
	 */
	public boolean isVersioned()
	{
		return false;
	}

	/**
	 * @see wicket.Page#isErrorPage()
	 */
	public boolean isErrorPage()
	{
		return true;
	}
}

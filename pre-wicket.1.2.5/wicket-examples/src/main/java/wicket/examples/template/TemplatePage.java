/*
 * $Id$ $Revision$ $Date$
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
package wicket.examples.template;

import wicket.examples.WicketExamplePage;
import wicket.markup.html.basic.Label;
import wicket.markup.html.link.BookmarkablePageLink;
import wicket.markup.html.link.Link;
import wicket.model.PropertyModel;

/**
 * Our base page that serves as a template for pages that inherit from it.
 * Doesn't have to be abstract, but was made abstract here to stress the fact
 * that this page is not meant for direct use.
 * 
 * @author Eelco Hillenius
 */
public abstract class TemplatePage extends WicketExamplePage
{
	/** title of the current page. */
	private String pageTitle = "(no title)";

	/** the current banner. */
	private Banner currentBanner;

	/**
	 * Constructor
	 */
	public TemplatePage()
	{
		add(new Label("title", new PropertyModel(this, "pageTitle")));
		add(currentBanner = new Banner1("ad"));
		add(new Link("changeAdLink")
		{
			/**
			 * @see wicket.markup.html.link.Link#onClick()
			 */
			public void onClick()
			{
				if (currentBanner.getClass() == Banner1.class)
				{
					TemplatePage.this.replace(currentBanner = new Banner2("ad"));
				}
				else
				{
					TemplatePage.this.replace(currentBanner = new Banner1("ad"));
				}
			}
		});
		add(new BookmarkablePageLink("page1Link", Page1.class));
		add(new BookmarkablePageLink("page2Link", Page2.class));
	}

	/**
	 * Gets the title.
	 * 
	 * @return title
	 */
	public final String getPageTitle()
	{
		return pageTitle;
	}

	/**
	 * Sets the title.
	 * 
	 * @param title
	 *            title
	 */
	public final void setPageTitle(String title)
	{
		this.pageTitle = title;
	}

	/**
	 * @see wicket.Component#isVersioned()
	 */
	public boolean isVersioned()
	{
		// TODO Bug: Versioning gives problems... probably has to do with markup
		// inheritance
		return false;
	}
}
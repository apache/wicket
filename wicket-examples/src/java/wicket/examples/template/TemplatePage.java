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
 * <p>
 * Alternatively, instead of creating new instances of components whenever we
 * want to replace others (Banner1/ Banner2), we can re-use components. This
 * class could be re-written like this:
 * 
 * <pre>
 *       public abstract class TemplatePage extends WicketExamplePage
 *       {
 *              private String pageTitle = &quot;(no title)&quot;;
 *       
 *              private Banner currentBanner;
 *       
 *              private Banner banner1;
 *       
 *              private Banner banner2;
 *       
 *              public TemplatePage()
 *              {
 *                      new Label(this, &quot;title&quot;, new PropertyModel(this, &quot;pageTitle&quot;));
 *                      banner2 = new Banner2(this, &quot;ad&quot;);
 *                      currentBanner = banner1 = new Banner1(this, &quot;ad&quot;);
 *       
 *                     new Link(this, &quot;changeAdLink&quot;)
 *                     {
 *                              public void onClick()
 *                              {
 *                                      if (currentBanner == banner1)
 *                                      {
 *                                              currentBanner = banner2;
 *                                              banner2.reAttach();
 *                                      }
 *                                      else
 *                                      {
 *                                              currentBanner = banner1;
 *                                              banner1.reAttach();
 *                                      }
 *                              }
 *                      };
 *       ...
 * </pre>
 * 
 * </p>
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
		new Label(this, "title", new PropertyModel(this, "pageTitle"));
		currentBanner = new Banner1(this, "ad");
		new Link(this, "changeAdLink")
		{
			/**
			 * @see wicket.markup.html.link.Link#onClick()
			 */
			@Override
			public void onClick()
			{
				if (currentBanner.getClass() == Banner1.class)
				{
					// we already had a component directly under the page with
					// id 'ad'. Creating a new one like this (same id, same
					// hierarchy position) will have the effect that this new
					// component will be set as the current one, and thus will
					// be rendered instead of the previous child. In Wicket
					// pre 2.0 (before you had to pass in the parent in the
					// constructor to create the hierarchy, and had to use
					// Component#add instead) you achieved the same by calling
					// Component#replace. Now you either construct a new
					// component with the same parent and same id, or - if
					// you have a reference to a component that was previously
					// created with that parent - you call Component#reAttach
					// to set that component as the current one.
					new Banner2(TemplatePage.this, "ad");
				}
				else
				{
					new Banner1(TemplatePage.this, "ad");
				}
			}
		};
		new BookmarkablePageLink(this, "page1Link", Page1.class);
		new BookmarkablePageLink(this, "page2Link", Page2.class);
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
	@Override
	public boolean isVersioned()
	{
		// TODO Bug: Versioning gives problems... probably has to do with markup
		// inheritance
		return false;
	}
}
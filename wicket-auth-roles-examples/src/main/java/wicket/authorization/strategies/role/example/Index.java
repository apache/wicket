/*
 * $Id$ $Revision$
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
package wicket.authorization.strategies.role.example;

import wicket.Session;
import wicket.authorization.strategies.role.example.pages.AdminAnnotationsBookmarkablePage;
import wicket.authorization.strategies.role.example.pages.AdminAnnotationsInternalPage;
import wicket.authorization.strategies.role.example.pages.AdminBookmarkablePage;
import wicket.authorization.strategies.role.example.pages.AdminInternalPage;
import wicket.authorization.strategies.role.example.pages.AnnotationsPanelsPage;
import wicket.authorization.strategies.role.example.pages.PanelsPage;
import wicket.markup.html.WebPage;
import wicket.markup.html.basic.Label;
import wicket.markup.html.link.BookmarkablePageLink;
import wicket.markup.html.link.Link;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;
import wicket.model.Model;
import wicket.model.PropertyModel;

/**
 * Home page for the roles example.
 * 
 * @author Eelco Hillenius
 */
public class Index extends WebPage
{
	/**
	 * Construct.
	 */
	public Index()
	{
		new Label(this,"currentUser", new PropertyModel(this, "session.user"));
		new ListView<User>(this,"users", RolesApplication.USERS)
		{
			@Override
			protected void populateItem(ListItem<User> item)
			{
				final User user = item.getModelObject();
				Link link = new Link(item,"selectUserLink")
				{
					@Override
					public void onClick()
					{
						RolesSession session = (RolesSession)Session.get();
						session.setUser(user);
					}
				};
				new Label(link,"userId", new Model<User>(user));
			}
		};

		// pages that are proteced using wicket meta data
		new BookmarkablePageLink(this,"adminBookmarkableLink", AdminBookmarkablePage.class);
		new Link(this,"adminInternalLink")
		{
			@Override
			public void onClick()
			{
				setResponsePage(new AdminInternalPage("foo"));
			}
		};
		new BookmarkablePageLink(this,"panelsPageLink", PanelsPage.class);

		// pages that are protected using annotations
		new BookmarkablePageLink(this,"adminAnnotBookmarkableLink", AdminAnnotationsBookmarkablePage.class);
		new Link(this,"adminAnnotInternalLink")
		{
			@Override
			public void onClick()
			{
				setResponsePage(new AdminAnnotationsInternalPage("bar"));
			}
		};
		new BookmarkablePageLink(this,"panelsAnnotPageLink", AnnotationsPanelsPage.class);
	}
}

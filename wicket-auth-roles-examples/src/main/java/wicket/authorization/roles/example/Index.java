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
package wicket.authorization.roles.example;

import wicket.Session;
import wicket.authorization.roles.example.pages.AdminBookmarkable;
import wicket.authorization.roles.example.pages.AdminInternal;
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
		add(new Label("currentUser", new PropertyModel(this, "session.user")));
		add(new ListView("users", RolesAuthApplication.USERS)
		{
			@Override
			protected void populateItem(ListItem item)
			{
				final User user = (User)item.getModelObject();
				item.add(new Link("selectUserLink")
				{
					@Override
					public void onClick()
					{
						RolesAuthSession session = (RolesAuthSession)Session.get();
						session.setUser(user);
					}
				}.add(new Label("userId", new Model(user))));
			}
		});

		add(new BookmarkablePageLink("adminBookmarkableLink", AdminBookmarkable.class));
		add(new Link("adminInternalLink")
		{
			@Override
			public void onClick()
			{
				setResponsePage(new AdminInternal("foo"));
			}
		});
	}
}

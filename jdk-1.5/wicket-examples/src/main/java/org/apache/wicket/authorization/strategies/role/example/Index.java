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
package org.apache.wicket.authorization.strategies.role.example;

import org.apache.wicket.Session;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import org.apache.wicket.authorization.strategies.role.example.pages.AdminAnnotationsBookmarkablePage;
import org.apache.wicket.authorization.strategies.role.example.pages.AdminAnnotationsInternalPage;
import org.apache.wicket.authorization.strategies.role.example.pages.AdminBookmarkablePage;
import org.apache.wicket.authorization.strategies.role.example.pages.AdminInternalPage;
import org.apache.wicket.authorization.strategies.role.example.pages.AnnotationsPanelsPage;
import org.apache.wicket.authorization.strategies.role.example.pages.PanelsPage;

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
		add(new ListView("users", RolesApplication.USERS)
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
						RolesSession session = (RolesSession)Session.get();
						session.setUser(user);
					}
				}.add(new Label("userId", new Model(user))));
			}
		});

		// pages that are protected using wicket meta data
		add(new BookmarkablePageLink("adminBookmarkableLink", AdminBookmarkablePage.class));
		add(new Link("adminInternalLink")
		{
			@Override
			public void onClick()
			{
				setResponsePage(new AdminInternalPage("foo"));
			}
		});
		add(new BookmarkablePageLink("panelsPageLink", PanelsPage.class));

		// pages that are protected using annotations
		add(new BookmarkablePageLink("adminAnnotBookmarkableLink", AdminAnnotationsBookmarkablePage.class));
		add(new Link("adminAnnotInternalLink")
		{
			@Override
			public void onClick()
			{
				setResponsePage(new AdminAnnotationsInternalPage("bar"));
			}
		});
		add(new BookmarkablePageLink("panelsAnnotPageLink", AnnotationsPanelsPage.class));
	}
}

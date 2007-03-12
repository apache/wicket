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
package wicket.authorization.strategies.role.example.pages;

import java.util.Arrays;
import java.util.List;

import wicket.ajax.AjaxRequestTarget;
import wicket.ajax.markup.html.AjaxLink;
import wicket.authorization.Action;
import wicket.authorization.strategies.role.Roles;
import wicket.authorization.strategies.role.annotations.AuthorizeAction;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.WebPage;
import wicket.markup.html.basic.Label;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;
import wicket.markup.html.panel.Panel;

/**
 * Bookmarkable page that may only be accessed by users that have role ADMIN.
 * 
 * @author Eelco Hillenius
 */
public class AnnotationsPanelsPage extends WebPage
{
	@AuthorizeAction(action = Action.RENDER, roles = Roles.ADMIN)
	private static class AdminLabel extends Label
	{
		/**
		 * @param id
		 * @param nbr
		 */
		public AdminLabel(String id, String nbr)
		{
			super(id, "label for admins " + nbr);
		}
	}

	/**
	 * A panel that is only visible for users with role ADMIN.
	 */
	@AuthorizeAction(action = Action.RENDER, roles = Roles.ADMIN)
	private static final class ForAdmins extends Panel
	{
		/**
		 * Construct.
		 * 
		 * @param id
		 */
		public ForAdmins(String id)
		{
			super(id);
		}
	}

	/**
	 * A panel that is only visible for users with role ADMIN or USER.
	 */
	@AuthorizeAction(action = Action.RENDER, roles = { Roles.ADMIN, Roles.USER })
	private static final class ForAdminsAndUsers extends Panel
	{
		/**
		 * Construct.
		 * 
		 * @param id
		 */
		public ForAdminsAndUsers(String id)
		{
			super(id);
		}
	}

	/**
	 * A panel that is visible for all users.
	 */
	private static final class ForAllUsers extends Panel
	{
		/**
		 * Construct.
		 * 
		 * @param id
		 */
		public ForAllUsers(String id)
		{
			super(id);
		}
	}

	/**
	 * A panel that is only visible for users with role ADMIN or USER.
	 */
	@AuthorizeAction(action = Action.RENDER, roles = { Roles.ADMIN, Roles.USER })
	private static final class Test extends Panel
	{
		/**
		 * Construct.
		 * 
		 * @param id
		 */
		public Test(String id)
		{
			super(id);
			List l = Arrays.asList("1", "2", "3", "4", "5");
			ListView listView = new ListView("list", l)
			{
				@Override
				protected void populateItem(ListItem item)
				{
					String i = item.getModelObjectAsString();
					item.add(new UserLabel("userLabel", i));
					item.add(new AdminLabel("adminLabel", i));
				}
			};
			add(listView);
			listView.setReuseItems(true);
		}
	}

	@AuthorizeAction(action = Action.RENDER, roles = Roles.USER)
	private static class UserLabel extends Label
	{
		/**
		 * @param id
		 * @param nbr
		 */
		public UserLabel(String id, String nbr)
		{
			super(id, "label for users " + nbr);
		}
	}

	private WebMarkupContainer outer;

	private boolean showDummy = true;

	/**
	 * Construct.
	 */
	public AnnotationsPanelsPage()
	{
		add(new ForAllUsers("forAllUsersPanel"));
		add(new ForAdminsAndUsers("forAdminsAndUsersPanel"));
		add(new ForAdmins("forAdminsPanel"));
		add(outer = new WebMarkupContainer("outer"));
		outer.setOutputMarkupId(true);

		outer.add(new WebMarkupContainer("test").setOutputMarkupId(true));
		add(new AjaxLink("link")
		{
			@Override
			public void onClick(AjaxRequestTarget target)
			{
				showDummy = !showDummy;
				if (showDummy)
				{
					outer.replace(new WebMarkupContainer("test"));
				}
				else
				{
					outer.replace(new Test("test"));
				}
				target.addComponent(outer);
			}
		});
	}

}

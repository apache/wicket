/*
 * $Id: AnnotationsPanelsPage.java 5904 2006-05-26 23:24:34 +0000 (Fri, 26 May
 * 2006) joco01 $ $Revision$ $Date: 2006-05-26 23:24:34 +0000 (Fri, 26
 * May 2006) $
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
package wicket.authorization.strategies.role.example.pages;

import wicket.MarkupContainer;
import wicket.authorization.Action;
import wicket.authorization.strategies.role.Roles;
import wicket.authorization.strategies.role.annotations.AuthorizeAction;
import wicket.markup.html.WebPage;
import wicket.markup.html.panel.Panel;

/**
 * Bookmarkable page that may only be accessed by users that have role ADMIN.
 * 
 * @author Eelco Hillenius
 */
public class AnnotationsPanelsPage extends WebPage
{
	/**
	 * Construct.
	 */
	public AnnotationsPanelsPage()
	{
		new ForAllUsers(this, "forAllUsersPanel");
		new ForAdminsAndUsers(this, "forAdminsAndUsersPanel");
		new ForAdmins(this, "forAdminsPanel");
	}

	/**
	 * A panel that is visible for all users.
	 */
	private static final class ForAllUsers extends Panel
	{
		/**
		 * Construct.
		 * 
		 * @param parent
		 *            The parent component
		 * @param id
		 */
		public ForAllUsers(MarkupContainer parent, String id)
		{
			super(parent, id);
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
		 * @param parent
		 *            The parent component
		 * @param id
		 */
		public ForAdminsAndUsers(MarkupContainer parent, String id)
		{
			super(parent, id);
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
		 * @param parent
		 *            The parent component
		 * @param id
		 */
		public ForAdmins(MarkupContainer parent, String id)
		{
			super(parent, id);
		}
	}
}

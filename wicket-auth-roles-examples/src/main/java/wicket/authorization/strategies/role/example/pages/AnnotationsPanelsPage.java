/*
 * $Id$
 * $Revision$ $Date$
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
		add(new ForAllUsers("forAllUsersPanel"));
		add(new ForAdminsAndUsers("forAdminsAndUsersPanel"));
		add(new ForAdmins("forAdminsPanel"));
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
	@AuthorizeAction(action = "RENDER", roles = {"ADMIN", "USER"})
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
	 * A panel that is only visible for users with role ADMIN.
	 */
	@AuthorizeAction(action = "RENDER", roles = "ADMIN")
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
}

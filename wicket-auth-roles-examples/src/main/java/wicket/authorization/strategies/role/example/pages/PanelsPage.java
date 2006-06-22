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

import wicket.MarkupContainer;
import wicket.authorization.strategies.role.metadata.MetaDataRoleAuthorizationStrategy;
import wicket.markup.html.WebPage;
import wicket.markup.html.panel.Panel;

/**
 * Bookmarkable page that may only be accessed by users that have role ADMIN.
 * 
 * @author Eelco Hillenius
 */
public class PanelsPage extends WebPage
{
	/**
	 * Construct.
	 */
	public PanelsPage()
	{
		ForAllUsers forAllUsers = new ForAllUsers(this, "forAllUsersPanel");
		// don't have to do anything here; component is by default not protected

		ForAdminsAndUsers forAdminsAndUsers = new ForAdminsAndUsers(this, "forAdminsAndUsersPanel");
		// authorise roles admin and user (and thus deny everyone else) for the
		// Component.RENDER action
		MetaDataRoleAuthorizationStrategy.authorize(forAdminsAndUsers, RENDER, "ADMIN");
		MetaDataRoleAuthorizationStrategy.authorize(forAdminsAndUsers, RENDER, "USER");

		ForAdmins forAdmins = new ForAdmins(this, "forAdminsPanel");
		// authorise role admin (and thus deny everyone else) for the
		// Component.RENDER action
		MetaDataRoleAuthorizationStrategy.authorize(forAdmins, RENDER, "ADMIN");
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
	 * A panel that is only visible for users with role ADMIN.
	 */
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

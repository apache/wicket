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
package org.apache.wicket.examples.authorization.pages;

import org.apache.wicket.authroles.authorization.strategies.role.metadata.MetaDataRoleAuthorizationStrategy;
import org.apache.wicket.examples.authorization.BasePage;
import org.apache.wicket.markup.html.panel.Panel;


/**
 * Bookmarkable page that may only be accessed by users that have role ADMIN.
 * 
 * @author Eelco Hillenius
 */
public class PanelsPage extends BasePage
{
	/**
	 * Construct.
	 */
	public PanelsPage()
	{
		ForAllUsers forAllUsers = new ForAllUsers("forAllUsersPanel");
		// don't have to do anything here; component is by default not protected
		add(forAllUsers);

		ForAdminsAndUsers forAdminsAndUsers = new ForAdminsAndUsers("forAdminsAndUsersPanel");
		add(forAdminsAndUsers);
		// authorise roles admin and user (and thus deny everyone else) for the
		// Component.RENDER action
		MetaDataRoleAuthorizationStrategy.authorize(forAdminsAndUsers, RENDER, "ADMIN");
		MetaDataRoleAuthorizationStrategy.authorize(forAdminsAndUsers, RENDER, "USER");

		ForAdmins forAdmins = new ForAdmins("forAdminsPanel");
		// authorise role admin (and thus deny everyone else) for the
		// Component.RENDER action
		MetaDataRoleAuthorizationStrategy.authorize(forAdmins, RENDER, "ADMIN");
		add(forAdmins);
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
	 * A panel that is only visible for users with role ADMIN.
	 */
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

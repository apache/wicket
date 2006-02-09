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
package wicket.authorization.strategies.role.metadata;

import junit.framework.TestCase;
import wicket.authorization.Action;

/**
 * Test case for {@link wicket.authorization.strategies.role.metadata.ActionPermissions}.
 * 
 * @author Eelco Hillenius
 */
public class ActionPermissionsTest extends TestCase
{
	/**
	 * Construct.
	 */
	public ActionPermissionsTest()
	{
		super();
	}

	/**
	 * Construct.
	 * @param arg0
	 */
	public ActionPermissionsTest(String arg0)
	{
		super(arg0);
	}

	/**
	 * Test adding roles.
	 * 
	 * @throws Exception
	 */
	public void testAdd1() throws Exception
	{
		ActionPermissions permissions = new ActionPermissions();
		Action mambo = new Action("mambo");
		permissions.authorize(mambo, "jonathan");
		permissions.authorize(mambo, "johan");
		permissions.authorize(mambo, "maurice");
		permissions.authorize(mambo, "eelco");
		assertEquals(4, permissions.rolesFor(mambo).size());
		permissions.unauthorize(mambo, "maurice");
		assertEquals(3, permissions.rolesFor(mambo).size());
		permissions.authorizeAll(mambo);
		assertEquals(0, permissions.rolesFor(mambo));
	}
}

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
package wicket.authorization.strategies.role;

import wicket.authorization.strategies.role.metadata.AuthorizedAction;
import wicket.authorization.strategies.role.metadata.AuthorizedActions;
import junit.framework.TestCase;

/**
 * Test case for {@link wicket.authorization.strategies.role.metadata.AuthorizedAction}.
 * 
 * @author Eelco Hillenius
 */
public class AuthorizedActionTest extends TestCase
{
	/**
	 * Construct.
	 */
	public AuthorizedActionTest()
	{
		super();
	}

	/**
	 * Construct.
	 * @param arg0
	 */
	public AuthorizedActionTest(String arg0)
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
		AuthorizedActions actions = new AuthorizedActions();
		actions.add(new AuthorizedAction("TEST", new String[] { "FOO", "BAR" }));
		actions.add(new AuthorizedAction("TEST", new String[] { "ADMIN", "USER" }));
		String[] rolesToTest = actions.roles("TEST");
		assertNotNull(rolesToTest);
		assertEquals(4, rolesToTest.length);
	}

	/**
	 * Test adding roles.
	 * 
	 * @throws Exception
	 */
	public void testAdd2() throws Exception
	{
		AuthorizedActions actions = new AuthorizedActions();
		actions.add(new AuthorizedAction("TEST", new String[] { "FOO", "BAR" }));
		actions.add(new AuthorizedAction("TEST", new String[] { "FOO", "USER" }));
		String[] rolesToTest = actions.roles("TEST");
		assertNotNull(rolesToTest);
		assertEquals(3, rolesToTest.length);
	}

	/**
	 * Test adding roles.
	 * 
	 * @throws Exception
	 */
	public void testAdd3() throws Exception
	{
		AuthorizedActions actions = new AuthorizedActions();
		actions.add(new AuthorizedAction("TEST1", new String[] { "FOO", "BAR" }));
		actions.add(new AuthorizedAction("TEST2", new String[] { "FOO", "USER", "GEE" }));
		String[] rolesToTest1 = actions.roles("TEST1");
		assertNotNull(rolesToTest1);
		assertEquals(2, rolesToTest1.length);
		String[] rolesToTest2 = actions.roles("TEST2");
		assertNotNull(rolesToTest2);
		assertEquals(3, rolesToTest2.length);
	}
}

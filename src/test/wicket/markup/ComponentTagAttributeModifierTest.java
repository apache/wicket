/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.markup;

import java.io.Serializable;

import junit.framework.Assert;
import junit.framework.TestCase;
import wicket.AttributeModifier;
import wicket.model.Model;

/**
 * Test case for the component tag attribute modifer test.
 * 
 * @author Chris Turner
 */
public class ComponentTagAttributeModifierTest extends TestCase
{

	/**
	 * Create a test case instance.
	 * 
	 * @param name
	 *            The test name
	 */
	public ComponentTagAttributeModifierTest(final String name)
	{
		super(name);
	}

	/**
	 * 
	 */
	public void testSerializable()
	{
		AttributeModifier modifier = new AttributeModifier("test", new Model("model"));
		Assert.assertTrue(modifier instanceof Serializable);
	}

	/**
	 * 
	 */
	public void testBadConstructor()
	{
		try
		{
			new AttributeModifier(null, new Model("model"));
			Assert.fail("IllegalArgumentException should be thrown on null attribute name");
		}
		catch (IllegalArgumentException e)
		{
			// Expected result
		}

		try
		{
			new AttributeModifier("test", null);
		}
		catch (IllegalArgumentException e)
		{
			Assert.fail("IllegalArgumentException should not be thrown on null replace model");
		}
	}
}

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
package wicket.util.lang;

import java.io.Serializable;

import wicket.WicketTestCase;
import wicket.markup.html.form.TextField;
import wicket.model.Model;
import wicket.model.PropertyModel;

/**
 * Tests the Objects class.
 * 
 * @author Martijn Dashorst
 */
public class ObjectsTest extends WicketTestCase
{

	/**
	 * Construct.
	 * 
	 * @param name
	 */
	public ObjectsTest(String name)
	{
		super(name);
	}

	/**
	 * Test method for 'wicket.util.lang.Objects.equal(Object, Object)'
	 */
	public void testEqual()
	{
		Object object = new Object();
		assertTrue(Objects.equal(object, object));

		assertFalse(Objects.equal(null, object));
		assertFalse(Objects.equal(object, null));
		assertTrue(Objects.equal(null, null));

		assertFalse(Objects.equal(new Object(), new Object()));
		assertTrue(Objects.equal(new Integer(1), new Integer(1)));
		assertFalse(Objects.equal("1", new Integer(1)));
		assertFalse(Objects.equal(new Integer(1), "1"));
		assertTrue(Objects.equal("1", new Integer(1).toString()));
		assertTrue(Objects.equal(new Integer(1).toString(), "1"));
	}

	/**
	 * Test method for 'wicket.util.lang.Objects.clone(Object)'
	 */
	public void testCloneNull()
	{
		Object clone = Objects.cloneModel(null);
		assertEquals(null, clone);
	}

	/**
	 * Test method for 'wicket.util.lang.Objects.clone(Object)'
	 */
	public void testCloneString()
	{
		String cloneMe = "Mini-me";

		Object clone = Objects.cloneModel(cloneMe);
		assertEquals(cloneMe, clone);
		assertNotSame(cloneMe, clone);
	}

	/**
	 * Test method for 'wicket.util.lang.Objects.clone(Object)'
	 */
	public void testCloneObject()
	{
		Object cloneMe = new Object();

		try
		{
			Objects.cloneModel(cloneMe);
			fail("Exception expected");
		}
		catch (RuntimeException e)
		{
			assertTrue(true);
		}
	}

	/**
	 * Test method for component cloning
	 */
	public void testComponentClone()
	{
		PropertyModel pm = new PropertyModel(new TextField("test", new Model("test")),
				"modelObject");
		PropertyModel pm2 = (PropertyModel)Objects.cloneModel(pm);
		assertTrue(pm.getObject(null) == pm2.getObject(null));
	}

	/**
	 * Test method for 'wicket.util.lang.Objects.clone(Object)'
	 */
	public void testCloneCloneObject()
	{
		CloneObject cloneMe = new CloneObject();
		cloneMe.nr = 1;

		Object clone = Objects.cloneModel(cloneMe);
		assertEquals(cloneMe, clone);
		assertNotSame(cloneMe, clone);
	}

	/**
	 * Used for testing the clone function.
	 */
	private static final class CloneObject implements Serializable
	{
		private static final long serialVersionUID = 1L;

		/**
		 * int for testing equality.
		 */
		private int nr;

		/**
		 * @see Object#equals(java.lang.Object)
		 */
		public boolean equals(Object o)
		{
			CloneObject other = (CloneObject)o;
			return other.nr == nr;
		}
	}
}

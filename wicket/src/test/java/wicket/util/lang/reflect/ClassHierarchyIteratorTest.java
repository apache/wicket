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
package wicket.util.lang.reflect;

import java.util.Iterator;

import junit.framework.TestCase;

/**
 * Tests for {@link ClassHierarchyIteratorTest}
 * 
 * @author ivaynberg
 */
public class ClassHierarchyIteratorTest extends TestCase
{
	private static class A
	{

	}

	private static class B extends A
	{

	}

	/**
	 * Tests subclass to superclass ordered iterator
	 */
	public void testSubToSuper()
	{
		Iterator<Class> it = new ClassHieararchyIterator(B.class, ClassOrder.SUB_TO_SUPER);
		assertTrue(it.hasNext());
		assertEquals(it.next(), B.class);
		assertTrue(it.hasNext());
		assertEquals(it.next(), A.class);
		assertTrue(it.hasNext());
		assertEquals(it.next(), Object.class);
		assertFalse(it.hasNext());
	}

	/**
	 * Tests superclass to subclass ordered iterator
	 */
	public void testSuperToSub()
	{
		Iterator<Class> it = new ClassHieararchyIterator(B.class, ClassOrder.SUPER_TO_SUB);
		assertTrue(it.hasNext());
		assertEquals(it.next(), Object.class);
		assertTrue(it.hasNext());
		assertEquals(it.next(), A.class);
		assertTrue(it.hasNext());
		assertEquals(it.next(), B.class);
		assertFalse(it.hasNext());
	}

}

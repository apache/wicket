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
package org.apache.wicket.util.lang;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the <code>Primitives</code> class. The code for testing the hashcode is taken from the
 * junit-addons framework (http://junit-addons.sourceforge.net). It didn't seem worth it to include
 * the whole framework.
 * 
 * @author Martijn Dashorst
 * @author <a href="mailto:pholser@yahoo.com">Paul Holser</a>
 */
public class PrimitivesTest
{
	/**
	 * Test stub for testing the hashcode function.
	 */
	private class HashCodeObject
	{
		int value;

		/**
		 * Sets the value.
		 * 
		 * @param value
		 *            the value to use
		 */
		public HashCodeObject(int value)
		{
			this.value = value;
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode()
		{
			return Primitives.hashCode(value);
		}
	}

	private HashCodeObject eq1;
	private HashCodeObject eq2;
	private HashCodeObject eq3;
	private HashCodeObject neq;

	/**
	 * Creates the objects for the tests.
	 */
	@BeforeEach
	public void before()
	{
		eq1 = new HashCodeObject(26);
		eq2 = new HashCodeObject(26);
		eq3 = new HashCodeObject(26);
		neq = new HashCodeObject(27);
	}

	/**
	 * Tests the <code>hashCode</code> contract.
	 * 
	 * @author <a href="mailto:pholser@yahoo.com">Paul Holser</a>
	 */
	@Test
	public final void hashCodeContract()
	{
		assertEquals(eq1.hashCode(), eq2.hashCode(), "1st vs. 2nd");
		assertEquals(eq1.hashCode(), eq3.hashCode(), "1st vs. 3rd");
		assertEquals(eq2.hashCode(), eq3.hashCode(), "2nd vs. 3rd");
		assertTrue(eq1.hashCode() != neq.hashCode(), "1st vs. neq");
	}

	/**
	 * Tests the consistency of <code>hashCode</code>.
	 * 
	 * @author <a href="mailto:pholser@yahoo.com">Paul Holser</a>
	 */
	@Test
	public final void hashCodeIsConsistentAcrossInvocations()
	{
		int eq1Hash = eq1.hashCode();
		int eq2Hash = eq2.hashCode();
		int eq3Hash = eq3.hashCode();
		int neqHash = neq.hashCode();

		for (int i = 0; i < 2; ++i)
		{
			assertEquals(eq1Hash, eq1.hashCode(), "1st equal instance");
			assertEquals(eq2Hash, eq2.hashCode(), "2nd equal instance");
			assertEquals(eq3Hash, eq3.hashCode(), "3rd equal instance");
			assertEquals(neqHash, neq.hashCode(), "not-equal instance");
		}
	}
}

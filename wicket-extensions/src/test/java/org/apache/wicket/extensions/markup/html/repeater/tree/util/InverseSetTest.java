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
package org.apache.wicket.extensions.markup.html.repeater.tree.util;

import java.util.HashSet;

import junit.framework.Assert;

import org.apache.wicket.model.IDetachable;
import org.junit.Test;

/**
 * Test for {@link InverseSet}.
 * 
 * @author svenmeier
 */
public class InverseSetTest extends Assert
{
	private TestSet set;

	/**
	 * Construct.
	 */
	public InverseSetTest()
	{
		set = new TestSet();
		set.add("A");
	}

	/**
	 * Test contains.
	 */
	@Test
	public void contains()
	{
		InverseSet<String> inverse = new InverseSet<String>(set);
		assertFalse(inverse.contains("A"));
		assertTrue(inverse.contains("B"));

		inverse.remove("B");
		assertFalse(inverse.contains("A"));
		assertFalse(inverse.contains("B"));

		inverse.add("A");
		assertTrue(inverse.contains("A"));
		assertFalse(inverse.contains("B"));

		inverse.detach();
		assertTrue(set.detached);
	}

	private class TestSet extends HashSet<String> implements IDetachable
	{
		private static final long serialVersionUID = 1L;

		boolean detached = false;

		public void detach()
		{
			detached = true;
		}
	}
}
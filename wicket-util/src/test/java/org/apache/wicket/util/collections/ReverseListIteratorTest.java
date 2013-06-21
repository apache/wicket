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
package org.apache.wicket.util.collections;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link ReverseListIterator}
 */
public class ReverseListIteratorTest extends Assert
{

	/**
	 * Test that it reverses the items of a list while iterating on them
	 */
	@Test
	public void reverse()
	{
		List<Integer> list = new ArrayList<>();
		for (int i = 0; i < 10; i++)
		{
			list.add(i);
		}

		Integer expected = 9;
		for (Integer actual : new ReverseListIterator<Integer>(list))
		{
			assertEquals(expected, actual);
			expected--;
		}
	}

}

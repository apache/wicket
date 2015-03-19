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
package org.apache.wicket.util.value;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * {@link LongValue} tests
 * 
 * @author igor
 */
public class LongValueTest
{
	/** Test {@link LongValue#max(LongValue, LongValue)} */
	@Test
	public void max()
	{
		LongValue v1 = new LongValue(1);
		LongValue v2 = new LongValue(2);

		assertEquals(v2, LongValue.max(v1, v2));
		assertEquals(v2, LongValue.max(v2, v1));

		try
		{
			LongValue.max(v1, null);
			fail("Should have failed on null arg");
		}
		catch (IllegalArgumentException e)
		{
			// expected
		}

		try
		{
			LongValue.max(null, v1);
			fail("Should have failed on null arg");
		}
		catch (IllegalArgumentException e)
		{
			// expected
		}
	}

	/** Test {@link LongValue#maxNullSafe(LongValue, LongValue)} */
	@Test
	public void maxNullSafe()
	{
		LongValue v1 = new LongValue(1);
		LongValue v2 = new LongValue(2);

		assertEquals(v2, LongValue.maxNullSafe(v1, v2));
		assertEquals(v2, LongValue.maxNullSafe(v2, v1));
		assertEquals(v2, LongValue.maxNullSafe(null, v2));
		assertEquals(v2, LongValue.maxNullSafe(v2, null));
		assertNull(LongValue.maxNullSafe(null, null));
	}
}

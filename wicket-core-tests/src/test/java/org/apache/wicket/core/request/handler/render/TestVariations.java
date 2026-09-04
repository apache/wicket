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
package org.apache.wicket.core.request.handler.render;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TestVariations
{
	@Test
	void testSingle()
	{
		VariationIterator<Boolean> single = VariationIterator.of(Variation.ofBoolean());
		assertTrue(single.hasNext());
		single.next();
		assertTrue(single.hasNext());
		single.next();
		assertFalse(single.hasNext());

		Exception ex = null;
		try
		{
			single.next();
		}
		catch (Exception e)
		{
			ex = e;
		}
		assertNotNull(ex);
	}

	@Test
	void testDouble()
	{
		VariationIterator<Integer> numbers = VariationIterator.of(new Variation<>(1,2,3));
		VariationIterator<Boolean> flag = VariationIterator.of(numbers,Variation.ofBoolean());
		VariationIterator<?> last=flag;

		assertTrue(last.hasNext());
		last.next();
		assertTrue(last.hasNext());
		last.next();
		assertTrue(last.hasNext());
		last.next();
		assertTrue(last.hasNext());
		last.next();
		assertTrue(last.hasNext());
		last.next();
		assertTrue(last.hasNext());
		last.next();
		assertFalse(last.hasNext());

		Exception ex = null;
		try
		{
			last.next();
		}
		catch (Exception e)
		{
			ex = e;
		}
		assertNotNull(ex);
	}
}

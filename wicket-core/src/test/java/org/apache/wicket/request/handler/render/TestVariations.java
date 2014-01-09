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
package org.apache.wicket.request.handler.render;

import org.junit.Assert;
import org.junit.Test;

public class TestVariations
{
	@Test
	public void testSingle()
	{
		VariationIterator<Boolean> single = VariationIterator.of(Variation.ofBoolean());
		Assert.assertTrue(single.hasNext());
		single.nextVariation();
		Assert.assertFalse(single.hasNext());

		Exception ex = null;
		try
		{
			single.nextVariation();
		}
		catch (Exception e)
		{
			ex = e;
		}
		Assert.assertNotNull(ex);
	}

	@Test
	public void testDouble()
	{
		VariationIterator<Integer> numbers = VariationIterator.of(new Variation<>(1,2,3));
		VariationIterator<Boolean> flag = VariationIterator.of(numbers,Variation.ofBoolean());
		VariationIterator<?> last=flag;

		Assert.assertTrue(last.hasNext());
		last.nextVariation();
		Assert.assertTrue(last.hasNext());
		last.nextVariation();
		Assert.assertTrue(last.hasNext());
		last.nextVariation();
		Assert.assertTrue(last.hasNext());
		last.nextVariation();
		Assert.assertTrue(last.hasNext());
		last.nextVariation();
		Assert.assertFalse(last.hasNext());

		Exception ex = null;
		try
		{
			last.nextVariation();
		}
		catch (Exception e)
		{
			ex = e;
		}
		Assert.assertNotNull(ex);
	}
}

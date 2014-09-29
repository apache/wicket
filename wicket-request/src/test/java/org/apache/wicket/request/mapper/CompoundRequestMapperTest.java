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
package org.apache.wicket.request.mapper;

import org.apache.wicket.request.mapper.CompoundRequestMapper.MapperWithScore;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link CompoundRequestMapper}
 */
public class CompoundRequestMapperTest extends Assert
{
	/**
	 * Test {@link MapperWithScore#compareTo(MapperWithScore)}.
	 */
	@Test
	public void score()
	{
		assertTrue(score(0).compareTo(score(0)) == 0);
		assertTrue(score(0).compareTo(score(10)) > 0);
		assertTrue(score(10).compareTo(score(0)) < 0);
		assertTrue(score(0).compareTo(score(10)) > 0);
		assertTrue(score(-10).compareTo(score(0)) > 0);
		assertTrue(score(0).compareTo(score(-10)) < 0);
		assertTrue(score(10).compareTo(score(Integer.MIN_VALUE + 1)) < 0);
		assertTrue(score(Integer.MIN_VALUE + 1).compareTo(score(10)) > 0);
		assertTrue(score(10).compareTo(score(Integer.MAX_VALUE)) > 0);
		assertTrue(score(Integer.MAX_VALUE).compareTo(score(10)) < 0);
	}

	private MapperWithScore score(int score)
	{
		return new MapperWithScore(null, score);
	}
}

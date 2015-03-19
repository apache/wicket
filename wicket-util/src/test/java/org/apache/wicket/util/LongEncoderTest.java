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
package org.apache.wicket.util;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests {@link LongEncoder}
 * 
 * @author igor
 */
public class LongEncoderTest
{
	/**
	 * Tests the default alphabet included with the encoder
	 */
	@Test
	public void defaultAlphabet()
	{
		Set<String> encoded = new HashSet<>();
		for (int i = -10000; i < 10000; i++)
		{
			String enc = LongEncoder.encode(i);
			Assert.assertFalse("uniqueness: " + i, encoded.contains(enc));
			encoded.add(enc);
			Assert.assertEquals("decoding: " + i, i, LongEncoder.decode(enc));
		}
	}
}

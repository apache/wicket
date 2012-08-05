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

import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Assert;
import org.junit.Test;

/**
 * @since 1.5.8
 */
public class NumbersTest extends Assert
{
	@Test
	public void getMinValue()
	{
		assertEquals(Integer.MIN_VALUE, Numbers.getMinValue(Integer.class));
		assertEquals(Integer.MIN_VALUE, Numbers.getMinValue(int.class));
		assertEquals(Long.MIN_VALUE, Numbers.getMinValue(Long.class));
		assertEquals(Long.MIN_VALUE, Numbers.getMinValue(long.class));
		assertEquals(Float.MIN_VALUE, Numbers.getMinValue(Float.class));
		assertEquals(Float.MIN_VALUE, Numbers.getMinValue(float.class));
		assertEquals(Double.MIN_VALUE, Numbers.getMinValue(Double.class));
		assertEquals(Double.MIN_VALUE, Numbers.getMinValue(double.class));
		assertEquals(Byte.MIN_VALUE, Numbers.getMinValue(Byte.class));
		assertEquals(Byte.MIN_VALUE, Numbers.getMinValue(byte.class));
		assertEquals(Short.MIN_VALUE, Numbers.getMinValue(Short.class));
		assertEquals(Short.MIN_VALUE, Numbers.getMinValue(short.class));
		assertEquals(Double.MIN_VALUE, Numbers.getMinValue(BigDecimal.class));
		assertEquals(Double.MIN_VALUE, Numbers.getMinValue(BigInteger.class));
		assertEquals(Double.MIN_VALUE, Numbers.getMinValue(null));
	}

	@Test
	public void getMaxValue()
	{
		assertEquals(Integer.MAX_VALUE, Numbers.getMaxValue(Integer.class));
		assertEquals(Integer.MAX_VALUE, Numbers.getMaxValue(int.class));
		assertEquals(Long.MAX_VALUE, Numbers.getMaxValue(Long.class));
		assertEquals(Long.MAX_VALUE, Numbers.getMaxValue(long.class));
		assertEquals(Float.MAX_VALUE, Numbers.getMaxValue(Float.class));
		assertEquals(Float.MAX_VALUE, Numbers.getMaxValue(float.class));
		assertEquals(Double.MAX_VALUE, Numbers.getMaxValue(Double.class));
		assertEquals(Double.MAX_VALUE, Numbers.getMaxValue(double.class));
		assertEquals(Byte.MAX_VALUE, Numbers.getMaxValue(Byte.class));
		assertEquals(Byte.MAX_VALUE, Numbers.getMaxValue(byte.class));
		assertEquals(Short.MAX_VALUE, Numbers.getMaxValue(Short.class));
		assertEquals(Short.MAX_VALUE, Numbers.getMaxValue(short.class));
		assertEquals(Double.MAX_VALUE, Numbers.getMaxValue(BigDecimal.class));
		assertEquals(Double.MAX_VALUE, Numbers.getMaxValue(BigInteger.class));
		assertEquals(Double.MAX_VALUE, Numbers.getMaxValue(null));
	}
}

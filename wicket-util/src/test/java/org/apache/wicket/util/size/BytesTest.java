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
package org.apache.wicket.util.size;

import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.string.StringValueConversionException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test cases for this object
 * 
 * @author Jonathan Locke
 */
public final class BytesTest
{
	/**
	 * Backup of the default locale.
	 */
	private Locale defaultLocale = null;

	/**
	 * Save the default locale.
	 */
	@BeforeEach
	public void before()
	{
		defaultLocale = Locale.getDefault(Locale.Category.FORMAT);
	}

	/**
	 * Restore the default locale.
	 */
	@AfterEach
	public void after()
	{
		Locale.setDefault(Locale.Category.FORMAT, defaultLocale);
	}

	/**
	 * 
	 * @throws StringValueConversionException
	 */
	@Test
	public void allOperationsCurrentLocale() throws StringValueConversionException
	{
		assertEquals(Bytes.bytes(1024), Bytes.kilobytes(1));
		assertEquals(Bytes.bytes(1024 * 1024), Bytes.megabytes(1));
		assertEquals("1GB", Bytes.gigabytes(1).toString());


		final Bytes b = Bytes.kilobytes(7.3);

		assertEquals(b, Bytes.kilobytes(7.3));
		assertTrue(b.greaterThan(Bytes.kilobytes(7.25)));
		assertTrue(b.lessThan(Bytes.kilobytes(7.9)));
		assertEquals(Bytes.valueOf(b.toString()), b);
	}

	/**
	 * 
	 * @throws StringValueConversionException
	 */
	@Test
	public void stringOperationsDotLocale() throws StringValueConversionException
	{
		Locale.setDefault(Locale.UK);
		assertEquals("1GB", Bytes.gigabytes(1).toString());
		assertEquals(Bytes.valueOf("15.5KB").bytes(), ((15 * 1024) + 512));

		final Bytes b = Bytes.kilobytes(7.3);

		assertEquals(Bytes.valueOf(b.toString()), b);
	}

	/**
	 * 
	 * @throws StringValueConversionException
	 */
	@Test
	public void stringOperationsCommaLocale() throws StringValueConversionException
	{
		Locale.setDefault(Locale.GERMANY);
		assertEquals("1GB", Bytes.gigabytes(1).toString());
		assertEquals(Bytes.valueOf("15,5KB").bytes(), ((15 * 1024) + 512));

		final Bytes b = Bytes.kilobytes(7.3);

		assertEquals(Bytes.valueOf(b.toString()), b);
	}

	/**
	 * 
	 * @throws StringValueConversionException
	 */
	@Test
	public void allOperationsExplicitLocale() throws StringValueConversionException
	{
		assertEquals("1GB", Bytes.gigabytes(1).toString());
		assertEquals("1,5GB", Bytes.gigabytes(1.5).toString(Locale.GERMAN));
		assertEquals("1.5GB", Bytes.gigabytes(1.5).toString(Locale.US));

		final Bytes b = Bytes.kilobytes(7.3);
		assertEquals(b, Bytes.valueOf(b.toString(Locale.GERMAN), Locale.GERMAN));

		assertEquals(Bytes.valueOf("15,5KB", Locale.GERMAN).bytes(), ((15 * 1024) + 512));
		assertEquals(Bytes.valueOf("15.5KB", Locale.US).bytes(), ((15 * 1024) + 512));
	}
}

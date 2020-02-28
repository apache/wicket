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

import org.apache.wicket.util.string.StringValueConversionException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the <code>Bytes</code> class.
 */
public class BytesTest
{
	/**
	 * Backup of the default locale.
	 */
	private Locale originalFormatLocale = null;
	private Locale originalDefaultLocale = null;

	/**
	 * Save the default locale.
	 */
	@BeforeEach
	public void before()
	{
		originalFormatLocale = Locale.getDefault(Locale.Category.FORMAT);
		originalDefaultLocale = Locale.getDefault();

		// these tests run in US locale for formatting and German default locale - they should still
		// work with split
		// locale.
		Locale.setDefault(Locale.GERMANY);
		Locale.setDefault(Locale.Category.FORMAT, Locale.US);
	}

	/**
	 * Restore the default locale.
	 */
	@AfterEach
	public void after()
	{
		Locale.setDefault(originalDefaultLocale);
		Locale.setDefault(Locale.Category.FORMAT, originalFormatLocale);
	}

	/**
	 * Tests the values.
	 */
	@Test
	public void teraBytes()
	{
		assertEquals(Bytes.gigabytes(1024), Bytes.terabytes(1));
		assertEquals(Bytes.gigabytes(1024.0), Bytes.terabytes(1.0));
		assertEquals(Bytes.gigabytes(1024.0), Bytes.terabytes(1));

		assertEquals(1L, Bytes.bytes(1).bytes());
		assertEquals(1024L, Bytes.kilobytes(1).bytes());
		assertEquals(1024L * 1024, Bytes.megabytes(1).bytes());
		assertEquals(1024L * 1024 * 1024, Bytes.gigabytes(1).bytes());
		assertEquals(1024L * 1024 * 1024 * 1024, Bytes.terabytes(1).bytes());

		assertEquals(1.5, Bytes.bytes(1536).kilobytes());
		assertEquals(1.0, Bytes.kilobytes(1).kilobytes());
		assertEquals(0.5, Bytes.bytes(512).kilobytes());

		assertEquals(1.5, Bytes.kilobytes(1536).megabytes());
		assertEquals(1.0, Bytes.megabytes(1).megabytes());
		assertEquals(0.5, Bytes.kilobytes(512).megabytes());

		assertEquals(1.5, Bytes.megabytes(1536).gigabytes());
		assertEquals(1.0, Bytes.gigabytes(1).gigabytes());
		assertEquals(0.5, Bytes.megabytes(512).gigabytes());

		assertEquals(1.5, Bytes.gigabytes(1536).terabytes());
		assertEquals(1.0, Bytes.terabytes(1).terabytes());
		assertEquals(0.5, Bytes.gigabytes(512).terabytes());
	}

	/**
	 * Tests the valueOf method.
	 * 
	 * @throws StringValueConversionException
	 */
	@Test
	public void valueOf() throws StringValueConversionException
	{
		assertEquals(Bytes.valueOf("1024GB"), Bytes.valueOf("1TB"));
		assertEquals(Bytes.valueOf("1024MB"), Bytes.valueOf("1GB"));
		assertEquals(Bytes.valueOf("1024KB"), Bytes.valueOf("1MB"));
		assertEquals(Bytes.valueOf("1024B"), Bytes.valueOf("1KB"));

		assertEquals(Bytes.valueOf("2048GB"), Bytes.valueOf("2TB"));
		assertEquals(Bytes.valueOf("2048MB"), Bytes.valueOf("2GB"));
		assertEquals(Bytes.valueOf("2048KB"), Bytes.valueOf("2MB"));
		assertEquals(Bytes.valueOf("2048B"), Bytes.valueOf("2KB"));

		assertEquals(Bytes.valueOf("1024GB", Locale.GERMAN), Bytes.valueOf("1TB"));
		assertEquals(Bytes.valueOf("1024MB", Locale.GERMAN), Bytes.valueOf("1GB"));
		assertEquals(Bytes.valueOf("1024KB", Locale.GERMAN), Bytes.valueOf("1MB"));
		assertEquals(Bytes.valueOf("1024B", Locale.GERMAN), Bytes.valueOf("1KB"));

		assertEquals(Bytes.valueOf("2048GB", Locale.GERMAN), Bytes.valueOf("2TB"));
		assertEquals(Bytes.valueOf("2048MB", Locale.GERMAN), Bytes.valueOf("2GB"));
		assertEquals(Bytes.valueOf("2048KB", Locale.GERMAN), Bytes.valueOf("2MB"));
		assertEquals(Bytes.valueOf("2048B", Locale.GERMAN), Bytes.valueOf("2KB"));

		try
		{
			Bytes.valueOf("1PB");
			fail("Exception expected");
		}
		catch (StringValueConversionException e)
		{
			assertTrue(true);
		}
		try
		{
			Bytes.valueOf("baPB");
			fail("Exception expected");
		}
		catch (StringValueConversionException e)
		{
			assertTrue(true);
		}
	}

	/**
	 * Tests the toString() method.
	 */
	@Test
	public void testToString()
	{
		assertEquals("1 bytes", Bytes.bytes(1).toString());
		assertEquals("1KB", Bytes.bytes(1024).toString());
		assertEquals("1MB", Bytes.bytes(1024 * 1024L).toString());
		assertEquals("1GB", Bytes.bytes(1024 * 1024 * 1024L).toString());
		assertEquals("1TB", Bytes.bytes(1024 * 1024 * 1024 * 1024L).toString());
		assertEquals("1.5KB", Bytes.bytes(1024 * 1.5).toString());

		assertEquals("1 bytes", Bytes.bytes(1).toString(Locale.GERMAN));
	}

	/**
	 * Negative values are not supported
	 */
	@Test
	public void negative()
	{
		assertThrows(IllegalArgumentException.class, () -> {
			Bytes.bytes(-1);
			fail("Bytes should not support negative values!");
		});

	}
}

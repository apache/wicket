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
package wicket.util.size;


import java.util.Locale;

import junit.framework.Assert;
import junit.framework.TestCase;
import wicket.util.lang.Bytes;
import wicket.util.string.StringValueConversionException;

/**
 * Test cases for this object
 * 
 * @author Jonathan Locke
 */
public final class BytesTest extends TestCase
{
	/**
	 * Backup of the default locale.
	 */
	private Locale defaultLocale = null;

	/**
	 * Save the default locale.
	 */
	public void setUp()
	{
		defaultLocale = Locale.getDefault();
	}

	/**
	 * Restore the default locale.
	 */
	public void tearDown()
	{
		Locale.setDefault(defaultLocale);
	}

	/**
	 * 
	 * @throws StringValueConversionException
	 */
	public void testAllOperationsCurrentLocale() throws StringValueConversionException
	{
		Assert.assertTrue(Bytes.bytes(1024).equals(Bytes.kilobytes(1)));
		Assert.assertTrue(Bytes.bytes(1024 * 1024).equals(Bytes.megabytes(1)));
		Assert.assertTrue("1G".equals(Bytes.gigabytes(1).toString()));

		final Bytes b = Bytes.kilobytes(7.3);

		Assert.assertTrue(b.equals(Bytes.kilobytes(7.3)));
		Assert.assertTrue(b.greaterThan(Bytes.kilobytes(7.25)));
		Assert.assertTrue(b.lessThan(Bytes.kilobytes(7.9)));
		Assert.assertTrue(Bytes.valueOf(b.toString()).equals(b));
	}

	/**
	 * 
	 * @throws StringValueConversionException
	 */
	public void testStringOperationsDotLocale() throws StringValueConversionException
	{
		Locale.setDefault(Locale.UK);
		Assert.assertTrue("1G".equals(Bytes.gigabytes(1).toString()));
		Assert.assertTrue(Bytes.valueOf("15.5K").bytes() == ((15 * 1024) + 512));

		final Bytes b = Bytes.kilobytes(7.3);

		Assert.assertTrue(Bytes.valueOf(b.toString()).equals(b));
	}

	/**
	 * 
	 * @throws StringValueConversionException
	 */
	public void testStringOperationsCommaLocale() throws StringValueConversionException
	{
		Locale.setDefault(Locale.GERMANY);
		Assert.assertTrue("1G".equals(Bytes.gigabytes(1).toString()));
		Assert.assertTrue(Bytes.valueOf("15,5K").bytes() == ((15 * 1024) + 512));

		final Bytes b = Bytes.kilobytes(7.3);

		Assert.assertTrue(Bytes.valueOf(b.toString()).equals(b));
	}

	/**
	 * 
	 * @throws StringValueConversionException
	 */
	public void testAllOperationsExplicitLocale() throws StringValueConversionException
	{
		Assert.assertTrue("1G".equals(Bytes.gigabytes(1).toString()));
		Assert.assertTrue("1,5G".equals(Bytes.gigabytes(1.5).toString(Locale.GERMAN)));
		Assert.assertTrue("1.5G".equals(Bytes.gigabytes(1.5).toString(Locale.US)));

		final Bytes b = Bytes.kilobytes(7.3);
		Assert.assertEquals(b, Bytes.valueOf(b.toString(Locale.GERMAN), Locale.GERMAN));

		Assert.assertTrue(Bytes.valueOf("15,5K", Locale.GERMAN).bytes() == ((15 * 1024) + 512));
		Assert.assertTrue(Bytes.valueOf("15.5K", Locale.US).bytes() == ((15 * 1024) + 512));
	}
}

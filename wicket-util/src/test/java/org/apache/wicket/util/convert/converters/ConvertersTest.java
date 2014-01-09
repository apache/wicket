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
package org.apache.wicket.util.convert.converters;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.converter.BigDecimalConverter;
import org.apache.wicket.util.convert.converter.BooleanConverter;
import org.apache.wicket.util.convert.converter.ByteConverter;
import org.apache.wicket.util.convert.converter.CalendarConverter;
import org.apache.wicket.util.convert.converter.CharacterConverter;
import org.apache.wicket.util.convert.converter.DateConverter;
import org.apache.wicket.util.convert.converter.DoubleConverter;
import org.apache.wicket.util.convert.converter.FloatConverter;
import org.apache.wicket.util.convert.converter.IntegerConverter;
import org.apache.wicket.util.convert.converter.LongConverter;
import org.apache.wicket.util.convert.converter.ShortConverter;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the base converters.
 * 
 * @author Eelco Hillenius
 */
public final class ConvertersTest extends Assert
{
	/** Dutch locale for localized testing. */
	private static final Locale DUTCH_LOCALE = new Locale("nl", "NL");

	/**
	 * @throws Exception
	 */
	@Test
	public void thousandSeperator() throws Exception
	{
		BigDecimalConverter bdc = new BigDecimalConverter();
		assertEquals(new BigDecimal(3000), bdc.convertToObject("3 000", Locale.FRENCH));

		DoubleConverter dc = new DoubleConverter();
		assertEquals(3000, dc.convertToObject("3 000", Locale.FRENCH), 0.001);
	}

	/**
	 * WICKET-4988 nbsp between digits only
	 */
	@Test
	public void thousandSeperatorWithCurrency() throws Exception
	{
		FloatConverter fc = new FloatConverter()
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected NumberFormat newNumberFormat(Locale locale)
			{
				return NumberFormat.getCurrencyInstance(locale);
			}
		};

		// \u00A0 = nbsp
		// \u00A4 = currency symbol (unspecified currency)
		String string = "1\u00A0234,00 \u00A4";

		assertEquals(string, fc.convertToString(Float.valueOf(1234f), Locale.FRENCH));
		assertEquals(Float.valueOf(1234f), fc.convertToObject(string, Locale.FRENCH));
	}

	/**
	 * @throws Exception
	 *             WICKET-1344 public void testBigDecimalRounding() throws Exception {
	 *             BigDecimalConverter bdc = new BigDecimalConverter(); assertEquals("123.45",
	 *             bdc.convertToObject("123.45", Locale.ENGLISH).toString()); }
	 */

	/**
	 * Test boolean conversions.
	 */
	@Test
	public void booleanConversions()
	{
		BooleanConverter converter = new BooleanConverter();
		assertEquals(Boolean.FALSE, converter.convertToObject("", Locale.US));
		assertEquals("true", converter.convertToString(Boolean.TRUE, Locale.getDefault()));
		assertEquals("false", converter.convertToString(Boolean.FALSE, Locale.getDefault()));
		assertEquals(Boolean.TRUE, converter.convertToObject("true", Locale.getDefault()));
		assertEquals(Boolean.FALSE, converter.convertToObject("false", Locale.getDefault()));
		try
		{
			converter.convertToObject("whatever", Locale.getDefault());
			fail("Conversion should have thrown an exception");
		}
		catch (ConversionException e)
		{
			// this is correct
		}
	}

	/**
	 * Test byte conversions.
	 */
	@Test
	public void byteConversions()
	{
		ByteConverter converter = new ByteConverter();
		assertNull(converter.convertToObject("", Locale.US));
		assertEquals(new Byte((byte)10), converter.convertToObject("10", Locale.US));
		assertEquals("10", converter.convertToString((byte)10, Locale.US));
		try
		{
			converter.convertToObject("whatever", Locale.US);
			fail("Conversion should have thrown an exception");
		}
		catch (ConversionException e)
		{
			// This is correct
		}
		try
		{
			converter.convertToObject("10whatever", Locale.US);
			fail("Conversion should have thrown an exception");
		}
		catch (ConversionException e)
		{
			// This is correct
		}
		try
		{
			converter.convertToObject("256", Locale.US);
			fail("Conversion should have thrown an exception");
		}
		catch (ConversionException e)
		{
			// This is correct
		}
	}

	/**
	 * Test double conversions.
	 */
	@Test
	public void doubleConversions()
	{
		DoubleConverter converter = new DoubleConverter();
		assertEquals("7.1", converter.convertToString(7.1, Locale.US));
		assertEquals("7,1", converter.convertToString(7.1, DUTCH_LOCALE));
		assertNull(converter.convertToObject("", Locale.US));
		assertEquals(1.1, converter.convertToObject("1.1", Locale.US), 0.001);
		assertEquals("1.1", converter.convertToString(1.1, Locale.US));
		try
		{
			converter.convertToObject("whatever", Locale.US);
			fail("Conversion should have thrown an exception");
		}
		catch (ConversionException e)
		{
			// this is correct
		}
		try
		{
			converter.convertToObject("1.1whatever", Locale.US);
			fail("Conversion should have thrown an exception");
		}
		catch (ConversionException e)
		{
			// this is correct
		}
	}

	/**
	 * Test float conversions.
	 */
	@Test
	public void floatConversions()
	{
		FloatConverter converter = new FloatConverter();
		assertNull(converter.convertToObject("", Locale.US));
		assertEquals(new Float(1.1), converter.convertToObject("1.1", Locale.US));
		assertEquals("1.1", converter.convertToString(new Float(1.1), Locale.US));
		try
		{
			converter.convertToObject("whatever", Locale.US);
			fail("Conversion should have thrown an exception");
		}
		catch (ConversionException e)
		{
			// this is correct
		}
		try
		{
			converter.convertToObject("1.1whatever", Locale.US);
			fail("Conversion should have thrown an exception");
		}
		catch (ConversionException e)
		{
			// this is correct
		}
	}

	/**
	 * Test integer conversions.
	 */
	@Test
	public void integerConversions()
	{
		IntegerConverter converter = new IntegerConverter();
		assertEquals("7", converter.convertToString(7, Locale.US));
		assertNull(converter.convertToObject("", Locale.US));
		assertEquals(new Integer(10), converter.convertToObject("10", Locale.US));
		assertEquals("10", converter.convertToString(10, Locale.US));
		try
		{
			converter.convertToObject("whatever", Locale.US);
			fail("Conversion should have thrown an exception");
		}
		catch (ConversionException e)
		{
			// This is correct
		}
		try
		{
			converter.convertToObject("10whatever", Locale.US);
			fail("Conversion should have thrown an exception");
		}
		catch (ConversionException e)
		{
			// This is correct
		}
		try
		{
			converter.convertToObject("" + ((long)Integer.MAX_VALUE + 1), Locale.US);
			fail("Conversion should have thrown an exception");
		}
		catch (ConversionException e)
		{
			// This is correct
		}
	}

	/**
	 * Test long conversions.
	 */
	@Test
	public void longConversions()
	{
		LongConverter converter = new LongConverter();
		assertNull(converter.convertToObject("", Locale.US));
		assertEquals(new Long(10), converter.convertToObject("10", Locale.US));
		assertEquals("10", converter.convertToString((long)10, Locale.US));
		try
		{
			converter.convertToObject("whatever", Locale.US);
			fail("Conversion should have thrown an exception");
		}
		catch (ConversionException e)
		{
			// This is correct
		}
		try
		{
			converter.convertToObject("10whatever", Locale.US);
			fail("Conversion should have thrown an exception");
		}
		catch (ConversionException e)
		{
			// This is correct
		}
		try
		{
			converter.convertToObject("" + Long.MAX_VALUE + "0", Locale.US);
			fail("Conversion should have thrown an exception");
		}
		catch (ConversionException e)
		{
			// This is correct
		}
	}

	/**
	 * Test short conversions
	 */
	@Test
	public void shortConversions()
	{
		ShortConverter converter = new ShortConverter();
		assertNull(converter.convertToObject("", Locale.US));
		assertEquals(new Short((short)10), converter.convertToObject("10", Locale.US));
		assertEquals("10", converter.convertToString((short)10, Locale.US));
		try
		{
			converter.convertToObject("whatever", Locale.US);
			fail("Conversion should have thrown an exception");
		}
		catch (ConversionException e)
		{
			// This is correct
		}
		try
		{
			converter.convertToObject("10whatever", Locale.US);
			fail("Conversion should have thrown an exception");
		}
		catch (ConversionException e)
		{
			// This is correct
		}
		try
		{
			converter.convertToObject("" + (Short.MAX_VALUE + 1), Locale.US);
			fail("Conversion should have thrown an exception");
		}
		catch (ConversionException e)
		{
			// This is correct
		}
	}

	/**
	 * Test for character locale conversions.
	 */
	@Test
	public void characterConverter()
	{
		CharacterConverter converter = new CharacterConverter();

		assertNull(converter.convertToObject("", Locale.US));
		assertEquals("A", converter.convertToString('A', DUTCH_LOCALE));
		assertEquals((Object)'A', converter.convertToObject("A", DUTCH_LOCALE));

		try
		{
			converter.convertToObject("AA", Locale.US);
			fail("Conversion should have thrown an exception");
		}
		catch (ConversionException e)
		{
			// this is correct
		}
	}

	/**
	 * Test date locale conversions.
	 */
	@Test
	public void dateConverter()
	{
		DateConverter converter = new DateConverter();

		assertNull(new DateConverter().convertToObject("", Locale.US));

		Calendar cal = Calendar.getInstance(DUTCH_LOCALE);
		cal.clear();
		cal.set(2002, Calendar.OCTOBER, 24);
		Date date = cal.getTime();

		assertEquals("24-10-02", converter.convertToString(date, DUTCH_LOCALE));
		assertEquals(date, converter.convertToObject("24-10-02", DUTCH_LOCALE));

		assertEquals("10/24/02", converter.convertToString(date, Locale.US));
		assertEquals(date, converter.convertToObject("10/24/02", Locale.US));

		try
		{
			converter.convertToObject("whatever", Locale.US);
			fail("Conversion should have thrown an exception");
		}
		catch (ConversionException e)
		{
			// this is correct
		}
		try
		{
			converter.convertToObject("10/24/02whatever", Locale.US);
			fail("Conversion should have thrown an exception");
		}
		catch (ConversionException e)
		{
			// this is correct
		}
	}

	/**
	 * Test calendar locale conversions.
	 */
	@Test
	public void calendarConverter()
	{
		CalendarConverter converter = new CalendarConverter();

		Calendar cal = Calendar.getInstance(DUTCH_LOCALE);
		cal.clear();
		cal.set(2011, Calendar.MAY, 1);

		assertEquals("1-5-11", converter.convertToString(cal, DUTCH_LOCALE));
		assertEquals(cal, converter.convertToObject("1-5-11", DUTCH_LOCALE));

		cal = Calendar.getInstance(Locale.US);
		cal.clear();
		cal.set(2011, Calendar.MAY, 1);
		assertEquals("5/1/11", converter.convertToString(cal, Locale.US));
		assertEquals(cal, converter.convertToObject("5/1/11", Locale.US));

		try
		{
			converter.convertToObject("whatever", Locale.US);
			fail("Conversion should have thrown an exception");
		}
		catch (ConversionException e)
		{
			// this is correct
		}
		try
		{
			converter.convertToObject("5/1/11whatever", Locale.US);
			fail("Conversion should have thrown an exception");
		}
		catch (ConversionException e)
		{
			// this is correct
		}
	}

	/**
	 * See WICKET-2878 and
	 * http://java.sun.com/j2se/1.4.2/docs/api/java/math/BigDecimal
	 * .html#BigDecimal%28double%29
	 */
	@Test
	public void bigDecimalsDoubles()
	{
		BigDecimal bd = new BigDecimalConverter().convertToObject("0.1", Locale.US);
		assertTrue(bd.doubleValue() == 0.1d);

		bd = new BigDecimalConverter().convertToObject("0,1", Locale.GERMAN);
		assertTrue(bd.doubleValue() == 0.1d);
	}
}
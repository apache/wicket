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
import java.text.ChoiceFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.converter.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

@SuppressWarnings("javadoc")
public final class ConvertersTest
{
	/** Dutch locale for localized testing. **/
	private static final Locale DUTCH_LOCALE = new Locale("nl", "NL");

	@Test
	public void thousandSeperator()
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
	public void thousandSeperatorWithCurrency()
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
		String string = "1\u00A0234,00\u00A0\u00A4";

		assertEquals(string, fc.convertToString(Float.valueOf(1234f), Locale.FRENCH));
		assertEquals(Float.valueOf(1234f), fc.convertToObject(string, Locale.FRENCH));
	}

	@Test
	public void validBooleanConversions()
	{
		BooleanConverter converter = new BooleanConverter();
		assertEquals(Boolean.FALSE, converter.convertToObject("", Locale.US));
		assertEquals("true", converter.convertToString(Boolean.TRUE, Locale.getDefault(Locale.Category.FORMAT)));
		assertEquals("false", converter.convertToString(Boolean.FALSE, Locale.getDefault(Locale.Category.FORMAT)));
		assertEquals(Boolean.TRUE, converter.convertToObject("true", Locale.getDefault(Locale.Category.FORMAT)));
		assertEquals(Boolean.FALSE, converter.convertToObject("false", Locale.getDefault(Locale.Category.FORMAT)));
	}

	@Test
	public void invalidBooleanConversion()
	{
		BooleanConverter converter = new BooleanConverter();

		assertThrows(ConversionException.class, () -> {
			converter.convertToObject("whatever", Locale.getDefault(Locale.Category.FORMAT));
		});
	}

	@Test
	public void validByteConversions()
	{
		ByteConverter converter = new ByteConverter();
		assertNull(converter.convertToObject("", Locale.US));
		assertEquals(Byte.valueOf((byte)10), converter.convertToObject("10", Locale.US));
		assertEquals("10", converter.convertToString((byte)10, Locale.US));
	}

	@Test
	public void invalidByteConversion1()
	{
		ByteConverter converter = new ByteConverter();
		assertThrows(ConversionException.class, () -> {
			converter.convertToObject("whatever", Locale.US);
		});
	}

	@Test
	public void invalidByteConversion2()
	{
		ByteConverter converter = new ByteConverter();
		assertThrows(ConversionException.class, () -> {
			converter.convertToObject("10whatever", Locale.US);
		});
	}

	@Test
	public void invalidByteConversion3()
	{
		ByteConverter converter = new ByteConverter();
		assertThrows(ConversionException.class, () -> {
			converter.convertToObject("256", Locale.US);
		});
	}

	@Test
	public void validDoubleConversions()
	{
		DoubleConverter converter = new DoubleConverter();
		assertEquals("7.1", converter.convertToString(7.1, Locale.US));
		assertEquals("7,1", converter.convertToString(7.1, DUTCH_LOCALE));
		assertNull(converter.convertToObject("", Locale.US));
		assertEquals(1.1, converter.convertToObject("1.1", Locale.US), 0.001);
		assertEquals("1.1", converter.convertToString(1.1, Locale.US));
	}

	@Test
	public void invalidDoubleConversion1()
	{
		DoubleConverter converter = new DoubleConverter();
		assertThrows(ConversionException.class, () -> {
			converter.convertToObject("whatever", Locale.US);
		});
	}

	@Test
	public void invalidDoubleConversion2()
	{
		DoubleConverter converter = new DoubleConverter();
		assertThrows(ConversionException.class, () -> {
			converter.convertToObject("1.1whatever", Locale.US);
		});
	}

	@Test
	public void invalidDoubleConversion3()
	{
		DoubleConverter converter = new DoubleConverter();
		assertThrows(ConversionException.class, () -> {
			converter.convertToObject("1" + Double.MAX_VALUE, Locale.US);
		});
	}

	@Test
	public void validFloatConversions()
	{
		FloatConverter converter = new FloatConverter();
		assertNull(converter.convertToObject("", Locale.US));
		assertEquals(Float.valueOf(1.1F), converter.convertToObject("1.1", Locale.US));
		assertEquals("1.1", converter.convertToString(1.1F, Locale.US));
	}

	@Test
	public void invalidFloatConversion1()
	{
		FloatConverter converter = new FloatConverter();
		assertThrows(ConversionException.class, () -> {
			converter.convertToObject("whatever", Locale.US);
		});
	}

	@Test
	public void invalidFloatConversion2()
	{
		FloatConverter converter = new FloatConverter();
		assertThrows(ConversionException.class, () -> {
			converter.convertToObject("1.1whatever", Locale.US);
		});
	}

	@Test
	public void invalidFloatConversion3()
	{
		FloatConverter converter = new FloatConverter();
		assertThrows(ConversionException.class, () -> {
			converter.convertToObject("1" + Float.MAX_VALUE, Locale.US);
		});
	}

	@Test
	public void validIntegerConversions()
	{
		IntegerConverter converter = new IntegerConverter();
		assertEquals("7", converter.convertToString(7, Locale.US));
		assertNull(converter.convertToObject("", Locale.US));
		assertEquals(Integer.valueOf(10), converter.convertToObject("10", Locale.US));
		assertEquals("10", converter.convertToString(10, Locale.US));
	}

	@Test
	public void invalidIntegerConversion1()
	{
		IntegerConverter converter = new IntegerConverter();
		assertThrows(ConversionException.class, () -> {
			converter.convertToObject("whatever", Locale.US);
		});
	}

	@Test
	public void invalidIntegerConversion2()
	{
		IntegerConverter converter = new IntegerConverter();
		assertThrows(ConversionException.class, () -> {
			converter.convertToObject("10whatever", Locale.US);
		});
	}

	@Test
	public void invalidIntegerConversion3()
	{
		IntegerConverter converter = new IntegerConverter();
		assertThrows(ConversionException.class, () -> {
			converter.convertToObject("1.0", Locale.US);
		});
	}

	@Test
	public void invalidIntegerConversion4()
	{
		IntegerConverter converter = new IntegerConverter();
		assertThrows(ConversionException.class, () -> {
			converter.convertToObject("1" + Integer.MAX_VALUE, Locale.US);
		});
	}

	@Test
	public void validLongConversions()
	{
		LongConverter converter = new LongConverter();
		assertNull(converter.convertToObject("", Locale.US));
		assertEquals(Long.valueOf(10), converter.convertToObject("10", Locale.US));
		assertEquals("10", converter.convertToString(10L, Locale.US));
	}

	@Test
	public void invalidLongConversion1()
	{
		LongConverter converter = new LongConverter();
		assertThrows(ConversionException.class, () -> {
			converter.convertToObject("whatever", Locale.US);
		});
	}

	@Test
	public void invalidLongConversion2()
	{
		LongConverter converter = new LongConverter();
		assertThrows(ConversionException.class, () -> {
			converter.convertToObject("10whatever", Locale.US);
		});
	}

	@Test
	public void invalidLongConversion3()
	{
		LongConverter converter = new LongConverter();
		assertThrows(ConversionException.class, () -> {
			converter.convertToObject("1" + Long.MAX_VALUE, Locale.US);
		});
	}

	@Test
	public void invalidLongConversion4()
	{
		LongConverter converter = new LongConverter();

		// WICKET-5853 assert that the compared number is out of range of Long
		final String biggerThanLong = "9223372036854776833";
		assertEquals(1,
			new BigDecimal(biggerThanLong).compareTo(BigDecimal.valueOf(Long.MAX_VALUE)));
		assertThrows(ConversionException.class, () -> {
			converter.convertToObject(biggerThanLong, Locale.US);
		});
	}

	@Test
	public void invalidLongConversion5()
	{
		LongConverter converter = new LongConverter();

		// WICKET-5853 assert that the compared number is out of range of Long
		final String biggerThanLong = "9223372036854776832";
		assertEquals(1,
			new BigDecimal(biggerThanLong).compareTo(BigDecimal.valueOf(Long.MAX_VALUE)));
		assertThrows(ConversionException.class, () -> {
			converter.convertToObject(biggerThanLong, Locale.US);
		});
	}

	@Test
	public void shortConversions()
	{
		ShortConverter converter = new ShortConverter();
		assertNull(converter.convertToObject("", Locale.US));
		assertEquals(Short.valueOf((short)10), converter.convertToObject("10", Locale.US));
		assertEquals("10", converter.convertToString((short) 10, Locale.US));
	}

	@Test
	public void invalidShortConversion1()
	{
		ShortConverter converter = new ShortConverter();
		assertThrows(ConversionException.class, () -> {
			converter.convertToObject("whatever", Locale.US);
		});
	}

	@Test
	public void invalidShortConversion2()
	{
		ShortConverter converter = new ShortConverter();
		assertThrows(ConversionException.class, () -> {
			converter.convertToObject("10whatever", Locale.US);
		});
	}

	@Test
	public void invalidShortConversion3()
	{
		ShortConverter converter = new ShortConverter();
		assertThrows(ConversionException.class, () -> {
			converter.convertToObject("" + (Short.MAX_VALUE + 1), Locale.US);
		});
	}

	@Test
	public void validCharacterConverter()
	{
		CharacterConverter converter = new CharacterConverter();

		assertNull(converter.convertToObject("", Locale.US));
		assertEquals("A", converter.convertToString('A', DUTCH_LOCALE));
		assertEquals((Object)'A', converter.convertToObject("A", DUTCH_LOCALE));
	}

	@Test
	public void invalidCharacterConversion1()
	{
		CharacterConverter converter = new CharacterConverter();
		assertThrows(ConversionException.class, () -> {
			converter.convertToObject("AA", Locale.US);
		});
	}

	@Test
	public void validDateConverters()
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
	}

	@Test
	public void invalidDateConversion1()
	{
		DateConverter converter = new DateConverter();
		assertThrows(ConversionException.class, () -> {
			converter.convertToObject("whatever", Locale.US);
		});
	}

	@Test
	public void invalidDateConversion2()
	{
		DateConverter converter = new DateConverter();
		assertThrows(ConversionException.class, () -> {
			converter.convertToObject("10/24/02whatever", Locale.US);
		});
	}

	@Test
	public void calendarConverter()
	{
		CalendarConverter converter = new CalendarConverter();

		Calendar cal = Calendar.getInstance(DUTCH_LOCALE);
		cal.clear();
		cal.set(2011, Calendar.MAY, 1);

		assertEquals("01-05-11", converter.convertToString(cal, DUTCH_LOCALE));
		assertEquals(cal, converter.convertToObject("1-5-11", DUTCH_LOCALE));

		cal = Calendar.getInstance(Locale.US);
		cal.clear();
		cal.set(2011, Calendar.MAY, 1);
		assertEquals("5/1/11", converter.convertToString(cal, Locale.US));
		assertEquals(cal, converter.convertToObject("5/1/11", Locale.US));
	}

	@Test
	public void invalidCalendarConversion1()
	{
		CalendarConverter converter = new CalendarConverter();
		assertThrows(ConversionException.class, () -> {
			converter.convertToObject("whatever", Locale.US);
		});
	}

	@Test
	public void invalidCalendarConversion2()
	{
		CalendarConverter converter = new CalendarConverter();
		assertThrows(ConversionException.class, () -> {
			converter.convertToObject("5/1/11whatever", Locale.US);
		});
	}

	/**
	 * See WICKET-2878 and
	 * http://java.sun.com/j2se/1.4.2/docs/api/java/math/BigDecimal.html#BigDecimal%28double%29
	 */
	@Test
	public void bigDecimalConverter()
	{
		BigDecimal bd = new BigDecimalConverter().convertToObject("0.1", Locale.US);
		assertEquals(new BigDecimal("0.1"), bd);

		bd = new BigDecimalConverter().convertToObject("0,1", Locale.GERMAN);
		assertEquals(new BigDecimal("0.1"), bd);

		String max = "1" + Double.MAX_VALUE;
		bd = new BigDecimalConverter().convertToObject(max, Locale.US);
		assertEquals(new BigDecimal(max), bd);
	}

	@Test
	public void customFormat()
	{
		@SuppressWarnings("serial")
		IntegerConverter converter = new IntegerConverter()
		{
			protected NumberFormat newNumberFormat(Locale locale)
			{
				return new ChoiceFormat(new double[] { 1, 2, 3 },
					new String[] { "one", "two", "three" });
			}
		};

		Integer integer = converter.convertToObject("two", Locale.US);
		assertEquals(Integer.valueOf(2), integer);
	}

	@Test
	public void invalidCustomConversion1()
	{
		@SuppressWarnings("serial")
		IntegerConverter converter = new IntegerConverter()
		{
			protected NumberFormat newNumberFormat(Locale locale)
			{
				return new ChoiceFormat(new double[] { 1, 2, 3 },
					new String[] { "one", "two", "three" });
			}
		};

		assertThrows(ConversionException.class, () -> {
			converter.convertToObject("four", Locale.US);
		});
	}
}

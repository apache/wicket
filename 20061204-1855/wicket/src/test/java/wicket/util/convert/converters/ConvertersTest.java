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
package wicket.util.convert.converters;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import junit.framework.TestCase;
import wicket.util.convert.ConversionException;
import wicket.util.convert.ConverterFactory;
import wicket.util.convert.IConverter;

/**
 * Tests for the base converters.
 * 
 * @author Eelco Hillenius
 */
public final class ConvertersTest extends TestCase
{
	/** Dutch locale for localized testing. */
	private static final Locale DUTCH_LOCALE = new Locale("nl", "NL");

	/**
	 * Construct.
	 */
	public ConvertersTest()
	{
		super();
	}

	/**
	 * Construct.
	 * 
	 * @param name
	 */
	public ConvertersTest(String name)
	{
		super(name);
	}

	/**
	 * Test generalized conversion
	 */
	public void testConversion()
	{
        final IConverter converter = new ConverterFactory().newConverter(Locale.US);
        assertEquals(new Long(7), converter.convert(new Integer(7), Long.class));
        assertEquals("7", converter.convert(new Integer(7), String.class));
        assertEquals("7.1", converter.convert(new Double(7.1), String.class));
        converter.setLocale(DUTCH_LOCALE);
        assertEquals("7,1", converter.convert(new Double(7.1), String.class));
		
		Calendar cal = Calendar.getInstance(DUTCH_LOCALE);
		cal.clear();
		cal.set(2002, Calendar.OCTOBER, 24);
		Date date = cal.getTime();

		assertEquals(date, converter.convert("24-10-02", Date.class));
		assertEquals("24-10-02", converter.convert(date, String.class));

		// empty strings should return null, NOT throw NPEs
		assertNull(converter.convert("", Double.class));
		assertNull(converter.convert("", Long.class));
		assertNull(converter.convert("", Float.class));
		assertNull(converter.convert("", Integer.class));
		assertNull(converter.convert("", Byte.class));
		assertNull(converter.convert("", Character.class));
		assertNull(converter.convert("", Date.class));
		assertNull(converter.convert("", Short.class));
		assertEquals(Boolean.FALSE, converter.convert("", Boolean.class));
		assertNotNull(converter.convert("", String.class));
	}

	/**
	 * Test boolean conversions.
	 */
	public void testBooleanConversions()
	{
		assertEquals("true", new StringConverter().convert(Boolean.TRUE, Locale.getDefault()));
		assertEquals("false", new StringConverter().convert(Boolean.FALSE, Locale.getDefault()));
		assertEquals(Boolean.TRUE, new BooleanConverter().convert(Boolean.TRUE, Locale.getDefault()));
		assertEquals(Boolean.FALSE, new BooleanConverter().convert(Boolean.FALSE, Locale.getDefault()));
		assertEquals(Boolean.TRUE, new BooleanConverter().convert("true", Locale.getDefault()));
		assertEquals(Boolean.FALSE, new BooleanConverter().convert("false", Locale.getDefault()));
		try
		{
			new BooleanConverter().convert("whatever", Locale.getDefault());
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
	public void testByteConversions()
	{
		ByteConverter converter = new ByteConverter();
		assertEquals(new Byte((byte)10), converter.convert(Byte.valueOf("10"),Locale.US));
		assertEquals(new Byte((byte)10), converter.convert("10",Locale.US));
		assertEquals("10", new StringConverter().convert(new Byte((byte)10),Locale.US));
        try
        {
            converter.convert("whatever",Locale.US);
            fail("Conversion should have thrown an exception");
        }
        catch (ConversionException e)
        {
            // This is correct
        }
        try
        {
            converter.convert("10whatever",Locale.US);
            fail("Conversion should have thrown an exception");
        }
        catch (ConversionException e)
        {
            // This is correct
        }
		try
		{
			converter.convert("256",Locale.US);
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
	public void testDoubleConversions()
	{
		DoubleConverter converter = new DoubleConverter();
		assertEquals(new Double(1.1), converter.convert(new Double(1.1),Locale.US));
		assertEquals(new Double(1.1), converter.convert("1.1",Locale.US));
		assertEquals("1.1", new StringConverter().convert(new Double(1.1),Locale.US));
        try
        {
            converter.convert("whatever",Locale.US);
            fail("Conversion should have thrown an exception");
        }
        catch (ConversionException e)
        {
            // this is correct
        }
        try
        {
            converter.convert("1.1whatever",Locale.US);
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
	public void testFloatConversions()
	{
		FloatConverter converter = new FloatConverter();
		assertEquals(new Float(1.1), converter.convert(new Float(1.1),Locale.US));
		assertEquals(new Float(1.1), converter.convert("1.1",Locale.US));
		assertEquals("1.1", new StringConverter().convert(new Float(1.1),Locale.US));
        try
        {
            converter.convert("whatever",Locale.US);
            fail("Conversion should have thrown an exception");
        }
        catch (ConversionException e)
        {
            // this is correct
        }
        try
        {
            converter.convert("1.1whatever",Locale.US);
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
	public void testIntegerConversions()
	{
		IntegerConverter converter = new IntegerConverter();
		assertEquals(new Integer(10), converter.convert(Integer.valueOf("10"),Locale.US));
		assertEquals(new Integer(10), converter.convert("10",Locale.US));
		assertEquals("10", new StringConverter().convert(new Integer(10),Locale.US));
        try
        {
            converter.convert("whatever",Locale.US);
            fail("Conversion should have thrown an exception");
        }
        catch (ConversionException e)
        {
            // This is correct
        }
        try
        {
            converter.convert("10whatever",Locale.US);
            fail("Conversion should have thrown an exception");
        }
        catch (ConversionException e)
        {
            // This is correct
        }
		try
		{
			converter.convert("" + ((long)Integer.MAX_VALUE + 1),Locale.US);
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
	public void testLongConversions()
	{
		LongConverter converter = new LongConverter();
		assertEquals(new Long(10), converter.convert(Long.valueOf("10"),Locale.US));
		assertEquals(new Long(10), converter.convert("10",Locale.US));
		assertEquals("10", new StringConverter().convert(new Long(10),Locale.US));
        try
        {
            converter.convert("whatever",Locale.US);
            fail("Conversion should have thrown an exception");
        }
        catch (ConversionException e)
        {
            // This is correct
        }
        try
        {
            converter.convert("10whatever",Locale.US);
            fail("Conversion should have thrown an exception");
        }
        catch (ConversionException e)
        {
            // This is correct
        }
		try
		{
			new LongConverter().convert("" + Long.MAX_VALUE + "0",Locale.US);
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
	public void testShortConversions()
	{
		ShortConverter converter = new ShortConverter();
		assertEquals(new Short((short)10), converter.convert(Short.valueOf("10"),Locale.US));
		assertEquals(new Short((short)10), converter.convert("10",Locale.US));
		assertEquals("10", new StringConverter().convert(new Short((short)10),Locale.US));
        try
        {
            converter.convert("whatever",Locale.US);
            fail("Conversion should have thrown an exception");
        }
        catch (ConversionException e)
        {
            // This is correct
        }
        try
        {
            converter.convert("10whatever",Locale.US);
            fail("Conversion should have thrown an exception");
        }
        catch (ConversionException e)
        {
            // This is correct
        }
		try
		{
			converter.convert("" + (Short.MAX_VALUE + 1),Locale.US);
			fail("Conversion should have thrown an exception");
		}
		catch (ConversionException e)
		{
			// This is correct
		}
	}

	/**
	 * Test date locale conversions.
	 */
	public void testDateConverter()
	{
		DateConverter converter = new DateConverter();
		StringConverter stringConverter = new StringConverter();

		Calendar cal = Calendar.getInstance(DUTCH_LOCALE);
		cal.clear();
		cal.set(2002, Calendar.OCTOBER, 24);
		Date date = cal.getTime();

		assertEquals("24-10-02", stringConverter.convert(date,DUTCH_LOCALE));
		assertEquals(date, converter.convert("24-10-02",DUTCH_LOCALE));

		assertEquals("10/24/02", stringConverter.convert(date,Locale.US));
		assertEquals(date, converter.convert("10/24/02",Locale.US));

        try
        {
            converter.convert("whatever",Locale.US);
            fail("Conversion should have thrown an exception");
        }
        catch (ConversionException e)
        {
            // this is correct
        }
        try
        {
            converter.convert("10/24/02whatever",Locale.US);
            fail("Conversion should have thrown an exception");
        }
        catch (ConversionException e)
        {
            // this is correct
        }
	}
}
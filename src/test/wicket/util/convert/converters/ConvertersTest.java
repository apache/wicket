/*
 * $Id$
 * $Revision$ $Date$
 * 
 * ================================================================================
 * Copyright (c) All rechten voorbehouden.
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

	public void testConversion()
	{
        final IConverter converter = new ConverterFactory().newConverter(Locale.US);
        assertEquals(new Long(7), converter.convert(new Integer(7), Long.class));
        assertEquals("7", converter.convert(new Integer(7), String.class));
        assertEquals("7.1", converter.convert(new Double(7.1), String.class));
        
        // TODO someone could add a whole lot more tests here, including DUTCH_LOCALE tests - JL
	}

	/**
	 * Test boolean conversions.
	 */
	public void testBooleanConversions()
	{
		assertEquals("true", new StringConverter().convert(Boolean.TRUE));
		assertEquals("false", new StringConverter().convert(Boolean.FALSE));
		assertEquals(Boolean.TRUE, new BooleanConverter().convert(Boolean.TRUE));
		assertEquals(Boolean.FALSE, new BooleanConverter().convert(Boolean.FALSE));
		assertEquals(Boolean.TRUE, new BooleanConverter().convert("true"));
		assertEquals(Boolean.FALSE, new BooleanConverter().convert("false"));
		try
		{
			new BooleanConverter().convert("whatever");
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
		ByteConverter converter = new ByteConverter(Locale.US);
		assertEquals(Locale.US, converter.getLocale());
		assertEquals(new Byte((byte)10), converter.convert(Byte.valueOf("10")));
		assertEquals(new Byte((byte)10), converter.convert("10"));
		assertEquals("10", new StringConverter().convert(new Byte((byte)10)));
		try
		{
			converter.convert("whatever");
			fail("Conversion should have thrown an exception");
		}
		catch (ConversionException e)
		{
			// This is correct
		}
		try
		{
			converter.convert("256");
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
		DoubleConverter converter = new DoubleConverter(Locale.US);
		assertEquals(Locale.US, converter.getLocale());
		assertEquals(new Double(1.1), converter.convert(new Double(1.1)));
		assertEquals(new Double(1.1), converter.convert("1.1"));
		assertEquals("1.1", new StringConverter(Locale.US).convert(new Double(1.1)));
		try
		{
			converter.convert("whatever");
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
		FloatConverter converter = new FloatConverter(Locale.US);
		assertEquals(new Float(1.1), converter.convert(new Float(1.1)));
		assertEquals(new Float(1.1), converter.convert("1.1"));
		assertEquals("1.1", new StringConverter(Locale.US).convert(new Float(1.1)));
		try
		{
			converter.convert("whatever");
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
		IntegerConverter converter = new IntegerConverter(Locale.US);
		assertEquals(Locale.US, converter.getLocale());
		assertEquals(new Integer(10), converter.convert(Integer.valueOf("10")));
		assertEquals(new Integer(10), converter.convert("10"));
		assertEquals("10", new StringConverter(Locale.US).convert(new Integer(10)));
		try
		{
			converter.convert("whatever");
			fail("Conversion should have thrown an exception");
		}
		catch (ConversionException e)
		{
			// This is correct
		}
		try
		{
			converter.convert("" + ((long)Integer.MAX_VALUE + 1));
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
		LongConverter converter = new LongConverter(Locale.US);
		assertEquals(Locale.US, converter.getLocale());
		assertEquals(new Long(10), converter.convert(Long.valueOf("10")));
		assertEquals(new Long(10), converter.convert("10"));
		assertEquals("10", new StringConverter(Locale.US).convert(new Long(10)));
		try
		{
			converter.convert("whatever");
			fail("Conversion should have thrown an exception");
		}
		catch (ConversionException e)
		{
			// This is correct
		}
		try
		{
			new LongConverter().convert("" + Long.MAX_VALUE + "0");
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
		ShortConverter converter = new ShortConverter(Locale.US);
		assertEquals(Locale.US, converter.getLocale());
		assertEquals(new Short((short)10), converter.convert(Short.valueOf("10")));
		assertEquals(new Short((short)10), converter.convert("10"));
		assertEquals("10", new StringConverter(Locale.US).convert(new Short((short)10)));
		try
		{
			converter.convert("whatever");
			fail("Conversion should have thrown an exception");
		}
		catch (ConversionException e)
		{
			// This is correct
		}
		try
		{
			converter.convert("" + (Short.MAX_VALUE + 1));
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
		DateConverter converter = new DateConverter(DUTCH_LOCALE);
		StringConverter stringConverter = new StringConverter(DUTCH_LOCALE);

		Calendar cal = Calendar.getInstance(DUTCH_LOCALE);
		cal.clear();
		cal.set(2002, Calendar.OCTOBER, 24);
		Date date = cal.getTime();

		assertEquals("24-10-02", stringConverter.convert(date));
		assertEquals(date, converter.convert("24-10-02"));

		converter.setLocale(Locale.US);
		stringConverter.setLocale(Locale.US);
		assertEquals("10/24/02", stringConverter.convert(date));
		assertEquals(date, converter.convert("10/24/02"));

		try
		{
			converter.convert("whatever");
			fail("Conversion should have thrown an exception");
		}
		catch (ConversionException e)
		{
			// this is correct
		}
	}
}
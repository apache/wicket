/*
 * $Id$ $Revision:
 * 1.2 $ $Date$
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

/**
 * Tests for the base converters.
 * 
 * @author Eelco Hillenius
 */
public final class ConvertersTest extends TestCase
{
	/** dutch locale for localized testing. */
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
        assertEquals(new Byte((byte)10), new ByteConverter().convert(Byte.valueOf("10")));
        assertEquals(new Byte((byte)10), new ByteConverter().convert("10"));
        assertEquals("10", new StringConverter().convert(new Byte((byte)10)));                
        try
        {
            new ByteConverter().convert("whatever");
            fail("Conversion should have thrown an exception");
        }
        catch (ConversionException e)
        {
            // This is correct
        }
        try
        {
            new ByteConverter().convert("256");
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
        assertEquals(new Double(1.1), new DoubleConverter().convert(new Double(1.1)));
        assertEquals(new Double(1.1), new DoubleConverter().convert("1.1"));
		assertEquals("1.1", new StringConverter().convert(new Double(1.1)));
		try
		{
			new DoubleConverter().convert("whatever");
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
        assertEquals(new Float(1.1), new FloatConverter().convert(new Float(1.1)));
        assertEquals(new Float(1.1), new FloatConverter().convert("1.1"));
        assertEquals("1.1", new StringConverter().convert(new Float(1.1)));
        try
        {
            new FloatConverter().convert("whatever");
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
        assertEquals(new Integer(10), new IntegerConverter().convert(Integer.valueOf("10")));
        assertEquals(new Integer(10), new IntegerConverter().convert("10"));
        assertEquals("10", new StringConverter().convert(new Integer(10)));                
        try
        {
            new IntegerConverter().convert("whatever");
            fail("Conversion should have thrown an exception");
        }
        catch (ConversionException e)
        {
            // This is correct
        }
        try
        {
            new IntegerConverter().convert("" + ((long)Integer.MAX_VALUE + 1));
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
        assertEquals(new Integer(10), new IntegerConverter().convert(Integer.valueOf("10")));
        assertEquals(new Integer(10), new IntegerConverter().convert("10"));
        assertEquals("10", new StringConverter().convert(new Integer(10)));                
        try
        {
            new IntegerConverter().convert("whatever");
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
        assertEquals(new Short((short)10), new ShortConverter().convert(Short.valueOf("10")));
        assertEquals(new Short((short)10), new ShortConverter().convert("10"));
        assertEquals("10", new StringConverter().convert(new Short((short)10)));                
        try
        {
            new ShortConverter().convert("whatever");
            fail("Conversion should have thrown an exception");
        }
        catch (ConversionException e)
        {
            // This is correct
        }
        try
        {
            new ShortConverter().convert("" + (Short.MAX_VALUE + 1));
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
        
        converter.setLocale(DUTCH_LOCALE);
        stringConverter.setLocale(DUTCH_LOCALE);

        Calendar cal = Calendar.getInstance(DUTCH_LOCALE);
		cal.clear();
		cal.set(2002, Calendar.OCTOBER, 24);
		Date date = cal.getTime();
        
		assertEquals("24-okt-2002", stringConverter.convert(date));
		assertEquals(date, converter.convert("24-okt-2002"));

		converter.setLocale(Locale.US);
        stringConverter.setLocale(Locale.US);
		assertEquals("Oct 24, 2002", stringConverter.convert(date));
		assertEquals(date, converter.convert("Oct 24, 2002"));
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
/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ================================================================================
 * Copyright (c)
 * All rechten voorbehouden.
 */
package wicket.util.convert.converters;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import wicket.util.convert.ConversionException;
import wicket.util.convert.converters.i18n.DateLocaleConverter;
import wicket.util.convert.converters.i18n.DoubleLocaleConverter;
import junit.framework.TestCase;

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
	 * @param name
	 */
	public ConvertersTest(String name)
	{
		super(name);
	}

	/**
	 * Test boolean converter.
	 */
	public void testBooleanConverter()
	{
		BooleanConverter conv = new BooleanConverter();
		assertEquals("true", conv.convert(Boolean.TRUE, String.class));
		assertEquals("false", conv.convert(Boolean.FALSE, String.class));
		assertEquals(Boolean.TRUE, conv.convert("true", Boolean.class));
		assertEquals(Boolean.FALSE, conv.convert("false", Boolean.class));
		try
		{
			conv.convert("foo", Integer.class);
			fail("illegal conversion attempt should have thrown an exception");
		}
		catch(ConversionException e)
		{
			// this is correct
		}
	}

	/**
	 * Test byte converter.
	 */
	public void testByteConverter()
	{
		ByteConverter conv = new ByteConverter();
		assertEquals("1", conv.convert(Byte.valueOf("1"), String.class));
		assertEquals(Byte.valueOf("1"), conv.convert("1", Byte.class));
		try
		{
			conv.convert("true", Boolean.class);
			fail("illegal conversion attempt should have thrown an exception");
		}
		catch(ConversionException e)
		{
			// this is correct
		}
		try
		{
			conv.convert("123456", Byte.class);
			fail("illegal conversion attempt should have thrown an exception");
		}
		catch(ConversionException e)
		{
			// this is correct
		}
	}

	/**
	 * Test character converter.
	 */
	public void testCharacterConverter()
	{
		CharacterConverter conv = new CharacterConverter();
		assertEquals("c", conv.convert(Character.valueOf('c'), String.class));
		assertEquals(Character.valueOf('c'), conv.convert("c", Character.class));
		try
		{
			conv.convert("true", Boolean.class);
			fail("illegal conversion attempt should have thrown an exception");
		}
		catch(ConversionException e)
		{
			// this is correct
		}
	}

	/**
	 * Test double converter.
	 */
	public void testDoubleConverter()
	{
		DoubleConverter conv = new DoubleConverter();
		assertEquals("1.1", conv.convert(Double.valueOf("1.1"), String.class));
		assertEquals(Double.valueOf("1.1"), conv.convert("1.1", Double.class));
		try
		{
			conv.convert("true", Boolean.class);
			fail("illegal conversion attempt should have thrown an exception");
		}
		catch(ConversionException e)
		{
			// this is correct
		}
	}

	/**
	 * Test float converter.
	 */
	public void testFloatConverter()
	{
		FloatConverter conv = new FloatConverter();
		assertEquals("1.1", conv.convert(Float.valueOf("1.1"), String.class));
		assertEquals(Float.valueOf("1.1"), conv.convert("1.1", Float.class));
		try
		{
			conv.convert("true", Boolean.class);
			fail("illegal conversion attempt should have thrown an exception");
		}
		catch(ConversionException e)
		{
			// this is correct
		}
	}

	/**
	 * Test integer converter.
	 */
	public void testIntegerConverter()
	{
		IntegerConverter conv = new IntegerConverter();
		assertEquals("10", conv.convert(Integer.valueOf("10"), String.class));
		assertEquals(Integer.valueOf("10"), conv.convert("10", Integer.class));
		try
		{
			conv.convert("true", Boolean.class);
			fail("illegal conversion attempt should have thrown an exception");
		}
		catch(ConversionException e)
		{
			// this is correct
		}
		try
		{
			conv.convert("1.1", Integer.class);
			fail("illegal conversion attempt should have thrown an exception");
		}
		catch(ConversionException e)
		{
			// this is correct
		}
	}

	/**
	 * Test long converter.
	 */
	public void testLongConverter()
	{
		LongConverter conv = new LongConverter();
		assertEquals("10", conv.convert(Long.valueOf("10"), String.class));
		assertEquals(Long.valueOf("10"), conv.convert("10", Integer.class));
		try
		{
			conv.convert("true", Boolean.class);
			fail("illegal conversion attempt should have thrown an exception");
		}
		catch(ConversionException e)
		{
			// this is correct
		}
		try
		{
			conv.convert("1.1", Long.class);
			fail("illegal conversion attempt should have thrown an exception");
		}
		catch(ConversionException e)
		{
			// this is correct
		}
	}

	/**
	 * Test short converter.
	 */
	public void testShortConverter()
	{
		ShortConverter conv = new ShortConverter();
		assertEquals("10", conv.convert(Short.valueOf("10"), String.class));
		assertEquals(Short.valueOf("10"), conv.convert("10", Short.class));
		try
		{
			conv.convert("true", Boolean.class);
			fail("illegal conversion attempt should have thrown an exception");
		}
		catch(ConversionException e)
		{
			// this is correct
		}
		try
		{
			conv.convert("1.1", Short.class);
			fail("illegal conversion attempt should have thrown an exception");
		}
		catch(ConversionException e)
		{
			// this is correct
		}
		try
		{
			conv.convert("32768", Short.class);
			fail("illegal conversion attempt should have thrown an exception");
		}
		catch(ConversionException e)
		{
			// this is correct
		}
	}

	/**
	 * Test double locale converter.
	 */
	public void testDoubleLocaleConverter()
	{
		DoubleLocaleConverter conv = new DoubleLocaleConverter();
		conv.setLocale(DUTCH_LOCALE);
		assertEquals("1,1", conv.convert(Double.valueOf("1.1"), String.class));
		assertEquals(Double.valueOf("1.1"), conv.convert("1,1", Double.class));
		assertEquals(Double.valueOf("11"), conv.convert("1.1", Double.class));
		try
		{
			conv.convert("true", Boolean.class);
			fail("illegal conversion attempt should have thrown an exception");
		}
		catch(ConversionException e)
		{
			// this is correct
		}
	}


	/**
	 * Test date locale converter.
	 */
	public void testDateLocaleConverter()
	{
		DateLocaleConverter conv = new DateLocaleConverter();
		conv.setLocale(DUTCH_LOCALE);
		Calendar cal = Calendar.getInstance(DUTCH_LOCALE);
		cal.clear(); // clear time fields for comparisonS
		cal.set(2002, 9, 24); // months from 0 - 11!
		Date date = cal.getTime();
		assertEquals("24-10-02", conv.convert(date, String.class));
		assertEquals(date, conv.convert("24-10-02", Date.class));

		conv.setLocale(Locale.US);
		assertEquals("10/24/02", conv.convert(date, String.class));
		assertEquals(date, conv.convert("10/24/02", Date.class));
		try
		{
			conv.convert("true", Boolean.class);
			fail("illegal conversion attempt should have thrown an exception");
		}
		catch(ConversionException e)
		{
			// this is correct
		}
	}
}

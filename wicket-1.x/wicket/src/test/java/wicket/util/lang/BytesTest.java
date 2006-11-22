package wicket.util.lang;

import java.util.Locale;

import junit.framework.TestCase;
import wicket.util.string.StringValueConversionException;

/**
 * Tests the <code>Bytes</code> class.
 */
public class BytesTest extends TestCase
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

		// these tests run in US locale.
		Locale.setDefault(Locale.US);
	}

	/**
	 * Restore the default locale.
	 */
	public void tearDown()
	{
		Locale.setDefault(defaultLocale);
	}

	/**
	 * Tests the values.
	 */
	public void testTeraBytes()
	{
		assertEquals(Bytes.gigabytes(1024), Bytes.terabytes(1));
		assertEquals(Bytes.gigabytes(1024.0), Bytes.terabytes(1.0));
		assertEquals(Bytes.gigabytes(1024.0), Bytes.terabytes(1));

		assertEquals(1L, Bytes.bytes(1).bytes());
		assertEquals(1024L, Bytes.kilobytes(1).bytes());
		assertEquals(1024L * 1024, Bytes.megabytes(1).bytes());
		assertEquals(1024L * 1024 * 1024, Bytes.gigabytes(1).bytes());
		assertEquals(1024L * 1024 * 1024 * 1024, Bytes.terabytes(1).bytes());

		assertEquals(1.5, Bytes.bytes(1536).kilobytes(), 0);
		assertEquals(1.0, Bytes.kilobytes(1).kilobytes(), 0);
		assertEquals(0.5, Bytes.bytes(512).kilobytes(), 0);

		assertEquals(1.5, Bytes.kilobytes(1536).megabytes(), 0);
		assertEquals(1.0, Bytes.megabytes(1).megabytes(), 0);
		assertEquals(0.5, Bytes.kilobytes(512).megabytes(), 0);

		assertEquals(1.5, Bytes.megabytes(1536).gigabytes(), 0);
		assertEquals(1.0, Bytes.gigabytes(1).gigabytes(), 0);
		assertEquals(0.5, Bytes.megabytes(512).gigabytes(), 0);

		assertEquals(1.5, Bytes.gigabytes(1536).terabytes(), 0);
		assertEquals(1.0, Bytes.terabytes(1).terabytes(), 0);
		assertEquals(0.5, Bytes.gigabytes(512).terabytes(), 0);
	}

	/**
	 * Tests the valueOf method.
	 * 
	 * @throws StringValueConversionException
	 */
	public void testValueOf() throws StringValueConversionException
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
	public void testToString()
	{
		assertEquals("1 bytes", Bytes.bytes(1).toString());
		assertEquals("1K", Bytes.bytes(1024).toString());
		assertEquals("1M", Bytes.bytes(1024 * 1024L).toString());
		assertEquals("1G", Bytes.bytes(1024 * 1024 * 1024L).toString());
		assertEquals("1T", Bytes.bytes(1024 * 1024 * 1024 * 1024L).toString());
		assertEquals("1.5K", Bytes.bytes(1024 * 1.5).toString());
		assertEquals("N/A", Bytes.bytes(-1).toString());

		assertEquals("1 bytes", Bytes.bytes(1).toString(Locale.GERMAN));
	}
}

package wicket.util.lang;

import java.io.Serializable;

import junit.framework.TestCase;

/**
 * Tests the Objects class.
 * 
 * @author Martijn Dashorst
 */
public class ObjectsTest extends TestCase
{
	/**
	 * Test method for 'wicket.util.lang.Objects.equal(Object, Object)'
	 */
	public void testEqual()
	{
		Object object = new Object();
		assertTrue(Objects.equal(object, object));

		assertFalse(Objects.equal(null, object));
		assertFalse(Objects.equal(object, null));
		assertTrue(Objects.equal(null, null));

		assertFalse(Objects.equal(new Object(), new Object()));
		assertTrue(Objects.equal(new Integer(1), new Integer(1)));
		assertFalse(Objects.equal("1", new Integer(1)));
		assertFalse(Objects.equal(new Integer(1), "1"));
		assertTrue(Objects.equal("1", new Integer(1).toString()));
		assertTrue(Objects.equal(new Integer(1).toString(), "1"));
	}

	/**
	 * Test method for 'wicket.util.lang.Objects.clone(Object)'
	 */
	public void testCloneNull()
	{
		Object clone = Objects.clone(null);
		assertEquals(null, clone);
	}

	/**
	 * Test method for 'wicket.util.lang.Objects.clone(Object)'
	 */
	public void testCloneString()
	{
		String cloneMe = "Mini-me";

		Object clone = Objects.clone(cloneMe);
		assertEquals(cloneMe, clone);
		assertNotSame(cloneMe, clone);
	}

	/**
	 * Test method for 'wicket.util.lang.Objects.clone(Object)'
	 */
	public void testCloneObject()
	{
		Object cloneMe = new Object();

		try
		{
			Objects.clone(cloneMe);
			fail("Exception expected");
		}
		catch (RuntimeException e)
		{
			assertTrue(true);
		}
	}

	/**
	 * Test method for 'wicket.util.lang.Objects.clone(Object)'
	 */
	public void testCloneCloneObject()
	{
		CloneObject cloneMe = new CloneObject();
		cloneMe.nr = 1;

		Object clone = Objects.clone(cloneMe);
		assertEquals(cloneMe, clone);
		assertNotSame(cloneMe, clone);
	}

	/**
	 * Used for testing the clone function.
	 */
	private static final class CloneObject implements Serializable
	{
		/**
		 * int for testing equality.
		 */
		private int nr;

		/**
		 * @see Object#equals(java.lang.Object)
		 */
		public boolean equals(Object o)
		{
			CloneObject other = (CloneObject)o;
			return other.nr == nr;
		}
	}
}

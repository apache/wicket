package wicket.util.lang;

import java.io.Serializable;

import wicket.MockPageWithOneComponent;
import wicket.WicketTestCase;
import wicket.markup.html.form.TextField;
import wicket.model.Model;
import wicket.model.PropertyModel;

/**
 * Tests the Objects class.
 * 
 * @author Martijn Dashorst
 */
public class ObjectsTest extends WicketTestCase
{

	/**
	 * Construct.
	 * 
	 * @param name
	 */
	public ObjectsTest(String name)
	{
		super(name);
	}

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
		Object clone = Objects.cloneModel(null);
		assertEquals(null, clone);
	}

	/**
	 * Test method for 'wicket.util.lang.Objects.clone(Object)'
	 */
	public void testCloneString()
	{
		String cloneMe = "Mini-me";

		Object clone = Objects.cloneModel(cloneMe);
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
			Objects.cloneModel((Serializable)cloneMe);
			fail("Exception expected");
		}
		catch (RuntimeException e)
		{
			assertTrue(true);
		}
	}

	/**
	 * Test method for component cloning
	 */
	public void testComponentClone()
	{
		PropertyModel pm = new PropertyModel(new TextField<String>(new MockPageWithOneComponent(), "component",
				new Model<String>("test")), "modelObject");
		PropertyModel pm2 = Objects.cloneModel(pm);
		assertTrue(pm.getObject() == pm2.getObject());
	}

	/**
	 * Test method for 'wicket.util.lang.Objects.clone(Object)'
	 */
	public void testCloneCloneObject()
	{
		CloneObject cloneMe = new CloneObject();
		cloneMe.nr = 1;

		Object clone = Objects.cloneModel(cloneMe);
		assertEquals(cloneMe, clone);
		assertNotSame(cloneMe, clone);
	}

	/**
	 * Used for testing the clone function.
	 */
	private static final class CloneObject implements Serializable
	{
		private static final long serialVersionUID = 1L;

		/**
		 * int for testing equality.
		 */
		private int nr;

		/**
		 * @see Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object o)
		{
			CloneObject other = (CloneObject)o;
			return other.nr == nr;
		}
	}
}

package wicket.util.lang;

import java.io.Serializable;
import java.lang.reflect.Field;

import wicket.MockPageWithOneComponent;
import wicket.WicketTestCase;
import wicket.behavior.SimpleAttributeModifier;
import wicket.markup.html.basic.SimplePage;
import wicket.markup.html.form.TextField;
import wicket.model.CompoundPropertyModel;
import wicket.model.Model;
import wicket.model.PropertyModel;
import wicket.util.collections.MiniMap;

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
	 * 
	 */
	public void testCloneModelByMapping()
	{
		PropertyModel<Integer> model = new PropertyModel<Integer>(new CloneObject(),"nr");
		model.setObject(1);
		assertEquals(1, (int)model.getObject());
		MiniMap<Field, Object> clonemap = Objects.mapObject(model);
		model.setObject(10);
		assertEquals(10, (int)model.getObject());
		
		Objects.restoreObject(model,clonemap);
		
		assertEquals(1, (int)model.getObject());
		
	}

	/**
	 * 
	 */
	public void testCloneModelByMappingWithFinal()
	{
		PropertyModel<Integer> model = new PropertyModel<Integer>(new CloneObject(),"test.nr");
		model.setObject(1);
		assertEquals(1, (int)model.getObject());
		MiniMap<Field, Object> clonemap = Objects.mapObject(model);
		model.setObject(10);
		assertEquals(10, (int)model.getObject());
		
		Objects.restoreObject(model,clonemap);
		
		assertEquals(1, (int)model.getObject());
		
	}
	
	/**
	 * 
	 */
	public void testCloneModelByMappingWithCompound()
	{
		PropertyModel<Integer> model = new PropertyModel<Integer>(new CompoundPropertyModel<CloneObject>(new CloneObject()),"nr");
		model.setObject(1);
		assertEquals(1, (int)model.getObject());
		MiniMap<Field, Object> clonemap = Objects.mapObject(model);
		model.setObject(10);
		assertEquals(10, (int)model.getObject());
		
		Objects.restoreObject(model,clonemap);
		
		assertEquals(1, (int)model.getObject());
		
	}

	/**
	 * 
	 */
	public void testCloneModelByMappingWithCompoundAndReplace()
	{
		CompoundPropertyModel<CloneObject> compound = new CompoundPropertyModel<CloneObject>(new CloneObject());
		PropertyModel<Integer> model = new PropertyModel<Integer>(compound,"nr");
		model.setObject(1);
		assertEquals(1, (int)model.getObject());
		MiniMap<Field, Object> clonemap = Objects.mapObject(model);
		compound.setObject(new CloneObject());
		model.setObject(10);
		assertEquals(10, (int)model.getObject());
		
		Objects.restoreObject(model,clonemap);
		
		assertEquals(1, (int)model.getObject());
		
	}

	/**
	 * 
	 */
	public void testCloneModelByMappingWithSharedModel()
	{
		CompoundPropertyModel<CloneObject> sharedModel = new CompoundPropertyModel<CloneObject>(new CloneObject());
		PropertyModel<Integer> model = new PropertyModel<Integer>(sharedModel,"nr");
		PropertyModel<Integer> model2 = new PropertyModel<Integer>(sharedModel,"nr");
		model.setObject(1);
		assertEquals(1, (int)model2.getObject());
		MiniMap<Field, Object> clonemap = Objects.mapObject(model);
		model.setObject(10);
		assertEquals(10, (int)model2.getObject());
		
		Objects.restoreObject(model,clonemap);
		
		assertEquals(1, (int)model2.getObject());
		
	}
	
	/**
	 * 
	 */
	public void testCloneSimplePageByMapping()
	{
		SimplePage page = new SimplePage();
		MiniMap<Field, Object> clonemap = Objects.mapObject(page);
		
		assertEquals(true, page.get("myLabel").isVisible());
		page.get("myLabel").setVisible(false);
		assertEquals(false, page.get("myLabel").isVisible());
		Objects.restoreObject(page,clonemap);
		assertEquals(true, page.get("myLabel").isVisible());
		
		page.remove("myLabel");
		
		assertNull(page.get("myLabel"));
		
		Objects.restoreObject(page,clonemap);
		
		assertNotNull(page.get("myLabel"));
		
		page.get("myLabel").add(new SimpleAttributeModifier("test","test"));
		
		assertEquals(1, page.get("myLabel").getBehaviors().size());
		
		Objects.restoreObject(page,clonemap);
		
		assertEquals(0, page.get("myLabel").getBehaviors().size());
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
		
		private final CloneObject2 test = new CloneObject2();

		/**
		 * @see Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object o)
		{
			if(o instanceof CloneObject)
			{
				CloneObject other = (CloneObject)o;
				return other.nr == nr;
			} 
			return false;
		}
	}
	
	/**
	 * Used for testing the clone function.
	 */
	private static final class CloneObject2 implements Serializable
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
			if(o instanceof CloneObject2)
			{
				CloneObject2 other = (CloneObject2)o;
				return other.nr == nr;
			}
			return false;
		}
	}	
	
}

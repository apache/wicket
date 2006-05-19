package wicket.injection;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.easymock.MockControl;

/**
 * Test {@link CompoundFieldValueFactory}
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class CompoundFieldValueFactoryTest extends TestCase
{
	private Integer testField;

	private Field field;

	private MockControl[] ctrl = new MockControl[4];

	private IFieldValueFactory[] fact = new IFieldValueFactory[4];

	protected void setUp() throws Exception
	{
		Field field = CompoundFieldValueFactoryTest.class.getDeclaredField("testField");

		for (int i = 0; i < 4; i++)
		{
			ctrl[i] = MockControl.createControl(IFieldValueFactory.class);
			fact[i] = (IFieldValueFactory) ctrl[i].getMock();
		}
	}

	protected void prepare(int cnt)
	{
		for (int i = 0; i < cnt; i++)
		{
			ctrl[i].expectAndReturn(fact[i].getFieldValue(field, this), null);
			ctrl[i].replay();
		}
	}

	protected void verify(int cnt)
	{
		for (int i = 0; i < cnt; i++)
		{
			ctrl[i].verify();
		}
	}

	/**
	 * Test array constructor
	 */
	public void testArrayConstructor()
	{
		prepare(2);
		CompoundFieldValueFactory f = new CompoundFieldValueFactory(
				new IFieldValueFactory[] {fact[0], fact[1]});
		f.getFieldValue(field, this);
		verify(2);

		try
		{
			f = new CompoundFieldValueFactory((IFieldValueFactory[]) null);
			fail();
		}
		catch (IllegalArgumentException e)
		{
			// noop
		}
	}

	/**
	 * Test list constructor
	 */
	public void testListConstructor()
	{
		prepare(4);
		List list = Arrays.asList(new IFieldValueFactory[] {fact[0], fact[1], fact[2],
				fact[3]});
		CompoundFieldValueFactory f = new CompoundFieldValueFactory(list);
		f.getFieldValue(field, this);
		verify(4);

		try
		{
			f = new CompoundFieldValueFactory((List) null);
			fail();
		}
		catch (IllegalArgumentException e)
		{
			// noop
		}

	}

	/**
	 * Test list constructor
	 */
	public void testABConstructor()
	{
		prepare(2);
		CompoundFieldValueFactory f = new CompoundFieldValueFactory(fact[0], fact[1]);
		f.getFieldValue(field, this);
		verify(2);

		try
		{
			f = new CompoundFieldValueFactory(fact[0], null);
			fail();
		}
		catch (IllegalArgumentException e)
		{
			// noop
		}
		try
		{
			f = new CompoundFieldValueFactory(null, fact[1]);
			fail();
		}
		catch (IllegalArgumentException e)
		{
			// noop
		}

	}

	/**
	 * Test list constructor
	 */
	public void testBreakOnNonNullReturn()
	{
		prepare(2);
		ctrl[2].expectAndReturn(fact[2].getFieldValue(field, this), new Object());
		ctrl[2].replay();
		ctrl[3].replay();
		List list = Arrays.asList(new IFieldValueFactory[] {fact[0], fact[1], fact[2],
				fact[3]});
		CompoundFieldValueFactory f = new CompoundFieldValueFactory(list);

		f.getFieldValue(field, this);

		verify(4);
	}

	/**
	 * Test addFactory()
	 */
	public void testAdd()
	{
		prepare(3);
		CompoundFieldValueFactory f = new CompoundFieldValueFactory(
				new IFieldValueFactory[] {fact[0], fact[1]});
		f.addFactory(fact[2]);
		f.getFieldValue(field, this);
		verify(3);

		try
		{
			f.addFactory(null);
			fail();
		}
		catch (IllegalArgumentException e)
		{
			// noop
		}
	}

}

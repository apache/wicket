package wicket.injection;

import java.lang.reflect.Field;

import junit.framework.TestCase;

/**
 * Test {@link NoopFieldValueFactory}
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class NoopFieldValueFactoryTest extends TestCase
{
	private Integer testField;

	private Field field;

	protected void setUp() throws Exception
	{
		Field field = NoopFieldValueFactoryTest.class.getDeclaredField("testField");
	}

	/**
	 * make sure null is returned
	 */
	public void test()
	{
		NoopFieldValueFactory fact = new NoopFieldValueFactory();
		assertNull(fact.getFieldValue(field, this));
	}

}

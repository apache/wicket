package wicket.extensions.injection;

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
	private Field field;
	
	@Override
	protected void setUp() throws Exception
	{
		field = NoopFieldValueFactoryTest.class.getDeclaredField("field");
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

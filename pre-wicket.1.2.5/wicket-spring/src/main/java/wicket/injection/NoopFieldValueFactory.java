package wicket.injection;

import java.lang.reflect.Field;

/**
 * Implementation of field value factory that ignores all fields
 * 
 * @author Igor Vaynberg (ivaynberg)
 *
 */
public class NoopFieldValueFactory implements IFieldValueFactory
{

	/**
	 * @see wicket.injection.IFieldValueFactory#getFieldValue(java.lang.reflect.Field, java.lang.Object)
	 */
	public Object getFieldValue(Field field, Object fieldOwner)
	{
		return null;
	}

	/**
	 * @see wicket.injection.IFieldValueFactory#supportsField(java.lang.reflect.Field)
	 */
	public boolean supportsField(Field field)
	{
		return false;
	}

}

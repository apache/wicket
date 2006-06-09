package wicket.extensions.injection;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Compound implementation of IFieldValueFactory. This field value factory will
 * keep trying added factories until one returns a non-null value or all are
 * tried.
 * 
 * 
 * @see IFieldValueFactory
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class CompoundFieldValueFactory implements IFieldValueFactory
{
	private List<IFieldValueFactory> delegates = new ArrayList<IFieldValueFactory>();

	/**
	 * Constructor
	 * 
	 * @param factories
	 */
	public CompoundFieldValueFactory(IFieldValueFactory[] factories)
	{
		if (factories == null)
		{
			throw new IllegalArgumentException("argument [factories] cannot be null");
		}

		for (int i = 0; i < factories.length; i++)
		{
			delegates.add(factories[i]);
		}
	}

	/**
	 * Constructor
	 * 
	 * @param factories
	 */
	public CompoundFieldValueFactory(List<IFieldValueFactory> factories)
	{
		if (factories == null)
		{
			throw new IllegalArgumentException("argument [factories] cannot be null");
		}
		delegates.addAll(factories);
	}

	/**
	 * Constructor
	 * 
	 * @param f1
	 * @param f2
	 */
	public CompoundFieldValueFactory(IFieldValueFactory f1, IFieldValueFactory f2)
	{
		if (f1 == null)
		{
			throw new IllegalArgumentException("argument [f1] cannot be null");
		}
		if (f2 == null)
		{
			throw new IllegalArgumentException("argument [f2] cannot be null");
		}
		delegates.add(f1);
		delegates.add(f2);
	}

	/**
	 * Adds a factory to the compound factory
	 * 
	 * @param factory
	 */
	public void addFactory(IFieldValueFactory factory)
	{
		if (factory == null)
		{
			throw new IllegalArgumentException("argument [factory] cannot be null");
		}
		delegates.add(factory);
	}

	/**
	 * @see wicket.extensions.injection.IFieldValueFactory#getFieldValue(java.lang.reflect.Field,
	 *      java.lang.Object)
	 */
	public Object getFieldValue(Field field, Object fieldOwner)
	{
		Iterator<IFieldValueFactory> it = delegates.iterator();
		while (it.hasNext())
		{
			final IFieldValueFactory factory = it.next();
			Object object = factory.getFieldValue(field, fieldOwner);
			if (object != null)
			{
				return object;
			}
		}
		return null;
	}

	/**
	 * @see wicket.extensions.injection.IFieldValueFactory#supportsField(java.lang.reflect.Field)
	 */
	public boolean supportsField(Field field)
	{
		Iterator<IFieldValueFactory> it = delegates.iterator();
		while (it.hasNext())
		{
			final IFieldValueFactory factory = it.next();
			if (factory.supportsField(field)) {
				return true;
			}
		}
		return false;
	}

}

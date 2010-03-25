package org.apache.wicket.util.visit;

/**
 * {@link IVisitFilter} that restricts visitors to only visiting objects of the
 * specified class
 * 
 * @author igor.vaynberg
 */
public class ClassVisitFilter implements IVisitFilter
{
	private final Class<?> clazz;

	/**
	 * Constructor
	 * 
	 * @param clazz
	 *            class of objects that visitors should be restricted to
	 */
	public ClassVisitFilter(Class<?> clazz)
	{
		this.clazz = clazz;
	}

	/** {@inheritDoc} */
	public boolean visitChildren(Object object)
	{
		return true;
	}

	/** {@inheritDoc} */
	public boolean visitObject(Object object)
	{
		return clazz.isAssignableFrom(object.getClass());
	}
}

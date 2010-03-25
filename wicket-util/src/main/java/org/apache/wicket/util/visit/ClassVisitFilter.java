package org.apache.wicket.util.visit;

public class ClassVisitFilter implements IVisitFilter
{
	private final Class<?> clazz;

	public ClassVisitFilter(Class<?> clazz)
	{
		this.clazz = clazz;
	}

	public boolean visitChildren(Object object)
	{
		return true;
	}

	public boolean visitObject(Object object)
	{
		return clazz.isAssignableFrom(object.getClass());
	}
}

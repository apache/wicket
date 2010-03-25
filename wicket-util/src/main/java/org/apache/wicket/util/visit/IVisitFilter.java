package org.apache.wicket.util.visit;

public interface IVisitFilter
{
	boolean visitObject(Object object);

	boolean visitChildren(Object object);

	public static IVisitFilter ANY = new IVisitFilter()
	{
		public boolean visitObject(Object object)
		{
			return true;
		}

		public boolean visitChildren(Object object)
		{
			return true;
		}
	};
	
}

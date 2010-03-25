package org.apache.wicket.util.visit;

/**
 * A filter that can be used to restrict the types of objects visited by the
 * visitor
 * 
 * @author igor.vaynberg
 */
public interface IVisitFilter
{
	/**
	 * Controls whether or not the {@code object} will be visited
	 * 
	 * @param object
	 * @return {@code true} if the object should be visited
	 */
	boolean visitObject(Object object);

	/**
	 * Controls whether or not the {@code object}'s children will be visited
	 * 
	 * @param object
	 * @return {@code true} if the object's children should be visited
	 */
	boolean visitChildren(Object object);

	/**
	 * A visitor filter that allows all objects and their children to be visited
	 */
	public static IVisitFilter ANY = new IVisitFilter()
	{
		/** {@inheritDoc} */
		public boolean visitObject(Object object)
		{
			return true;
		}

		/** {@inheritDoc} */
		public boolean visitChildren(Object object)
		{
			return true;
		}
	};

}

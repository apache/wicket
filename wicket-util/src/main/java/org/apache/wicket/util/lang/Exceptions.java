package org.apache.wicket.util.lang;

public class Exceptions
{
	private Exceptions()
	{

	}

	public static <T extends Throwable> T findCause(Throwable throwable, final Class<T> causeType)
	{
		return visit(throwable, new IThrowableVisitor<T>()
		{
			@SuppressWarnings("unchecked")
			public void visit(Throwable throwable, Traversal<T> traversal)
			{
				if (causeType.isAssignableFrom(throwable.getClass()))
				{
					traversal.stop((T)throwable);
				}

			}
		});
	}

	// TODO Component$IVisitor should utilize a similar object, much cleaner than magic return values
	public static class Traversal<T>
	{
		private T result;
		private boolean stopped;

		public void stop(T result)
		{
			this.result = result;
			stop();
		}

		public void stop()
		{
			stopped = true;
		}
	}

	public static interface IThrowableVisitor<T>
	{
		void visit(Throwable throwable, Traversal<T> traversal);
	}

	public static <T> T visit(Throwable throwable, IThrowableVisitor<T> visitor)
	{
		Traversal<T> traversal = new Traversal<T>();
		Throwable cursor = throwable;
		while (cursor != null)
		{
			visitor.visit(cursor, traversal);
			if (traversal.stopped)
			{
				return traversal.result;
			}
			cursor = cursor.getCause();
		}
		return null;
	}
}

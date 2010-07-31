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
			public void visit(Throwable throwable, Visit<T> visit)
			{
				if (causeType.isAssignableFrom(throwable.getClass()))
				{
					visit.stop((T)throwable);
				}

			}
		});
	}

	// TODO Component$IVisitor should utilize a similar object, much cleaner than magic return
	// values
	public static class Visit<T>
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
		void visit(Throwable throwable, Visit<T> visit);
	}

	public static <T> T visit(Throwable throwable, IThrowableVisitor<T> visitor)
	{
		Visit<T> visit = new Visit<T>();
		Throwable cursor = throwable;
		while (cursor != null)
		{
			visitor.visit(cursor, visit);
			if (visit.stopped)
			{
				return visit.result;
			}
			cursor = cursor.getCause();
		}
		return null;
	}
}

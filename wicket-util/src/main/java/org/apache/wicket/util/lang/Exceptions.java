/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.util.lang;

/**
 * 
 * @author igor.vaynberg
 */
public class Exceptions
{
	private Exceptions()
	{
	}

	/**
	 * Gets root cause of the throwable
	 * 
	 * @param throwable
	 * @return root cause
	 */
	public Throwable getRootCause(final Throwable throwable)
	{
		Throwable cursor = throwable;
		while (cursor.getCause() != null)
		{
			cursor = cursor.getCause();
		}
		return cursor;
	}

	/**
	 * Looks for a cause of the specified type in throwable's chain
	 * 
	 * @param <T>
	 * @param throwable
	 * @param causeType
	 * @return matched {@link Throwable} in the chain or {@code null} if none
	 */
	public static <T extends Throwable> T findCause(final Throwable throwable,
		final Class<T> causeType)
	{
		return visit(throwable, new IThrowableVisitor<T>()
		{
			@Override
			@SuppressWarnings("unchecked")
			public void visit(final Throwable throwable, final Visit<T> visit)
			{
				if (causeType.isAssignableFrom(throwable.getClass()))
				{
					visit.stop((T)throwable);
				}

			}
		});
	}

	/**
	 * Represents a visit
	 * 
	 * @author igor
	 * @param <T>
	 */
	public static class Visit<T>
	{
		private T result;
		private boolean stopped;

		/**
		 * Stops visit with specified resut
		 * 
		 * @param result
		 */
		public void stop(final T result)
		{
			this.result = result;
			stop();
		}

		/**
		 * Stops visit
		 */
		public void stop()
		{
			stopped = true;
		}
	}

	/**
	 * Visitor used to visit {@link Throwable} chains
	 * 
	 * @param <T>
	 */
	public static interface IThrowableVisitor<T>
	{
		/**
		 * Visit a throwable
		 * 
		 * @param throwable
		 * @param visit
		 */
		void visit(Throwable throwable, Visit<T> visit);
	}

	/**
	 * Visits the {@link Throwable}'s chain
	 * 
	 * @param <T>
	 * @param throwable
	 * @param visitor
	 * @return result set on visitor or {@code null} if none
	 */
	public static <T> T visit(final Throwable throwable, final IThrowableVisitor<T> visitor)
	{
		Visit<T> visit = new Visit<>();
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

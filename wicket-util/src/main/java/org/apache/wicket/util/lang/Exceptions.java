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
	 * 
	 * @param <T>
	 * @param throwable
	 * @param causeType
	 * @return
	 */
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
		public void stop(T result)
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
	 * 
	 * @param <T>
	 */
	public static interface IThrowableVisitor<T>
	{
		void visit(Throwable throwable, Visit<T> visit);
	}

	/**
	 * 
	 * @param <T>
	 * @param throwable
	 * @param visitor
	 * @return
	 */
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

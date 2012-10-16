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
package org.apache.wicket.util.io;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.Serializable;
import java.lang.reflect.Proxy;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.util.objects.checker.AbstractObjectChecker;
import org.apache.wicket.util.objects.checker.ObjectChecker;


/**
 * Utility class that analyzes objects for non-serializable nodes. Construct, then call
 * {@link #writeObject(Object)} with the object you want to check. When a non-serializable object is
 * found, a {@link WicketNotSerializableException} is thrown with a message that shows the trace up
 * to the not-serializable object. The exception is thrown for the first non-serializable instance
 * it encounters, so multiple problems will not be shown.
 *
 * @author eelcohillenius
 * @author Al Maw
 */
public class SerializableChecker extends ObjectChecker
{
	/**
	 * Exception that is thrown when a non-serializable object was found.
	 * @deprecated ObjectCheckException is thrown instead
	 */
	@Deprecated
	public static final class WicketNotSerializableException extends WicketRuntimeException
	{
		private static final long serialVersionUID = 1L;

		private WicketNotSerializableException(String message, Throwable cause)
		{
			super(message, cause);
		}
	}

	/**
	 * An implementation of IObjectChecker that checks whether the object
	 * implements {@link Serializable} interface
	 */
	public static class ObjectSerializationChecker extends AbstractObjectChecker
	{
		/** Exception that should be set as the cause when throwing a new exception. */
		private final NotSerializableException cause;

		/**
		 * A constructor to use when the checker is used before a previous attempt to
		 * serialize the object.
		 */
		public ObjectSerializationChecker()
		{
			this(null);
		}

		/**
		 * A constructor to use when there was a previous attempt to serialize the
		 * object and it failed with the {@code cause}.
		 *
		 * @param cause
		 *      the cause of the serialization failure in a previous attempt.
		 */
		public ObjectSerializationChecker(NotSerializableException cause)
		{
			this.cause = cause;
		}

		/**
		 * Makes the check for all objects. Exclusions by type is not supported.
		 * @param object
		 *      the object to check
		 * @return the {@link Result#SUCCESS} if the object can be serialized.
		 */
		@Override
		public Result check(Object object)
		{
			Result result = Result.SUCCESS;
			if (!(object instanceof Serializable) && (!Proxy.isProxyClass(object.getClass())))
			{
				result = new Result(Result.Status.FAILURE, "The object type is not Serializable!", cause);
			}

			return result;
		}
	}

	/**
	 * Constructor.
	 *
	 * @param exception
	 *            exception that should be set as the cause when throwing a new exception
	 *
	 * @throws IOException
	 */
	public SerializableChecker(NotSerializableException exception) throws IOException
	{
		super(new ObjectSerializationChecker(exception));
	}

	/**
	 * Delegate to preserve binary compatibility.
	 *
	 * @return {@code true} if the checker can be used
	 * @deprecated Use ObjectChecker#isAvailable() instead
	 */
	// TODO Wicket 7.0 - remove this method
	@Deprecated
	public static boolean isAvailable()
	{
		return ObjectChecker.isAvailable();
	}
}
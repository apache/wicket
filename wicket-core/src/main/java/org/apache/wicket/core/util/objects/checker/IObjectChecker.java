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
package org.apache.wicket.core.util.objects.checker;

import java.util.List;

import org.apache.wicket.util.lang.Args;

/**
 * IObjectChecker can be used to check whether an object has/has not given state
 * before serializing it. The serialization will be stopped if the object doesn't pass
 * the {@code #check(Object) check}.
 */
public interface IObjectChecker
{
	/**
	 * Represents the result of a check.
	 */
	public static class Result
	{
		public static enum Status
		{
			/**
			 * The check is successful
			 */
			SUCCESS,

			/**
			 * The check failed for some reason
			 */
			FAILURE
		}

		/**
		 * A singleton that can be used for successful checks
		 */
		public static final Result SUCCESS = new Result(Status.SUCCESS, "");

		/**
		 * The status of the check.
		 */
		public final Status status;

		/**
		 * The reason why a check succeeded/failed. Mandatory in failure case.
		 */
		public final String reason;

		/**
		 * An optional cause of a failure.
		 */
		public final Throwable cause;

		/**
		 * Constructor.
		 *
		 * @param status
		 *      the status of the result
		 * @param reason
		 *      the reason of successful/failed check
		 */
		public Result(Status status, String reason)
		{
			this(status, reason, null);
		}


		/**
		 * Constructor.
		 *
		 * @param status
		 *      the status of the result
		 * @param reason
		 *      the reason of successful/failed check
		 * @param cause
		 *      the cause of a failure. Optional.
		 */
		public Result(Status status, String reason, Throwable cause)
		{
			if (status == Status.FAILURE)
			{
				Args.notEmpty(reason, "reason");
			}
			this.status = status;
			this.reason = reason;
			this.cause = cause;
		}

		@Override
		public String toString()
		{
			return "Result{" +
					"reason='" + reason + '\'' +
					", status=" + status +
					'}';
		}
	}

	/**
	 * Checks an object that it meets some requirements before serializing it
	 *
	 * @param object
	 *      the object to check
	 * @return a Result object describing whether the check is successful or not
	 */
	Result check(Object object);

	/**
	 * @return A list of types which should not be checked by this checker
	 */
	List<Class<?>> getExclusions();
}

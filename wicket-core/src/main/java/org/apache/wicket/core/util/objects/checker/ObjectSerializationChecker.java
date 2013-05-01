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

import java.io.NotSerializableException;
import java.io.Serializable;
import java.lang.reflect.Proxy;

/**
 * An implementation of IObjectChecker that checks whether the object
 * implements {@link java.io.Serializable} interface
 */
public class ObjectSerializationChecker extends AbstractObjectChecker
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
	 * @return the {@link org.apache.wicket.core.util.objects.checker.IObjectChecker.Result#SUCCESS} if the object can be serialized.
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
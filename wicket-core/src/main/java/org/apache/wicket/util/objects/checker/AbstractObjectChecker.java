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
package org.apache.wicket.util.objects.checker;

import java.util.List;

import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Generics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A base class for IObjectChecker implementations which handles the logic
 * for checking type exclusions.
 */
public abstract class AbstractObjectChecker implements IObjectChecker
{
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractObjectChecker.class);

	private final List<Class<?>> exclusions;

	protected AbstractObjectChecker()
	{
		this(Generics.<Class<?>>newArrayList());
	}

	protected AbstractObjectChecker(List<Class<?>> exclusions)
	{
		this.exclusions = Args.notNull(exclusions, "exclusions");
	}

	public Result check(Object object)
	{
		Result result = Result.SUCCESS;

		if (object != null && getExclusions().isEmpty() == false)
		{
			Class<?> objectType = object.getClass();
			for (Class<?> excludedType : getExclusions())
			{
				if (excludedType.isAssignableFrom(objectType))
				{
					LOGGER.debug("Object with type '{}' wont be checked because its type is excluded ({})",
							objectType, excludedType);
					return result;
				}
			}
		}

		result = doCheck(object);

		return result;
	}

	/**
	 * The implementations should make the specific check on the object.
	 * @param object
	 *      the object to check
	 * @return the {@link Result result} of the specific check
	 */
	protected Result doCheck(Object object)
	{
		return Result.SUCCESS;
	}

	public List<Class<?>> getExclusions()
	{
		return exclusions;
	}
}

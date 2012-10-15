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

import org.apache.wicket.model.LoadableDetachableModel;

/**
 * An implementation of {@link IObjectChecker} that returns a failure
 * result when the checked object is a {@link LoadableDetachableModel}
 * and it is model object is still attached.
 */
public class NotDetachedModelChecker extends AbstractObjectChecker
{
	/**
	 * Constructor.
	 *
	 * Checks all passed objects.
	 */
	public NotDetachedModelChecker()
	{
		super();
	}

	/**
	 * Constructor.
	 *
	 * Checks objects which types are not excluded.
	 *
	 * @param exclusions
	 *      a list of types which should not be checked
	 */
	public NotDetachedModelChecker(List<Class<?>> exclusions)
	{
		super(exclusions);
	}

	@Override
	public Result doCheck(Object obj)
	{
		Result result = Result.SUCCESS;

		if (obj instanceof LoadableDetachableModel<?>)
		{
			LoadableDetachableModel<?> model = (LoadableDetachableModel<?>) obj;
			if (model.isAttached())
			{
				result = new Result(Result.Status.FAILURE, "Not detached model found!");
			}
		}

		return result;
	}
}

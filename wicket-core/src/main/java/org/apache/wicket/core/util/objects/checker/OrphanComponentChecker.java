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

import org.apache.wicket.Component;
import org.apache.wicket.Page;

/**
 * A checker that doesn't allow the serialization of {@link Component component}s
 * which are not a {@link Page page} and have no parent component.
 *
 * <p>
 *     Note: The Wizard component from wicket-extensions use such kind of orphaned components
 *     and will fail this check unless the step classes are specified as exclusions.
 * </p>
 */
public class OrphanComponentChecker extends AbstractObjectChecker
{
	/**
	 * Constructor.
	 *
	 * Checks all passed objects.
	 */
	public OrphanComponentChecker()
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
	public OrphanComponentChecker(List<Class<?>> exclusions)
	{
		super(exclusions);
	}

	@Override
	public Result doCheck(Object object)
	{
		Result result = Result.SUCCESS;

		if (object instanceof Component)
		{
			Component component = (Component) object;
			if (component instanceof Page == false && component.getParent() == null)
			{
				result = new Result(Result.Status.FAILURE, "A component without a parent is detected.");
			}
		}

		return result;
	}
}

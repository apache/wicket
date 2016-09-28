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

import org.apache.wicket.Page;

/**
 * An implementation of {@link IObjectChecker} that returns a failure
 * result when the checked object is a second {@link org.apache.wicket.Page} instance
 * in the component tree. The first checked page instance is assumed to be the component
 * tree root. Any other page instance checked by the same checker is a referenced page
 * by a component in the tree.
 *
 * @since 6.17.0
 */
public class DifferentPageChecker extends AbstractObjectChecker
{
	private Page root;

	@Override
	public Result doCheck(Object obj)
	{
		Result result = Result.SUCCESS;

		if (obj instanceof Page)
		{
			if (root != null && root != obj)
			{
				result = new Result(Result.Status.FAILURE, "Trying to serialize a page which is not the component tree root!");
			}
			else
			{
				root = (Page) obj;
			}
		}

		return result;
	}
}

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

import org.apache.wicket.Session;

/**
 * An implementation of {@link org.apache.wicket.core.util.objects.checker.IObjectChecker} that returns a failure
 * result when the checked object is a {@link org.apache.wicket.Session}.
 * Keeping a reference to the session instance in the component tree is an error.
 *
 * @since 6.17.0
 */
public class SessionChecker extends AbstractObjectChecker
{
	@Override
	public Result doCheck(Object obj)
	{
		Result result = Result.SUCCESS;

		if (obj instanceof Session)
		{
			result = new Result(Result.Status.FAILURE, "Trying to serialize the Wicket Session!");
		}

		return result;
	}
}

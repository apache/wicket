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
package org.apache.wicket.core.util.lang;

import org.apache.wicket.Application;

/**
 * Old {@link PropertyResolver} kept just as a facade for the current implementation
 */
@Deprecated
public final class PropertyResolver
{

	public static <T> T getValue(String expression, T object)
	{
		return Application.get().getApplicationSettings().getPropertyExpressionResolver()
			.getValue(expression, object);
	}

	public static <T> Class<T> getPropertyClass(String expression, Object object,
		Class<?> targetClass)
	{
		return Application.get().getApplicationSettings().getPropertyExpressionResolver()
			.getPropertyClass(expression, object, targetClass);
	}

	public static void setValue(String expression, Object object, Object value,
		PropertyResolverConverter prc)
	{
		Application.get().getApplicationSettings().getPropertyExpressionResolver()
			.setValue(expression, object, value, prc);
	}

}
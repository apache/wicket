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
package wicket.util.lang.reflect;

import java.lang.reflect.Method;
import java.util.Collection;


/**
 * Filter that can be used to cecide whether or not a method should be added to
 * a collection of other methods
 * 
 * @author ivaynberg
 */
public interface IMethodFilter
{
	/**
	 * @param check
	 *            method that needs to be decided upon
	 * @param accepted
	 *            collection of already added methods
	 * @return true if the method should be added, false otherwise
	 */
	boolean accept(Method check, Collection<Method> accepted);

	/**
	 * Filter that allows any method to be added
	 */
	public static final IMethodFilter ANY = new IMethodFilter()
	{

		/**
		 * @see wicket.util.lang.reflect.IMethodFilter#accept(java.lang.reflect.Method,
		 *      java.util.Collection)
		 */
		public boolean accept(Method check, Collection<Method> accepted)
		{
			return true;
		}

	};

	/**
	 * Filter that allows a method to be added if and only if a representative
	 * of its override chain is not already in the collection.
	 * 
	 * This makes it easy to have only a single representative on an invocation
	 * chain in the list so when the collection of methods is invoked one by one
	 * an overridden method is only called once.
	 */
	public static final IMethodFilter IGNORE_OVERRIDES = new IMethodFilter()
	{
		/**
		 * @see wicket.util.lang.reflect.IMethodFilter#accept(java.lang.reflect.Method,
		 *      java.util.Collection)
		 */
		public boolean accept(Method check, Collection<Method> accepted)
		{
			for (Method method : accepted)
			{
				if (ReflectionUtils.overrides(method, check))
				{
					return false;
				}
			}
			return true;
		}
	};
}

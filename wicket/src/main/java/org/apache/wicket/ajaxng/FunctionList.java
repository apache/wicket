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
package org.apache.wicket.ajaxng;

import java.util.Collection;
import java.util.List;

/**
 * Chaining list of javascript functions. Function can by any object that renders a javascript
 * function (in the form of "function(arg, ...) { ... }") on {@link #toString()}.
 * 
 * @author Matej Knopp
 */
public class FunctionList extends ChainingList<Object>
{
	/**
	 * Construct.
	 */
	public FunctionList()
	{
		super();
	}

	/**
	 * Construct.

	 * @param list
	 */
	public FunctionList(List<Object> list)
	{
		super(list);
	}

	@Override
	public FunctionList add(int index, Object element)
	{
		return (FunctionList)super.add(index, element);
	}

	@Override
	public FunctionList add(Object o)
	{
		return (FunctionList)super.add(o);
	}

	@Override
	public FunctionList addAll(Collection<? extends Object> c)
	{
		return (FunctionList)super.addAll(c);
	}

	@Override
	public FunctionList addAll(int index, Collection<? extends Object> c)
	{
		return (FunctionList)super.addAll(index, c);
	}

	@Override
	public FunctionList clear()
	{
		return (FunctionList)super.clear();
	}

	@Override
	public FunctionList remove(int index)
	{
		return (FunctionList)super.remove(index);
	}

	@Override
	public FunctionList remove(Object o)
	{
		return (FunctionList)super.remove(o);
	}

	@Override
	public FunctionList removeAll(Collection<?> c)
	{
		return (FunctionList)super.removeAll(c);
	}

	@Override
	public FunctionList set(int index, Object element)
	{
		return (FunctionList)super.set(index, element);
	}
}

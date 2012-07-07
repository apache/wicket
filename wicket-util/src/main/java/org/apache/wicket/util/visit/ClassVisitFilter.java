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
package org.apache.wicket.util.visit;

import org.apache.wicket.util.lang.Args;

/**
 * {@link IVisitFilter} that restricts visitors to only visiting objects of the specified class
 * 
 * @author igor.vaynberg
 */
public class ClassVisitFilter implements IVisitFilter
{
	private final Class<?> clazz;

	/**
	 * Constructor
	 * 
	 * @param clazz
	 *            class of objects that visitors should be restricted to
	 */
	public ClassVisitFilter(final Class<?> clazz)
	{
		Args.notNull(clazz, "clazz");

		this.clazz = clazz;
	}

	/** {@inheritDoc} */
	@Override
	public boolean visitChildren(final Object object)
	{
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public boolean visitObject(final Object object)
	{
		return clazz.isAssignableFrom(object.getClass());
	}
}

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
package org.apache.wicket.injection;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.util.lang.Args;

/**
 * Compound implementation of IFieldValueFactory. This field value factory will keep trying added
 * factories until one returns a non-null value or all are tried.
 * 
 * 
 * @see IFieldValueFactory
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class CompoundFieldValueFactory implements IFieldValueFactory
{
	private final List<IFieldValueFactory> delegates = new ArrayList<IFieldValueFactory>();

	/**
	 * Constructor
	 * 
	 * @param factories
	 */
	public CompoundFieldValueFactory(final IFieldValueFactory[] factories)
	{
		Args.notNull(factories, "factories");

		delegates.addAll(Arrays.asList(factories));
	}

	/**
	 * Constructor
	 * 
	 * @param factories
	 */
	public CompoundFieldValueFactory(final List<IFieldValueFactory> factories)
	{
		Args.notNull(factories, "factories");

		delegates.addAll(factories);
	}

	/**
	 * Constructor
	 * 
	 * @param f1
	 * @param f2
	 */
	public CompoundFieldValueFactory(final IFieldValueFactory f1, final IFieldValueFactory f2)
	{
		Args.notNull(f1, "f1");
		Args.notNull(f2, "f2");

		delegates.add(f1);
		delegates.add(f2);
	}

	/**
	 * Adds a factory to the compound factory
	 * 
	 * @param factory
	 */
	public void addFactory(final IFieldValueFactory factory)
	{
		Args.notNull(factory, "factory");

		delegates.add(factory);
	}

	/**
	 * @see org.apache.wicket.injection.IFieldValueFactory#getFieldValue(java.lang.reflect.Field,
	 *      java.lang.Object)
	 */
	@Override
	public Object getFieldValue(final Field field, final Object fieldOwner)
	{
		for (IFieldValueFactory factory : delegates)
		{
			Object object = factory.getFieldValue(field, fieldOwner);
			if (object != null)
			{
				return object;
			}
		}
		return null;
	}

	/**
	 * @see org.apache.wicket.injection.IFieldValueFactory#supportsField(java.lang.reflect.Field)
	 */
	@Override
	public boolean supportsField(final Field field)
	{
		for (IFieldValueFactory factory : delegates)
		{
			if (factory.supportsField(field))
			{
				return true;
			}
		}
		return false;
	}

}

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
package wicket.injection;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Compound implementation of IFieldValueFactory. This field value factory will
 * keep trying added factories until one returns a non-null value or all are
 * tried.
 * 
 * 
 * @see IFieldValueFactory
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class CompoundFieldValueFactory implements IFieldValueFactory
{
	private List delegates = new ArrayList();

	/**
	 * Constructor
	 * 
	 * @param factories
	 */
	public CompoundFieldValueFactory(IFieldValueFactory[] factories)
	{
		if (factories == null)
		{
			throw new IllegalArgumentException("argument [factories] cannot be null");
		}

		for (int i = 0; i < factories.length; i++)
		{
			delegates.add(factories[i]);
		}
	}

	/**
	 * Constructor
	 * 
	 * @param factories
	 */
	public CompoundFieldValueFactory(List factories)
	{
		if (factories == null)
		{
			throw new IllegalArgumentException("argument [factories] cannot be null");
		}
		delegates.addAll(factories);
	}

	/**
	 * Constructor
	 * 
	 * @param f1
	 * @param f2
	 */
	public CompoundFieldValueFactory(IFieldValueFactory f1, IFieldValueFactory f2)
	{
		if (f1 == null)
		{
			throw new IllegalArgumentException("argument [f1] cannot be null");
		}
		if (f2 == null)
		{
			throw new IllegalArgumentException("argument [f2] cannot be null");
		}
		delegates.add(f1);
		delegates.add(f2);
	}

	/**
	 * Adds a factory to the compound factory
	 * 
	 * @param factory
	 */
	public void addFactory(IFieldValueFactory factory)
	{
		if (factory == null)
		{
			throw new IllegalArgumentException("argument [factory] cannot be null");
		}
		delegates.add(factory);
	}

	/**
	 * @see wicket.injection.IFieldValueFactory#getFieldValue(java.lang.reflect.Field,
	 *      java.lang.Object)
	 */
	public Object getFieldValue(Field field, Object fieldOwner)
	{
		Iterator it = delegates.iterator();
		while (it.hasNext())
		{
			final IFieldValueFactory factory = (IFieldValueFactory) it.next();
			Object object = factory.getFieldValue(field, fieldOwner);
			if (object != null)
			{
				return object;
			}
		}
		return null;
	}

	/**
	 * @see wicket.injection.IFieldValueFactory#supportsField(java.lang.reflect.Field)
	 */
	public boolean supportsField(Field field)
	{
		Iterator it = delegates.iterator();
		while (it.hasNext())
		{
			final IFieldValueFactory factory = (IFieldValueFactory) it.next();
			if (factory.supportsField(field)) {
				return true;
			}
		}
		return false;
	}

}

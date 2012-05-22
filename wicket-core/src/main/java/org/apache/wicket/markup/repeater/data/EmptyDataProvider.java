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
package org.apache.wicket.markup.repeater.data;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.model.IModel;


/**
 * A convenience class to represent an empty data provider.
 * 
 * @author Phil Kulak
 * @param <T>
 */
public class EmptyDataProvider<T> implements IDataProvider<T>
{
	private static final long serialVersionUID = 1L;

	private static EmptyDataProvider<?> INSTANCE = new EmptyDataProvider<Void>();

	/**
	 * @param <T>
	 * @return the singleton instance of this class
	 */
	@SuppressWarnings("unchecked")
	public static <T> EmptyDataProvider<T> getInstance()
	{
		return (EmptyDataProvider<T>)INSTANCE;
	}

	/**
	 * @see IDataProvider#iterator(long, long)
	 */
	@Override
	public Iterator<T> iterator(long first, long count)
	{
		List<T> list = Collections.emptyList();
		return list.iterator();
	}

	/**
	 * @see IDataProvider#size()
	 */
	@Override
	public long size()
	{
		return 0;
	}

	/**
	 * @see IDataProvider#model(Object)
	 */
	@Override
	public IModel<T> model(Object object)
	{
		return null;
	}

	/**
	 * @see org.apache.wicket.model.IDetachable#detach()
	 */
	@Override
	public void detach()
	{
	}
}

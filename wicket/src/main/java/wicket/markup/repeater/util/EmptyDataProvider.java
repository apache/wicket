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
package wicket.markup.repeater.util;

import java.util.Collections;
import java.util.Iterator;

import wicket.markup.repeater.data.IDataProvider;
import wicket.model.IModel;

/**
 * A convienience class to represent an empty data provider.
 * 
 * @author Phil Kulak
 */
public class EmptyDataProvider implements IDataProvider
{
	private static final long serialVersionUID = 1L;

	private static EmptyDataProvider INSTANCE = new EmptyDataProvider();

	/**
	 * @return the singleton instance of this class
	 */
	public static EmptyDataProvider getInstance()
	{
		return INSTANCE;
	}

	/**
	 * @see wicket.markup.repeater.data.IDataProvider#iterator(int,
	 *      int)
	 */
	public Iterator iterator(int first, int count)
	{
		return Collections.EMPTY_LIST.iterator();
	}

	/**
	 * @see wicket.markup.repeater.data.IDataProvider#size()
	 */
	public int size()
	{
		return 0;
	}

	/**
	 * @see wicket.markup.repeater.data.IDataProvider#model(java.lang.Object)
	 */
	public IModel model(Object object)
	{
		return null;
	}

	/**
	 * @see wicket.model.IDetachable#detach()
	 */
	public void detach()
	{
	}
}

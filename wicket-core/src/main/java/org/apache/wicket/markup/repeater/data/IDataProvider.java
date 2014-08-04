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

import java.util.Iterator;

import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;


/**
 * Interface used to provide data to data views.
 * 
 * Example:
 * 
 * <pre>
 * class UsersProvider implements IDataProvider
 * {
 * 
 * 	public Iterator iterator(long first, long count)
 * 	{
 * 		((MyApplication)Application.get()).getUserDao().iterator(first, count);
 * 	}
 * 
 * 	public long size()
 * 	{
 * 		((MyApplication)Application.get()).getUserDao().getCount();
 * 	}
 * 
 * 	public IModel model(Object object)
 * 	{
 * 		return new DetachableUserModel((User)object);
 * 	}
 * }
 * </pre>
 * 
 * You can use the {@link IDetachable#detach()} method for cleaning up your IDataProvider instance.
 * So that you can do one query that returns both the size and the values if your dataset is small
 * enough the be able to do that.
 * 
 * @see IDetachable
 * @see DataViewBase
 * @see DataView
 * @see GridView
 * 
 * @author Igor Vaynberg (ivaynberg)
 * @param <T>
 * 
 */
public interface IDataProvider<T> extends IDetachable
{
	/**
	 * Gets an iterator for the subset of total data
	 * 
	 * @param first
	 *            first row of data
	 * @param count
	 *            minimum number of elements to retrieve
	 * 
	 * @return iterator capable of iterating over {first, first+count} items
	 */
	Iterator<? extends T> iterator(long first, long count);

	/**
	 * Gets total number of items in the collection represented by the DataProvider
	 * 
	 * @return total item count
	 */
	long size();

	/**
	 * Callback used by the consumer of this data provider to wrap objects retrieved from
	 * {@link #iterator(long, long)} with a model (usually a detachable one).
	 * 
	 * @param object
	 *            the object that needs to be wrapped
	 * 
	 * @return the model representation of the object
	 */
	IModel<T> model(T object);

}

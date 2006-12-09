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
package wicket.extensions.markup.html.repeater.data;

import java.util.Iterator;

import wicket.model.IDetachable;
import wicket.model.IModel;

/**
 * Interface used to provide data to data views.
 * 
 * Example:
 * 
 * <pre>
 *         class UsersProvider implements IDataProvider<User>() {
 *           
 *           Iterator<User> iterator(int first, int count) {
 *             MyApplication.get().getUserDao().iterator(first, count);
 *           }
 *           
 *           int size() {
 *             MyApplication.get().getUserDao().getCount();
 *           }
 *           
 *           IModel<User> model(User user) {
 *             return new DetachableUserModel<User>(user);
 *           }
 *         }
 * </pre>
 * 
 * You can use the {@link IDetachable#detach()} method for cleaning up your
 * IDataProvider instance. So that you can do one query that returns both
 * the size and the values if your dataset is small enough the be able to
 * do that.
 * 
 * @see IDetachable
 * @see DataViewBase
 * @see DataView
 * @see GridView
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 * @param <T> The IModel type
 */
public interface IDataProvider<T> extends IDetachable
{
	/**
	 * Gets an iterator for the subset of total data
	 * 
	 * @param first
	 *            first row of data
	 * @param count
	 *            minumum number of elements to retrieve
	 * 
	 * @return iterator capable of iterating over {first, first+count} items
	 */
	Iterator<T> iterator(int first, int count);

	/**
	 * Gets total number of items in the collection represented by the
	 * DataProvider
	 * 
	 * @return total item count
	 */
	int size();

	/**
	 * Callback used by the consumer of this data provider to wrap objects
	 * retrieved from {@link #iterator(int, int)} with a model (usually a
	 * detachable one).
	 * 
	 * @param object
	 *            the object that needs to be wrapped
	 * 
	 * @return the model representation of the object
	 */
	IModel<T> model(T object);

}

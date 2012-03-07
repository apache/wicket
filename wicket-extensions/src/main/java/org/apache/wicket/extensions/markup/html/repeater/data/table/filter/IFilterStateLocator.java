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
package org.apache.wicket.extensions.markup.html.repeater.data.table.filter;

import org.apache.wicket.util.io.IClusterable;

/**
 * Locator that locates the object that represents the state of the filter. Usually it is convenient
 * to let the data provider object implement this interface so that the data provider can be itself
 * used to locate the filter state object. This also makes it easy for the data provider to locate
 * the filter state which it will most likely need to filter the data.
 * <p>
 * Example
 * 
 * <pre>
 *    class UserDataProvider extends SortableDataProvider implements IFilterStateLocator<User> {
 *      private User filterBean=new User;
 *      
 *      public User getFilterState() { return filterBean; }
 *      public void setFilterState(User user) { filterBean=user; }
 *      
 *      public Iterator iterate(int start, int count) {
 *        getUserDao().find(start, count, filterBean);
 *      }
 *    }
 * </pre>
 * 
 * @param <T>
 *            type of filter state object
 * @author igor
 * 
 */
public interface IFilterStateLocator<T> extends IClusterable
{
	/**
	 * @return object that represents the state of the filter toolbar
	 */
	T getFilterState();

	/**
	 * Setter for the filter state object
	 * 
	 * @param state
	 *            filter state object
	 */
	void setFilterState(T state);
}

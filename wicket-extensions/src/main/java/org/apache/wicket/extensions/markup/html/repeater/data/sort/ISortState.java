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
package org.apache.wicket.extensions.markup.html.repeater.data.sort;

import org.apache.wicket.util.io.IClusterable;

/**
 * Interface used by OrderByLink to interact with any object that keeps track of sorting state
 * 
 * @author Igor Vaynberg (ivaynberg)
 * @param <S>
 *            the type of the sort property
 * 
 */
public interface ISortState<S> extends IClusterable
{
	/**
	 * Sets sort order of the property
	 * 
	 * @param property
	 *            the name of the property to sort on
	 * @param order
	 *            sort order
	 */
	void setPropertySortOrder(S property, SortOrder order);

	/**
	 * Gets the sort order of a property
	 * 
	 * @param property
	 *            sort property to be checked
	 * @return sort order
	 */
	SortOrder getPropertySortOrder(S property);

}

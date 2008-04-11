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
package org.apache.wicket.markup.repeater;

import org.apache.wicket.model.IModel;

/**
 * Factory interface for creating new child item containers for <b>AbstractPageableView</b>.
 * 
 * @see org.apache.wicket.markup.repeater.AbstractPageableView
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 * @param <T>
 *            Model object type
 */
public interface IItemFactory<T>
{
	/**
	 * Factory method for instances of Item. Each generated item must have a unique id with respect
	 * to other generated items.
	 * 
	 * @param index
	 *            the index of the new data item
	 * @param model
	 *            the model for the new data item
	 * 
	 * @return DataItem new DataItem
	 */
	Item<T> newItem(final int index, final IModel<T> model);

}

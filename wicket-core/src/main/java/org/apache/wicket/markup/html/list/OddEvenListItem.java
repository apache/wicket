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
package org.apache.wicket.markup.html.list;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.model.IModel;

/**
 * ListItem that adds <code>class="odd"</code> or <code>class="even"</code> html attribute depending
 * on its index. Useful for creating zebra styling for html tables.
 * 
 * @author ivaynberg
 * @param <T>
 */
public class OddEvenListItem<T> extends ListItem<T>
{
	private static final long serialVersionUID = 1L;

	public static final String ODD_CSS_CLASS_KEY = "oddListItemCssClass";

	public static final String EVEN_CSS_CLASS_KEY = "evenListItemCssClass";

	/**
	 * Constructor
	 * 
	 * @param index
	 *            list item's index
	 * @param model
	 *            list item's model
	 */
	public OddEvenListItem(int index, IModel<T> model)
	{
		super(index, model);
	}

	/** {@inheritDoc} */
	@Override
	protected void onComponentTag(ComponentTag tag)
	{
		super.onComponentTag(tag);

		tag.append("class", (getIndex() % 2 == 0) ? getString(EVEN_CSS_CLASS_KEY) : getString(ODD_CSS_CLASS_KEY), " ");
	}
}

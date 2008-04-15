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
package org.apache.wicket.extensions.markup.html.form.select;

import org.apache.wicket.Component;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;

/**
 * Component representing a single <code>&lt;option&gt;</code> html element
 * 
 * TODO Post 1.2: General: Example
 * 
 * @see Select
 * 
 * @author Igor Vaynberg
 */
public class SelectOption extends WebMarkupContainer
{
	private static final long serialVersionUID = 1L;

	/**
	 * @see WebMarkupContainer#WebMarkupContainer(String)
	 */
	public SelectOption(String id)
	{
		super(id);
	}

	/**
	 * @see WebMarkupContainer#WebMarkupContainer(String, IModel)
	 */
	public SelectOption(String id, IModel model)
	{
		super(id, model);
	}


	/**
	 * @see Component#onComponentTag(ComponentTag)
	 * @param tag
	 *            the abstraction representing html tag of this component
	 */
	@Override
	protected void onComponentTag(final ComponentTag tag)
	{

		// must be attached to <option .../> tag
		checkComponentTag(tag, "option");

		Select select = (Select)findParent(Select.class);
		if (select == null)
		{
			throw new WicketRuntimeException(
				"SelectOption component [" +
					getPath() +
					"] cannot find its parent Select. All SelectOption components must be a child of or below in the hierarchy of a Select component.");
		}

		// assign name and value
		tag.put("value", getPath());

		boolean isSelected = false;

		if (select.isSelected(this))
		{
			tag.put("selected", "selected");
		}

		// Default handling for component tag
		super.onComponentTag(tag);
	}

}

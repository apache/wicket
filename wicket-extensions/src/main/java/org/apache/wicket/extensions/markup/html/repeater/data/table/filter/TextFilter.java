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

import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;

/**
 * Filter that can be represented by a text field
 * 
 * @author Igor Vaynberg (ivaynberg)
 * @param <T>
 *            The {@link TextField}'s model object
 * 
 */
public class TextFilter<T> extends AbstractFilter
{
	private static final long serialVersionUID = 1L;

	private final TextField<T> filter;

	/**
	 * Constructor
	 * 
	 * @param id
	 *            component id
	 * @param model
	 *            model for the underlying form component
	 * @param form
	 *            filter form this filter will be added to
	 */
	public TextFilter(final String id, final IModel<T> model, final FilterForm<?> form)
	{
		super(id, form);
		filter = new TextField<>("filter", model);
		enableFocusTracking(filter);
		add(filter);
	}

	/**
	 * @return underlying {@link TextField} form component that represents this filter
	 */
	public final TextField<T> getFilter()
	{
		return filter;
	}
}

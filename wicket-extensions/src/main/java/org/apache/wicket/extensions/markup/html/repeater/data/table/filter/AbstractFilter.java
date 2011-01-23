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

import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

/**
 * Base class for filters that provides some useful functionality
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class AbstractFilter extends Panel
{
	private static final long serialVersionUID = 1L;

	private final FilterForm<?> form;

	/**
	 * @param id
	 *            component id
	 * @param form
	 *            filter form of the filter toolbar
	 */
	public AbstractFilter(final String id, final FilterForm<?> form)
	{
		super(id);
		this.form = form;
	}

	/**
	 * Enables the tracking of focus for the specified form component. This allows the filter form
	 * to restore focus to the component which caused the form submission. Great for when you are
	 * inside a filter textbox and use the enter key to submit the filter.
	 * 
	 * @param fc
	 *            form component for which focus tracking will be enabled
	 */
	protected void enableFocusTracking(final FormComponent<?> fc)
	{
		form.enableFocusTracking(fc);
	}

	protected IFilterStateLocator<?> getStateLocator()
	{
		return form.getStateLocator();
	}

	protected IModel<?> getStateModel()
	{
		return form.getDefaultModel();
	}

	protected Object getState()
	{
		return form.getDefaultModelObject();
	}

}

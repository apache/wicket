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
package org.apache.wicket.model;

import org.apache.wicket.Application;
import org.apache.wicket.Component;

/**
 * A model that represents a localized resource string. This is a lightweight version of the
 * {@link StringResourceModel}. It lacks parameter substitutions, but is generally easier to use.
 * <p>
 * If you don't use this model as primary component model (you don't specify it in component
 * constructor and don't assign it to component using {@link Component#setDefaultModel(IModel)}),
 * you will need to connect the model with a component using {@link #wrapOnAssignment(Component)}.
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class ResourceModel implements IComponentAssignedModel<String>
{
	private static final long serialVersionUID = 1L;

	private final String resourceKey;

	private final IModel<String> defaultValue;

	/**
	 * Constructor
	 * 
	 * @param resourceKey
	 *            key of the resource this model represents
	 */
	public ResourceModel(String resourceKey)
	{
		this(resourceKey, (IModel<String>)null);
	}

	/**
	 * Constructor
	 * 
	 * @param resourceKey
	 *            key of the resource this model represents
	 * @param defaultValue
	 *            value that will be returned if resource does not exist
	 * 
	 */
	public ResourceModel(String resourceKey, String defaultValue)
	{
		this(resourceKey, Model.of(defaultValue));
	}

	public ResourceModel(String resourceKey, IModel<String> defaultValue)
	{
		this.resourceKey = resourceKey;
		this.defaultValue = defaultValue;
	}

	@Override
	public String getObject()
	{
		// this shouldn't be called always wrapped!
		return Application.get()
			.getResourceSettings()
			.getLocalizer()
			.getString(resourceKey, null, null, null, null, defaultValue);
	}

	@Override
	public final void setObject(String object)
	{
		IComponentAssignedModel.super.setObject(object);
	}

	@Override
	public IWrapModel<String> wrapOnAssignment(final Component component)
	{
		return new AssignmentWrapper(component);
	}

	private class AssignmentWrapper extends LoadableDetachableModel<String>
		implements
			IWrapModel<String>
	{
		private static final long serialVersionUID = 1L;

		private final Component component;

		/**
		 * Construct.
		 * 
		 * @param component
		 */
		AssignmentWrapper(Component component)
		{
			this.component = component;
		}

		@Override
		public IModel<String> getWrappedModel()
		{
			return ResourceModel.this;
		}

		@Override
		protected String load()
		{
			return Application.get()
				.getResourceSettings()
				.getLocalizer()
				.getString(resourceKey, component, null, null, null, defaultValue);
		}

		@Override
		protected void onDetach()
		{
			ResourceModel.this.detach();
		}

	}
}

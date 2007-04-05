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
package wicket.model;

import wicket.Component;
import wicket.util.lang.PropertyResolver;

/**
 * A model that references a property by name on the current model of the
 * component it is bound to. This enables direct usage of inherited models such
 * as compound property models.
 * 
 * @author Jonathan Locke
 */
public class ComponentPropertyModel extends AbstractReadOnlyModel
		implements
			IComponentAssignedModel
{
	private static final long serialVersionUID = 1L;

	/** Name of property to read */
	private final String propertyName;

	/**
	 * Constructor
	 * 
	 * @param propertyName
	 *            The name of the property to reference
	 */
	public ComponentPropertyModel(final String propertyName)
	{
		this.propertyName = propertyName;
	}

	/**
	 * @see wicket.model.AbstractReadOnlyModel#getObject()
	 */
	public Object getObject()
	{
		throw new IllegalStateException("Wrapper should have been called");
	}

	/**
	 * @see wicket.model.IComponentAssignedModel#wrapOnAssignment(wicket.Component)
	 */
	public INestedModelContainer wrapOnAssignment(final Component component)
	{
		return new AssignmentWrapper(component, propertyName);
	}

	/**
	 * Wrapper used when assigning a ComponentPropertyModel to a component.
	 */
	static private class AssignmentWrapper extends AbstractReadOnlyModel
			implements
				INestedModelContainer
	{
		private static final long serialVersionUID = 1L;

		private final Component component;

		private final String propertyName;

		AssignmentWrapper(final Component component, final String propertyName)
		{
			this.component = component;
			this.propertyName = propertyName;
		}

		/**
		 * @see wicket.model.INestedModelContainer#getNestedModel()
		 */
		public IModel getNestedModel()
		{
			return component.getModel();
		}

		protected String propertyExpression()
		{
			return propertyName;
		}

		public Object getObject()
		{
			return PropertyResolver.getValue(propertyName, component.getInnermostModel()
					.getObject());
		}
	}
}

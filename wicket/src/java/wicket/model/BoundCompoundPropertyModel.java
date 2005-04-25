/*
 * $Id$
 * $Revision$ $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.model;

import java.io.Serializable;
import java.util.ArrayList;

import wicket.Component;

/**
 * A compound property model that supports type conversions and OGNL expression
 * bindings.
 * 
 * @author Jonathan Locke
 */
public class BoundCompoundPropertyModel extends CompoundPropertyModel
{
	/**
	 * List of Bindings. Although a Map would be a more natural implementation
	 * here, a List is much more compact in terms of space. Although it may take
	 * longer to find a component binding in theory, in practice it's unlikely
	 * that any BoundCompoundPropertyModel will really have enough bindings to
	 * matter.
	 */
	private final ArrayList bindings = new ArrayList(1);

	/**
	 * Internal binding representation.
	 * 
	 * @author Jonathan Locke
	 */
	private class Binding implements Serializable
	{
		private final Component component;
		private final String ognlExpression;
		private final Class type;

		private Binding(final Component component, final String ognlExpression, final Class type)
		{
			this.component = component;
			this.ognlExpression = ognlExpression;
			this.type = type;
		}
	}

	/**
	 * Constructor
	 * 
	 * @param model
	 *            The model object, which may or may not implement IModel
	 */
	public BoundCompoundPropertyModel(final Object model)
	{
		super(model);
	}

	/**
	 * Adds a property binding.
	 * 
	 * @param component
	 *            The component to bind
	 * @param ognlExpression
	 *            An OGNL expression pointing to the property in this model
	 * @return The component, for convenience in adding components
	 */
	public Component bind(final Component component, final String ognlExpression)
	{
		bind(component, ognlExpression, null);
		return component;
	}

	/**
	 * Adds a type conversion binding.
	 * 
	 * @param component
	 *            The component to bind
	 * @param type
	 *            The type of the property
	 * @return The component, for convenience in adding components
	 */
	public Component bind(final Component component, final Class type)
	{
		bind(component, component.getId(), type);
		return component;
	}

	/**
	 * Adds a property and type conversion binding.
	 * 
	 * @param component
	 *            The component to bind
	 * @param ognlExpression
	 *            An OGNL expression pointing to the property in this model
	 * @param type
	 *            The type of the property
	 * @return The component, for convenience in adding components
	 */
	public Component bind(final Component component, final String ognlExpression, final Class type)
	{
		// Add new binding
		bindings.add(new Binding(component, ognlExpression, type));
		return component;
	}

	/**
	 * @see wicket.model.CompoundPropertyModel#onDetach()
	 */
	protected void onDetach()
	{
		super.onDetach();

		// Minimize the size of the bindings list
		bindings.trimToSize();
	}

	/**
	 * @see wicket.model.AbstractPropertyModel#ognlExpression(wicket.Component)
	 */
	protected String ognlExpression(final Component component)
	{
		final Binding binding = getBinding(component);
		return binding != null ? binding.ognlExpression : component.getId();
	}

	/**
	 * @see wicket.model.AbstractPropertyModel#propertyType(wicket.Component)
	 */
	protected Class propertyType(final Component component)
	{
		final Binding binding = getBinding(component);
		return binding != null ? binding.type : null;
	}

	/**
	 * @param component
	 *            Component to get binding for
	 * @return The binding information
	 */
	private Binding getBinding(final Component component)
	{
		for (int i = 0; i < bindings.size(); i++)
		{
			final Binding binding = (Binding)bindings.get(i);
			if (component == binding.component)
			{
				return binding;
			}
		}
		return null;
	}
}

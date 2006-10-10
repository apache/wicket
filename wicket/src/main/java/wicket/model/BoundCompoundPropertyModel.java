/*
 * $Id: BoundCompoundPropertyModel.java 5861 2006-05-25 20:55:07 +0000 (Thu, 25
 * May 2006) eelco12 $ $Revision$ $Date: 2006-05-25 20:55:07 +0000 (Thu,
 * 25 May 2006) $
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
 * A compound property model that supports type conversions and property
 * expression bindings.
 * 
 * @param <T>
 *            Type of model object this model holds
 * 
 * @author Jonathan Locke
 */
public class BoundCompoundPropertyModel<T> extends CompoundPropertyModel<T>
{
	private static final long serialVersionUID = 1L;

	/**
	 * List of Bindings. Although a Map would be a more natural implementation
	 * here, a List is much more compact in terms of space. Although it may take
	 * longer to find a component binding in theory, in practice it's unlikely
	 * that any BoundCompoundPropertyModel will really have enough bindings to
	 * matter.
	 */
	private final ArrayList<Binding> bindings = new ArrayList<Binding>(1);

	/**
	 * Internal binding representation.
	 * 
	 * @author Jonathan Locke
	 */
	private class Binding implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private final Component component;
		private final String propertyExpression;

		private Binding(final Component component, final String propertyExpression)
		{
			this.component = component;
			this.propertyExpression = propertyExpression;
		}

		/**
		 * @see Object#toString()
		 */
		@Override
		public String toString()
		{
			StringBuffer sb = new StringBuffer("Binding(");
			sb.append(":component=[").append(component).append("]");
			sb.append(":expression=[").append(propertyExpression).append("]");
			sb.append(")");
			return sb.toString();
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
	 * @param propertyExpression
	 *            A property expression pointing to the property in this model
	 * @return The component, for convenience in adding components
	 */
	public Component bind(final Component component, final String propertyExpression)
	{
		bindings.add(new Binding(component, propertyExpression));
		return component;
	}

	/**
	 * Adds a type conversion binding.
	 * 
	 * @param component
	 *            The component to bind
	 * @return The component, for convenience in adding components
	 */
	public Component bind(final Component component)
	{
		bind(component, component.getId());
		return component;
	}

	/**
	 * @see wicket.model.CompoundPropertyModel#onDetach()
	 */
	@Override
	public void detach()
	{
		super.detach();

		// Minimize the size of the bindings list
		bindings.trimToSize();
	}

	@Override
	protected String propertyExpression(final Component component)
	{
		final Binding binding = getBinding(component);
		if (binding != null)
		{
			return binding.propertyExpression;
		}
		else if (component != null)
		{
			return component.getId();
		}
		return null;
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
			final Binding binding = bindings.get(i);
			if (component == binding.component)
			{
				return binding;
			}
		}
		return null;
	}

	/**
	 * @see wicket.model.AbstractDetachableModel#toString()
	 */
	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer(super.toString());
		sb.append(":bindings=[");
		for (int i = 0, size = this.bindings.size(); i < size; i++)
		{
			if (i > 0)
			{
				sb.append(",");
			}
			sb.append(bindings.get(i));
		}
		sb.append("]");
		return sb.toString();
	}
}

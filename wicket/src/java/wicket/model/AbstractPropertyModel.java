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

import wicket.Component;
import wicket.util.lang.PropertyResolver;
import wicket.util.string.Strings;

/**
 * Serves as a base class for different kinds of property models.
 * 
 * @see wicket.model.AbstractDetachableModel
 * 
 * @author Chris Turner
 * @author Eelco Hillenius
 * @author Jonathan Locke
 */
public abstract class AbstractPropertyModel extends AbstractDetachableModel
{
	/** Any model object (which may or may not implement IModel) */
	private Object nestedModel;

	/**
	 * Constructor
	 * 
	 * @param modelObject
	 *            The nested model object
	 */
	public AbstractPropertyModel(final Object modelObject)
	{
		if (modelObject == null)
		{
			throw new IllegalArgumentException("Parameter modelObject cannot be null");
		}

		this.nestedModel = modelObject;
	}

	/**
	 * Gets the nested model.
	 * 
	 * @return The nested model, <code>null</code> when this is the final
	 *         model in the hierarchy
	 */
	public final IModel getNestedModel()
	{
		if (nestedModel instanceof IModel)
		{
			return ((IModel)nestedModel);
		}
		return null;
	}

	/**
	 * @param component
	 *            The component to get the model object for
	 * @return The model for this property
	 */
	protected Object modelObject(final Component component)
	{
		if (nestedModel instanceof IModel)
		{
			final IModel model=(IModel)nestedModel;
			if (model instanceof ICompoundModel) {
				return model.getObject(null);
			} else {
				return model.getObject(component);
			}
			
		}
		return nestedModel;
	}

	/**
	 * @param component
	 *            The component to get a property expression for
	 * @return The property expression for the component
	 */
	protected abstract String propertyExpression(Component component);

	/**
	 * @see wicket.model.AbstractDetachableModel#onAttach()
	 */
	protected void onAttach()
	{
	}

	/**
	 * Unsets this property model's instance variables and detaches the model.
	 * 
	 * @see AbstractDetachableModel#onDetach()
	 */
	protected void onDetach()
	{
		// Detach nested object if it's an IModel
		if (nestedModel instanceof IModel)
		{
			((IModel)nestedModel).detach();
		}
	}

	/**
	 * @see wicket.model.AbstractDetachableModel#onGetObject(wicket.Component)
	 */
	protected Object onGetObject(final Component component)
	{
		final String expression = propertyExpression(component);
		if (Strings.isEmpty(expression))
		{
			// Return a meaningful value for an empty property expression
			return modelObject(component);
		}

		final Object modelObject = modelObject(component);
		if (modelObject != null)
		{
			return PropertyResolver.getValue(expression, modelObject);
		}
		return null;
	}

	/**
	 * Applies the property expression on the model object using the given
	 * object argument.
	 * 
	 * @param object
	 *            The object that will be used when setting a value on the model
	 *            object
	 * @see AbstractDetachableModel#onSetObject(Component, Object)
	 */
	protected void onSetObject(final Component component, Object object)
	{
		final String expression = propertyExpression(component);
		if (Strings.isEmpty(expression))
		{
			if (nestedModel instanceof IModel)
			{
				((IModel)nestedModel).setObject(null, object);
			}
			else
			{
				nestedModel = object;
			}
		}
		else
		{
			// Get the real object
			Object modelObject = modelObject(component);

			// If the object is a String
			if (object instanceof String)
			{
				// and that String is not empty
				final String string = (String)object;
				if (!Strings.isEmpty(string))
				{
					// and there is a non-null property type for the component
					final Class propertyType = propertyType(component);
					if (propertyType != null)
					{
						// convert the String to the right type
						object = component.getConverter().convert(string, propertyType);
					}
				}
			}

			PropertyResolver.setValue(expression, modelObject, object, component==null ? null:component.getConverter());
		}
	}

	/**
	 * @param component
	 *            The component
	 * @return The property type
	 */
	protected abstract Class propertyType(Component component);

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer(super.toString());
		sb.append(":nestedModel=[").append(nestedModel).append("]");
		return sb.toString();
	}
}

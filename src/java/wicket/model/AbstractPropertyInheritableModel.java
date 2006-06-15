/*
 * $Id: AbstractPropertyModel.java 5861 2006-05-25 20:55:07 +0000 (Thu, 25 May
 * 2006) eelco12 $ $Revision: 6012 $ $Date: 2006-05-25 20:55:07 +0000 (Thu, 25
 * May 2006) $
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
import wicket.Session;
import wicket.util.lang.PropertyResolver;
import wicket.util.lang.PropertyResolverConverter;
import wicket.util.string.Strings;

/**
 * Serves as a base class for different kinds of property models.
 * 
 * @see wicket.model.AbstractDetachableModel
 * 
 * @param <T>
 *            Type of model object this model holds
 * 
 * @author Chris Turner
 * @author Eelco Hillenius
 * @author Jonathan Locke
 */
public abstract class AbstractPropertyInheritableModel<T> extends AbstractDetachableInheritableModel<T>
{
	/** Any model object (which may or may not implement IModel) */
	private Object nestedModel;

	/**
	 * Constructor
	 * 
	 * @param modelObject
	 *            The nested model object
	 */
	public AbstractPropertyInheritableModel(final Object modelObject)
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
	@Override
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
			return ((IModel)nestedModel).getObject();
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
	@Override
	protected void onAttach()
	{
	}

	/**
	 * Unsets this property model's instance variables and detaches the model.
	 * 
	 * @see AbstractDetachableModel#onDetach()
	 */
	@Override
	protected void onDetach()
	{
		// Detach nested object if it's an IModel
		if (nestedModel instanceof IModel)
		{
			((IModel)nestedModel).detach();
		}
	}

	/**
	 * @see wicket.model.AbstractDetachableModel#onGetObject()
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected T onGetObject(Component component)
	{
		final String expression = propertyExpression(component);
		if (Strings.isEmpty(expression))
		{
			// Return a meaningful value for an empty property expression
			return (T)modelObject(component);
		}

		final Object modelObject = modelObject(component);
		if (modelObject != null)
		{
			return (T)PropertyResolver.getValue(expression, modelObject);
		}
		return null;
	}

	/**
	 * Applies the property expression on the model object using the given
	 * object argument.
	 * @param object
	 *            The object that will be used when setting a value on the model
	 *            object
	 * 
	 * @see AbstractDetachableModel#onSetObject(Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void onSetObject(Component component,T object)
	{
		final String expression = propertyExpression(component);
		if (Strings.isEmpty(expression))
		{
			if (nestedModel instanceof IModel)
			{
				((IModel)nestedModel).setObject(object);
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
					if (propertyType != null && component != null)
					{
						// convert the String to the right type
						object = (T)component.getConverter(propertyType).convertToObject(string,
								component.getLocale());
					}
				}
			}

			PropertyResolverConverter prc = null;
			if (component != null)
			{
				prc = new PropertyResolverConverter(component, component.getLocale());
			}
			else
			{
				prc = new PropertyResolverConverter(Session.get(), Session.get().getLocale());
			}

			PropertyResolver.setValue(expression, modelObject, object, prc);
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
	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer(super.toString());
		sb.append(":nestedModel=[").append(nestedModel).append("]");
		return sb.toString();
	}
}

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

import java.lang.reflect.Member;
import java.util.Map;

import ognl.DefaultTypeConverter;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
import wicket.Component;
import wicket.WicketRuntimeException;
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
	/** Ognl context wrapper object. It contains the type converter. */
	private transient ConversionContext conversionContext;

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
	 * @return The nested model, <code>null</code> when this is the 
	 *         final model in the hierarchy
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
			return ((IModel)nestedModel).getObject(component);
		}
		return nestedModel;
	}

	/**
	 * @param component
	 *            The component to get an OGNL expression for
	 * @return The OGNL expression for the component
	 */
	protected abstract String ognlExpression(Component component);

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
		// Reset OGNL context
		this.conversionContext = null;

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
		final String expression = ognlExpression(component);
		if (Strings.isEmpty(expression))
		{
			// No expression will cause OGNL to throw an exception. The OGNL
			// expression to return the current object is "#this". Instead
			// of throwing that exception, we'll provide a meaningful
			// return value
			return modelObject(component);
		}

		final Object modelObject = modelObject(component);
		if (modelObject != null)
		{
			try
			{
				// note: if property type is null it is ignored by Ognl
				prepareContext(component);
				return Ognl.getValue(expression, conversionContext, modelObject,
						propertyType(component));
			}
			catch (OgnlException e)
			{
				throw new WicketRuntimeException("OGNL Exception: expression='" + expression + "'; path='" + component.getPath() + "'", e);
			}
		}
		return null;
	}

	/**
	 * Applies the Ognl expression on the model object using the given object
	 * argument (Ognl.setValue).
	 * 
	 * @param object
	 *            the object that will be used when applying Ognl.setValue on
	 *            the model object
	 * @see AbstractDetachableModel#onSetObject(Component, Object)
	 */
	protected void onSetObject(final Component component, Object object)
	{
		try
		{
			final String expression = ognlExpression(component);
			if (Strings.isEmpty(expression))
			{
				// No expression will cause OGNL to throw an exception. The OGNL
				// expression to set the current object is "#this".
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
	
				// Let OGNL set the value
				prepareContext(component);
				Ognl.setValue(ognlExpression(component), conversionContext, modelObject, object);
			}
		}
		catch (OgnlException e)
		{
			throw new WicketRuntimeException(e);
		}
	}

	/**
	 * @param component
	 *            The component
	 * @return The property type
	 */
	protected abstract Class propertyType(Component component);

	/**
	 * Gets the Ognl context that is used for evaluating expressions. It
	 * contains the type converter that is used to access the converter
	 * framework.
	 * @param component The Component
	 */
	private final void prepareContext(final Component component)
	{
		// create a context object when it was not yet done for this request cycle
		if (conversionContext == null)
		{
			// Setup ognl context for this request
			this.conversionContext = new ConversionContext();
		}

		// set the current component for each request (as this could be a shared model, this
		// has to be done every time!!!
		conversionContext.component = component;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer(super.toString());
		sb.append(":nestedModel=[").append(nestedModel).append("]");
		return sb.toString();
	}
	/**
	 * Ognl context with a reference to the current component.
	 */
	private static final class ConversionContext extends OgnlContext
	{
		/** current component. */
		Component component;

		/**
		 * Construct.
		 */
		ConversionContext()
		{
			setTypeConverter(new TypeConverter());
		}

		/**
		 * Type converter for expressions.
		 */
		private final class TypeConverter extends DefaultTypeConverter
		{
			/**
			 * @see ognl.DefaultTypeConverter#convertValue(java.util.Map, java.lang.Object, java.lang.Class)
			 */
			public Object convertValue(Map context, Object value, Class toType)
			{
				if (value == null)
				{
					return null;
				}

				if (!toType.isArray() && value instanceof String[]
						&& ((String[])value).length == 1)
				{
					value = ((String[])value)[0];
				}

				if (value instanceof String && ((String)value).trim().equals(""))
				{
					return null;
				}
				return component.getConverter().convert(value, toType);
			}

			/**
			 * @see ognl.TypeConverter#convertValue(java.util.Map, java.lang.Object, java.lang.reflect.Member, java.lang.String, java.lang.Object, java.lang.Class)
			 */
			public Object convertValue(Map context, Object target, Member member,
					String propertyName, Object value, Class toType)
			{
				return convertValue(context, value, toType);
			}
		}
	}
}
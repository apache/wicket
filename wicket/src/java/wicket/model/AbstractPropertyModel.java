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
public abstract class AbstractPropertyModel extends AbstractDetachableModel implements IConvertible
{
	/** Serial Version ID. */
	private static final long serialVersionUID = -3136339624173288385L;

	/** Ognl context wrapper object. It contains the type converter. */
	private transient OgnlContext context;

	/** The converter source for converters to be used by this model. */
	private IConverterSource converterSource;

	/**
	 * This class is registered with the Ognl context before parsing in order to
	 * be able to use our converters. It implements Ognl TypeConverter and uses
	 * the ConverterRegistry to lookup converters. If no converter is found for
	 * a given type, the default conversion of Ognl is used.
	 */
	protected final class OgnlConverterWrapper extends DefaultTypeConverter
	{
		/**
		 * Construct.
		 */
		public OgnlConverterWrapper()
		{
		}

		/**
		 * Converts the provided value to provided type using provided context.
		 * 
		 * @param context
		 *            Ognl context
		 * @param value
		 *            The current, unconverted value
		 * @param toType
		 *            The type that should be converted to
		 * @return Object the converted value
		 * @see ognl.DefaultTypeConverter#convertValue(java.util.Map,
		 *      java.lang.Object, java.lang.Class)
		 */
		public Object convertValue(Map context, Object value, Class toType)
		{
			if (value == null)
			{
				return null;
			}

			if (!toType.isArray() && value instanceof String[] && ((String[])value).length == 1)
			{
				value = ((String[])value)[0];
			}

			if (value instanceof String && ((String)value).trim().equals(""))
			{
				return null;
			}
			return converterSource.getConverter().convert(value, toType);
		}

		/**
		 * This method is only here to satisfy the interface. Method
		 * convertValue(Map, Object, Class) is called, so parameters member and
		 * propertyName are ignored.
		 * 
		 * @param context
		 *            The context
		 * @param target
		 *            The target
		 * @param member
		 *            The member
		 * @param propertyName
		 *            The name of the property
		 * @param value
		 *            The value
		 * @param toType
		 *            The type to convert to
		 * @return the converted value
		 * @see ognl.DefaultTypeConverter#convertValue(java.util.Map,
		 *      java.lang.Object,java.lang.Class)
		 */
		public Object convertValue(Map context, Object target, Member member, String propertyName,
				Object value, Class toType)
		{
			return convertValue(context, value, toType);
		}
	}

	/**
	 * Constructor
	 */
	public AbstractPropertyModel()
	{
	}

	/**
	 * @see wicket.model.IConvertible#setConverterSource(IConverterSource)
	 */
	public final void setConverterSource(final IConverterSource converterSource)
	{
		this.converterSource = converterSource;
	}

	/**
	 * @param component
	 *            The component to get the model object for
	 * @return The model for this property
	 */
	protected abstract Object modelObject(Component component);

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
		this.context = null;
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
				return Ognl.getValue(expression, getContext(), modelObject,
						propertyType(component));
			}
			catch (OgnlException e)
			{
				throw new WicketRuntimeException(e);
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
						object = converterSource.getConverter().convert(string, propertyType);
					}
				}
			}

			// Let OGNL set the value
			Ognl.setValue(ognlExpression(component), getContext(), modelObject, object);
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
	 * 
	 * @return the Ognl context that is used for evaluating expressions.
	 */
	private final OgnlContext getContext()
	{
		if (context == null)
		{
			// Setup ognl context for this request
			this.context = new OgnlContext();
			context.setTypeConverter(new OgnlConverterWrapper());
		}
		return context;
	}
}
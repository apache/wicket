/*
 * $Id$ $Revision:
 * 1.31 $ $Date$
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
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
 * A PropertyModel is used to dynamically access a model using an <a
 * href="www.ognl.org">Ognl expression </a>.
 * <p>
 * For example, take the following bean:
 * 
 * <pre>
 * public class Person
 * {
 * 	private String name;
 * 
 * 	public String getName()
 * 	{
 * 		return name;
 * 	}
 * 
 * 	public void setName(String name)
 * 	{
 * 		this.name = name;
 * 	}
 * }
 * </pre>
 * 
 * We could construct a label that dynamically fetches the name property of the
 * given person object like this:
 * 
 * <pre>
 *            Person person = getSomePerson();
 *            ...
 *            add(new Label(&quot;myLabel&quot;, person, &quot;name&quot;);
 * </pre>
 * 
 * Where 'myLabel' is the name of the component, and 'name' is the Ognl
 * expression to get the name property.
 * </p>
 * <p>
 * In the same fashion, we can create form components that work dynamically on
 * the given model object. For instance, we could create a text field that
 * updates the name property of a person like this:
 * 
 * <pre>
 *            add(new TextField(&quot;myTextField&quot;, person, &quot;name&quot;);
 * </pre>
 * 
 * </p>
 * <p>
 * To force Ognl to convert to a specific type, you can provide constructor
 * argument 'propertyType'.if that is set, that type is used for conversion
 * instead of the type that is figured out by Ognl. This can be especially
 * usefull for when you have a generic property (like Serializable myProp) that
 * you want to be converted to a narrower type (e.g. an Integer). Ognl sees an
 * incomming string being compatible with the target property, and will then
 * bypass the converter. Hence, to force myProp being converted to and from and
 * integer, propertyType should be set to Integer.
 * </p>
 * 
 * @see wicket.model.IModel
 * @see wicket.model.Model
 * @see wicket.model.DetachableModel
 * 
 * @author Chris Turner
 * @author Eelco Hillenius
 */
public class PropertyModel extends DetachableModel implements IConvertible
{
	/** Serial Version ID. */
	private static final long serialVersionUID = -3136339624173288385L;

	/** Ognl context wrapper object. It contains the type converter. */
	private transient OgnlContext context;

	/** The converter provider to use to get converters to be used by this model. */
	private Component converterProvider;

	/** Ognl expression for property access. */
	private final String expression;

	/** The model. */
	private final IModel model;

	/**
	 * if this is set, this type is used for conversion instead of the type that
	 * is figured out by Ognl. This can be especially usefull for when you have
	 * a generic property (like Serializable myProp) that you want to be
	 * converted to a narrower type (e.g. an Integer). Ognl sees an incomming
	 * string being compatible with the target property, and will then bypass
	 * the converter. Hence, to force myProp being converted to and from and
	 * integer, propertyType should be set to Integer.
	 */
	private final Class propertyType;

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
			return converterProvider.getConverter().convert(value, toType);
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
	 * Construct with an IModel object and a Ognl expression that works on the
	 * given model. Additional formatting will be used depending on the
	 * configuration setting.
	 * 
	 * @param model
	 *            the wrapper
	 * @param expression
	 *            Ognl expression for property access
	 */
	public PropertyModel(final IModel model, final String expression)
	{
		this(model, expression, null);
	}

	/**
	 * Construct with an IModel object and a Ognl expression that works on the
	 * given model. Additional formatting will be used depending on the
	 * configuration setting.
	 * 
	 * @param model
	 *            the wrapper
	 * @param expression
	 *            Ognl expression for property access
	 * @param propertyType
	 *            the type to be used for conversion instead of the type that is
	 *            figured out by Ognl. This can be especially usefull for when
	 *            you have a generic property (like Serializable myProp) that
	 *            you want to be converted to a narrower type (e.g. an Integer).
	 *            Ognl sees an incomming string being compatible with the target
	 *            property, and will then bypass the converter. Hence, to force
	 *            myProp being converted to and from and integer, propertyType
	 *            should be set to Integer.
	 */
	public PropertyModel(final IModel model, final String expression, Class propertyType)
	{
		super(null);

		if (model == null)
		{
			throw new IllegalArgumentException("Model parameter must not be null");
		}

		this.model = model;
		this.expression = expression;
		this.propertyType = propertyType;
	}

	/**
	 * Gets the value that results when the given Ognl expression is applied to
	 * the model object (Ognl.getValue).
	 * 
	 * @return the value that results when the given Ognl expression is applied
	 *         to the model object
	 * @see wicket.model.IModel#getObject()
	 */
	public Object getObject()
	{
		final String expression = getExpression();
		if (Strings.isEmpty(expression))
		{
			// No expression will cause OGNL to throw an exception. The OGNL
			// expression to return the current object is "#this". Instead
			// of throwing that exception, we'll provide a meaningfull
			// return value
			return model.getObject();
		}

		if (model != null)
		{
			final Object modelObject = model.getObject();
			if (modelObject != null)
			{
				try
				{
					// note: if property type is null it is ignored by Ognl
					return Ognl.getValue(expression, getContext(), modelObject, propertyType);
				}
				catch (OgnlException e)
				{
					throw new WicketRuntimeException(e);
				}
			}
		}
		return null;
	}

	/**
	 * Gets the type to be used for conversion instead of the type that is
	 * figured out by Ognl.
	 * 
	 * @return the type to be used for conversion instead of the type that is
	 *         figured out by Ognl
	 */
	public final Class getPropertyType()
	{
		return propertyType;
	}

	/**
	 * Set the provider of the converter to be used by this property model.
	 * 
	 * @param component
	 *            the converter provider component
	 * @see wicket.model.IConvertible#setConverterProvider(wicket.Component)
	 */
	public void setConverterProvider(final Component component)
	{
		this.converterProvider = component;
	}

	/**
	 * Applies the Ognl expression on the model object using the given object
	 * argument (Ognl.setValue).
	 * 
	 * @param object
	 *            the object that will be used when applying Ognl.setValue on
	 *            the model object
	 * @see wicket.model.IModel#setObject(java.lang.Object)
	 */
	public void setObject(Object object)
	{
		try
		{
			// Get the ognl expression
			String expression = getExpression();

			// Get the real object
			Object target = getModel().getObject();

			// Convert the incoming object to the target type when not null and
			// the property type is set and the incoming object is a non-empty
			// string
			if (object != null && propertyType != null && (object instanceof String)
					&& (!((String)object).trim().equals("")))
			{
				// Convert to set type
				object = converterProvider.getConverter().convert(object, propertyType);
			}

			// Let ognl set the value
			Ognl.setValue(expression, getContext(), target, object);
		}
		catch (OgnlException e)
		{
			throw new WicketRuntimeException(e);
		}
	}

	/**
	 * Initializes the instance variables of this property model, and in case
	 * the wrapped model is a {@link IDetachableModel}, calls attach on the
	 * wrapped model.
	 * 
	 * @see wicket.model.DetachableModel#doAttach()
	 */
	protected final void doAttach()
	{
		if (model instanceof IDetachableModel)
		{
			((IDetachableModel)model).attach();
		}
	}

	/**
	 * Unsets this property model's instance variables and, in case the wrapped
	 * model is a {@link IDetachableModel}, calls dettach on the wrapped model.
	 * 
	 * @see wicket.model.DetachableModel#doDetach()
	 */
	protected final void doDetach()
	{
		if (model instanceof IDetachableModel)
		{
			((IDetachableModel)model).detach();
		}

		// Reset OGNL context
		this.context = null;
	}

	/**
	 * Gets the Ognl context that is used for evaluating expressions. It
	 * contains the type converter that is used to access the converter
	 * framework.
	 * 
	 * @return the Ognl context that is used for evaluating expressions.
	 */
	protected final OgnlContext getContext()
	{
		if (context == null)
		{
			// Setup ognl context for this request
			this.context = new OgnlContext();
			context.setTypeConverter(new OgnlConverterWrapper());
		}
		return context;
	}

	/**
	 * Gets the Ognl expression that works on the model. This expression is used
	 * with both Ognl.getValue (used in getObject) and Ognl.setValue (used in
	 * setObject). Usually, this expression accords with simple property acces
	 * (like if we have a Person object with a name property, the expression
	 * would be 'name'), but it can in principle contain any valid Ognl
	 * expression that has meaning with both the Ognl.getValue and Ognl.setValue
	 * operations.
	 * 
	 * @return expression the Ognl expression that works on the model.
	 */
	protected final String getExpression()
	{
		return expression;
	}

	/**
	 * Gets the model on which the Ognl expressions are applied. The expression
	 * will actually not be applied on the instance of IModel, but (naturally)
	 * on the wrapped model object or more accurate, the object that results
	 * from calling getObject on the instance of IModel.
	 * 
	 * @return The model on which the Ognl expressions are applied.
	 */
	protected final IModel getModel()
	{
		return model;
	}

	/**
	 * Sets the Ognl context that is used for evaluating expressions. It
	 * contains the type converter that is used to access the converter
	 * framework.
	 * 
	 * @param context
	 *            the Ognl context that is used for evaluating expressions
	 */
	protected final void setContext(OgnlContext context)
	{
		this.context = context;
	}
}
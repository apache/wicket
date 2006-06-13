/*
 * $Id: PropertyModel.java 5861 2006-05-25 20:55:07 +0000 (Thu, 25 May 2006)
 * eelco12 $ $Revision$ $Date: 2006-05-25 20:55:07 +0000 (Thu, 25 May
 * 2006) $
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

/**
 * A PropertyModel is used to dynamically access a model using a "property
 * expression". See {@link PropertyResolver} javadoc for allowed property
 * expressions.
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
 *        Person person = getSomePerson();
 *        ...
 *        add(new Label(&quot;myLabel&quot;, new PopertyModel(person, &quot;name&quot;));
 * </pre>
 * 
 * Where 'myLabel' is the name of the component, and 'name' is the property
 * expression to get the name property.
 * </p>
 * <p>
 * In the same fashion, we can create form components that work dynamically on
 * the given model object. For instance, we could create a text field that
 * updates the name property of a person like this:
 * 
 * <pre>
 *        add(new TextField(&quot;myTextField&quot;, new PropertyModel(person, &quot;name&quot;));
 * </pre>
 * 
 * </p>
 * <p>
 * To force conversion of property value to a specific type, you can provide
 * constructor argument 'propertyType'. if that is set, that type is used for
 * conversion instead of the type that is figured out by
 * {@link PropertyResolver}. This can be especially useful for when you have a
 * generic property (like Serializable myProp) that you want to be converted to
 * a narrower type (e.g. an Integer). {@link PropertyResolver} sees an incomming
 * string being compatible with the target property, and will then bypass the
 * converter. Hence, to force myProp being converted to and from an integer,
 * propertyType should be set to Integer.
 * </p>
 * 
 * @see wicket.model.IModel
 * @see wicket.model.Model
 * @see wicket.model.AbstractDetachableModel
 * 
 * @param <T>
 *            Type of model object this model holds
 * 
 * @author Chris Turner
 * @author Eelco Hillenius
 * @author Jonathan Locke
 */
public class PropertyModel<T> extends AbstractPropertyAssignmentAwareModel<T>
{
	private static final long serialVersionUID = 1L;

	/** Property expression for property access. */
	private final String expression;

	/**
	 * If this is set, this type is used for conversion instead of the type that
	 * is figured out by the property expression code. This can be especially
	 * useful for when you have a generic property (like Serializable myProp)
	 * that you want to be converted to a narrower type (e.g. an Integer). The
	 * property expression code sees an incoming string being compatible with
	 * the target property, and will then bypass the converter. Hence, to force
	 * myProp being converted to and from an integer, propertyType should be set
	 * to Integer.
	 */
	private final Class propertyType;

	/**
	 * Construct with a wrapped (IModel) or unwrapped (non-IModel) object and a
	 * property expression that works on the given model. Additional formatting
	 * will be used depending on the configuration setting.
	 * 
	 * @param modelObject
	 *            The model object, which may or may not implement IModel
	 * @param expression
	 *            Property expression for property access
	 */
	public PropertyModel(final Object modelObject, final String expression)
	{
		this(modelObject, expression, null);
	}

	/**
	 * Construct with a wrapped (IModel) or unwrapped (non-IModel) object and a
	 * property expression that works on the given model. Additional formatting
	 * will be used depending on the configuration setting.
	 * 
	 * @param modelObject
	 *            The model object, which may or may not implement IModel
	 * @param expression
	 *            Property expression for property access
	 * @param propertyType
	 *            The type to be used for conversion instead of the type that is
	 *            figured out by the property expression code. This can be
	 *            especially useful for when you have a generic property (like
	 *            Serializable myProp) that you want to be converted to a
	 *            narrower type (e.g. an Integer). The property expression code
	 *            sees an incoming string being compatible with the target
	 *            property, and will then bypass the converter. Hence, to force
	 *            myProp being converted to and from an integer, propertyType
	 *            should be set to Integer.
	 */
	public PropertyModel(final Object modelObject, final String expression, Class propertyType)
	{
		super(modelObject);
		this.expression = expression;
		this.propertyType = propertyType;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer(super.toString());
		sb.append(":expression=[").append(expression).append("]");
		sb.append(":propertyType=[").append(propertyType).append("]");
		return sb.toString();
	}

	/**
	 * @see wicket.model.AbstractPropertyAssignmentAwareModel#propertyExpression(wicket.Component)
	 */
	@Override
	protected String propertyExpression(Component component)
	{
		return expression;
	}

	/**
	 * @see wicket.model.AbstractPropertyAssignmentAwareModel#propertyType(wicket.Component)
	 */
	@Override
	protected Class propertyType(Component component)
	{
		return propertyType;
	}
}
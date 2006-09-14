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
 *         Person person = getSomePerson();
 *         ...
 *         add(new Label(&quot;myLabel&quot;, new PopertyModel(person, &quot;name&quot;));
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
 *         add(new TextField(&quot;myTextField&quot;, new PropertyModel(person, &quot;name&quot;));
 * </pre>
 * 
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
public class PropertyModel<T> extends AbstractPropertyModel<T>
{
	private static final long serialVersionUID = 1L;

	/** Property expression for property access. */
	private final String expression;

	/**
	 * Construct with a wrapped (IModel) or unwrapped (non-IModel) object and a
	 * property expression that works on the given model. Additional formatting
	 * will be used depending on the configuration setting.
	 * 
	 * @param targetObject
	 *            The target object, which may or may not implement IModel
	 * @param expression
	 *            Property expression for property access
	 */
	public PropertyModel(final Object targetObject, final String expression)
	{
		super(targetObject);
		this.expression = expression;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer(super.toString());
		sb.append(":expression=[").append(expression).append("]");
		return sb.toString();
	}

	/**
	 * @see wicket.model.AbstractPropertyModel#propertyExpression()
	 */
	@Override
	protected String propertyExpression()
	{
		return expression;
	}


}
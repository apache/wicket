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
package org.apache.wicket.model;

import org.apache.wicket.util.lang.PropertyResolver;

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
 *     Person person = getSomePerson();
 *     ...
 *     add(new Label(&quot;myLabel&quot;, new PopertyModel(person, &quot;name&quot;));
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
 *     add(new TextField(&quot;myTextField&quot;, new PropertyModel(person, &quot;name&quot;));
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
 * @see org.apache.wicket.model.IModel
 * @see org.apache.wicket.model.Model
 * @see org.apache.wicket.model.AbstractDetachableModel
 * 
 * @author Chris Turner
 * @author Eelco Hillenius
 * @author Jonathan Locke
 */
public class PropertyModel extends AbstractPropertyModel
{
	private static final long serialVersionUID = 1L;

	/** Property expression for property access. */
	private final String expression;

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
		super(modelObject);
		this.expression = expression;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer(super.toString());
		sb.append(":expression=[").append(expression).append("]");
		return sb.toString();
	}

	/**
	 * @see org.apache.wicket.model.AbstractPropertyModel#propertyExpression(org.apache.wicket.Component)
	 */
	protected String propertyExpression()
	{
		return expression;
	}
}
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

import org.apache.wicket.core.util.lang.PropertyResolver;

/**
 * A PropertyModel is used to dynamically access a model using a "property expression". See
 * {@link PropertyResolver} javadoc for allowed property expressions.
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
 * We could construct a label that dynamically fetches the name property of the given person object
 * like this:
 * 
 * <pre>
 *     Person person = getSomePerson();
 *     ...
 *     add(new Label(&quot;myLabel&quot;, new PropertyModel(person, &quot;name&quot;));
 * </pre>
 * 
 * Where 'myLabel' is the name of the component, and 'name' is the property expression to get the
 * name property.
 * </p>
 * <p>
 * In the same fashion, we can create form components that work dynamically on the given model
 * object. For instance, we could create a text field that updates the name property of a person
 * like this:
 * 
 * <pre>
 *     add(new TextField(&quot;myTextField&quot;, new PropertyModel(person, &quot;name&quot;));
 * </pre>
 * 
 * </p>
 * <p>
 * <strong>Note that the property resolver by default provides access to private members and methods. If
 * guaranteeing encapsulation of the target objects is a big concern, you should consider using an
 * alternative implementation.</strong>
 * </p>
 * 
 * @see PropertyResolver
 * @see IModel
 * @see Model
 * @see LoadableDetachableModel
 * 
 * @author Chris Turner
 * @author Eelco Hillenius
 * @author Jonathan Locke
 * 
 * @param <T>
 *            The Model object type
 */
public class PropertyModel<T> extends AbstractPropertyModel<T>
{
	private static final long serialVersionUID = 1L;

	/** Property expression for property access. */
	private final String expression;

	/**
	 * Construct with a wrapped (IModel) or unwrapped (non-IModel) object and a property expression
	 * that works on the given model.
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

	@Override
	public String toString()
	{
		return super.toString() + ":expression=[" + expression + ']';
	}

	@Override
	protected String propertyExpression()
	{
		return expression;
	}

	/**
	 * Type-infering factory method
	 * 
	 * @param <Z>
	 *            the of the resolved object from the property
	 * @param parent
	 *            object that contains the property
	 * @param property
	 *            property path
	 * @return {@link PropertyModel} instance
	 */
	public static <Z> PropertyModel<Z> of(Object parent, String property)
	{
		return new PropertyModel<>(parent, property);
	}
}

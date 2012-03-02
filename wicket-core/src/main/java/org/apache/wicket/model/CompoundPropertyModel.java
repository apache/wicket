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

import org.apache.wicket.Component;

/**
 * A simple compound model which uses the component's name as the property expression to retrieve
 * properties on the nested model object.
 * 
 * CompoundPropertyModel is a chaining model so it will call get/setobject on the given object if
 * the object is an instanceof IModel itself.
 * 
 * @see org.apache.wicket.model.IModel
 * @see org.apache.wicket.model.Model
 * @see org.apache.wicket.model.LoadableDetachableModel
 * @see IChainingModel
 * 
 * @author Jonathan Locke
 * 
 * @param <T>
 *            The model object type
 */
public class CompoundPropertyModel<T> extends ChainingModel<T> implements IComponentInheritedModel<T>
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param model
	 *            The model
	 */
	public CompoundPropertyModel(final IModel<T> model)
	{
		super(model);
	}

	/**
	 * Constructor
	 * 
	 * @param object
	 *            The model object
	 */
	public CompoundPropertyModel(final T object)
	{
		super(object);
	}

	/**
	 * Returns the property expression that should be used against the target object
	 * 
	 * @param component
	 * @return property expression that should be used against the target object
	 */
	protected String propertyExpression(Component component)
	{
		return component.getId();
	}

	/**
	 * @see org.apache.wicket.model.IComponentInheritedModel#wrapOnInheritance(org.apache.wicket.Component)
	 */
	@Override
	public <C> IWrapModel<C> wrapOnInheritance(Component component)
	{
		return new AttachedCompoundPropertyModel<C>(component);
	}

	/**
	 * Binds this model to a special property by returning a model that has this compound model as
	 * its nested/wrapped model and the property which should be evaluated. This can be used if the
	 * id of the Component isn't a valid property for the data object.
	 * 
	 * @param property
	 *      the name that will be used to find
	 * @return The IModel that is a wrapper around the current model and the property
	 * @param <S>
	 *            the type of the property
	 */
	public <S> IModel<S> bind(String property)
	{
		return new PropertyModel<S>(this, property);
	}

	/**
	 * Component aware variation of the {@link CompoundPropertyModel} that components that inherit
	 * the model get
	 * 
	 * @author ivaynberg
	 * @param <C>
	 *            The model object type
	 */
	private class AttachedCompoundPropertyModel<C> extends AbstractPropertyModel<C>
		implements
			IWrapModel<C>
	{
		private static final long serialVersionUID = 1L;

		private final Component owner;

		/**
		 * Constructor
		 * 
		 * @param owner
		 *            component that this model has been attached to
		 */
		public AttachedCompoundPropertyModel(Component owner)
		{
			super(CompoundPropertyModel.this);
			this.owner = owner;
		}

		/**
		 * @see org.apache.wicket.model.AbstractPropertyModel#propertyExpression()
		 */
		@Override
		protected String propertyExpression()
		{
			return CompoundPropertyModel.this.propertyExpression(owner);
		}

		/**
		 * @see org.apache.wicket.model.IWrapModel#getWrappedModel()
		 */
		@Override
		public IModel<T> getWrappedModel()
		{
			return CompoundPropertyModel.this;
		}

		/**
		 * @see org.apache.wicket.model.AbstractPropertyModel#detach()
		 */
		@Override
		public void detach()
		{
			super.detach();
			CompoundPropertyModel.this.detach();
		}
	}

	/**
	 * Type-infering factory method
	 * 
	 * @param <Z>
	 *     the type of the model's object
	 * @param model
	 *            model
	 * @return {@link CompoundPropertyModel} instance
	 */
	public static <Z> CompoundPropertyModel<Z> of(IModel<Z> model)
	{
		return new CompoundPropertyModel<Z>(model);
	}
}

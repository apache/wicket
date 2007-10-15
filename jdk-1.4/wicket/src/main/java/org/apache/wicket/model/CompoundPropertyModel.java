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
import org.apache.wicket.util.string.AppendingStringBuffer;

/**
 * A simple compound model which uses the component's name as the property
 * expression to retrieve properties on the nested model object.
 *
 * CompoundPropertyModel is a chaining model so it will call get/setobject
 * on the given object if the object is an instanceof IModel itself.
 *
 * @see org.apache.wicket.model.IModel
 * @see org.apache.wicket.model.Model
 * @see org.apache.wicket.model.LoadableDetachableModel
 * @see IChainingModel
 *
 * @author Jonathan Locke
 */
public class CompoundPropertyModel implements IComponentInheritedModel, IChainingModel
{
	private static final long serialVersionUID = 1L;

	private Object target;

	/**
	 * Constructor
	 *
	 * @param object
	 *            The model object, which may or may not implement IModel
	 */
	public CompoundPropertyModel(final Object object)
	{
		target = object;
	}

	/**
	 * @see org.apache.wicket.model.IModel#getObject()
	 */
	public Object getObject()
	{
		if (target instanceof IModel)
		{
			return ((IModel)target).getObject();
		}
		return target;
	}

	/**
	 * @see org.apache.wicket.model.IModel#setObject(java.lang.Object)
	 */
	public void setObject(Object object)
	{
		if (target instanceof IModel)
		{
			((IModel)target).setObject(object);
		}
		else
		{
			target = object;
		}
	}

	/**
	 * @see org.apache.wicket.model.IChainingModel#getChainedModel()
	 */
	public IModel getChainedModel()
	{
		if (target instanceof IModel)
		{
			return (IModel)target;
		}
		return null;
	}

	/**
	 * @see org.apache.wicket.model.IChainingModel#setChainedModel(org.apache.wicket.model.IModel)
	 */
	public void setChainedModel(IModel model)
	{
		target = model;
	}

	/**
	 * @see org.apache.wicket.model.IDetachable#detach()
	 */
	public void detach()
	{
		if (target instanceof IDetachable)
		{
			((IDetachable)target).detach();
		}
	}

	/**
	 * Returns the property expression that should be used against the target
	 * object
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
	public IWrapModel wrapOnInheritance(Component component)
	{
		return new AttachedCompoundPropertyModel(component);
	}

	/**
	 * Binds this model to a special property by returning a model
	 * that has this compound model as its nested/wrapped model and
	 * the property which should be evaluated.
	 * This can be used if the id of the Component isn't a valid property
	 * for the data object.
	 *
	 * @param property
	 * @return The IModel that is a wrapper around the current model and the property
	 */
	public IModel bind(String property)
	{
		return new PropertyModel(this,property);
	}

	/**
	 * Component aware variation of the {@link CompoundPropertyModel} that
	 * components that inherit the model get
	 *
	 * @author ivaynberg
	 */
	private class AttachedCompoundPropertyModel extends AbstractPropertyModel
			implements  IWrapModel
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
		protected String propertyExpression()
		{
			return CompoundPropertyModel.this.propertyExpression(owner);
		}

		/**
		 * @see org.apache.wicket.model.IWrapModel#getWrappedModel()
		 */
		public IModel getWrappedModel()
		{
			return CompoundPropertyModel.this;
		}

		/**
		 * @see org.apache.wicket.model.AbstractPropertyModel#detach()
		 */
		public void detach()
		{
			super.detach();
			CompoundPropertyModel.this.detach();
		}
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		AppendingStringBuffer sb = new AppendingStringBuffer().append("Model:classname=["
				+ getClass().getName() + "]");
		sb.append(":nestedModel=[").append(target).append("]");
		return sb.toString();
	}

	// TODO These methods are for helping people upgrade. Remove after
	// deprecation release.
	/**
	 * @param component
	 * @return
	 * @deprecated replace by {@link IModel#getObject()}.
	 */
	public final Object getObject(Component component)
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @param component
	 * @param object
	 * @deprecated replace by {@link IModel#setObject(Object)}.
	 */
	public final void setObject(Component component, Object object)
	{
		throw new UnsupportedOperationException();
	}

}

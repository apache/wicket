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
 * Quick detachable model that is implements the IComponentAssignedModel and the IModel interfaces.
 * Its a quick replacement for the current setObject(Component,Object) and getObject(Component)
 * methods when the component is needed in a detachable model.
 * 
 * @author jcompagner
 * 
 * @param <T>
 *            The model object type
 */
public class ComponentDetachableModel<T> implements IModel<T>, IComponentAssignedModel<T>
{
	private static final long serialVersionUID = 1L;

	/**
	 * Transient flag to prevent multiple detach/attach scenario. We need to maintain this flag as
	 * we allow 'null' model values.
	 */
	private transient boolean attached = false;

	/**
	 * This getObject throws an exception.
	 * 
	 * @see org.apache.wicket.model.IModel#getObject()
	 */
	@Override
	public final T getObject()
	{
		throw new RuntimeException("get object call not expected on a IComponentAssignedModel");
	}

	@Override
	public final void setObject(T object)
	{
		throw new RuntimeException("set object call not expected on a IComponentAssignedModel");
	}

	/**
	 * Gets whether this model has been attached to the current session.
	 * 
	 * @return whether this model has been attached to the current session
	 */
	public final boolean isAttached()
	{
		return attached;
	}

	/**
	 * Set this model in an attached state. Called if the constructor sets the data. (attached)
	 */
	protected final void setAttached()
	{
		attached = true;
	}

	/**
	 * Attaches to the current request. Implement this method with custom behavior, such as loading
	 * the model object.
	 */
	protected void attach()
	{
	}

	/**
	 * Called when getObject is called in order to retrieve the detachable object. Before this
	 * method is called, attach() is always called to ensure that the object is attached.
	 * 
	 * @param component
	 *            The component asking for the object
	 * @return The object
	 */
	protected T getObject(Component component)
	{
		return null;
	}

	/**
	 * Called when setObject is called in order to change the detachable object. Before this method
	 * is called, attach() is always called to ensure that the object is attached.
	 * 
	 * @param component
	 *            The component asking for replacement of the model object
	 * @param object
	 *            The new model object
	 */
	protected void setObject(Component component, T object)
	{
	}

	@Override
	public IWrapModel<T> wrapOnAssignment(Component comp)
	{
		return new WrapModel<T>(comp);
	}

	private class WrapModel<P> implements IWrapModel<T>
	{
		private static final long serialVersionUID = 1L;

		private final Component component;

		/**
		 * @param comp
		 */
		public WrapModel(Component comp)
		{
			component = comp;
		}

		@Override
		public IModel<T> getWrappedModel()
		{
			return ComponentDetachableModel.this;
		}

		/**
		 * Attaches the model.
		 */
		private void attach()
		{
			if (!attached)
			{
				attached = true;
				ComponentDetachableModel.this.attach();
			}
		}

		@Override
		public T getObject()
		{
			attach();
			return ComponentDetachableModel.this.getObject(component);
		}

		@Override
		public void setObject(T object)
		{
			attach();
			ComponentDetachableModel.this.setObject(component, object);
		}

		@Override
		public void detach()
		{
			if (attached)
			{
				attached = false;
				ComponentDetachableModel.this.detach();
			}
		}
	}
}

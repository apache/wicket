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
 * quick model that is implements the IComponentAssignedModel and the IModel interfaces.
 * Its a quick replacement for the current setObject(Component,Object) and
 * getObject(Component) methods when the component is needed in the model.
 * 
 * @author jcompagner
 */
public class ComponentModel implements IModel, IComponentAssignedModel
{
	private static final long serialVersionUID = 1L;

	/**
	 * This getObject throws an exception.
	 * 
	 * @see org.apache.wicket.model.IModel#getObject()
	 */
	public final Object getObject()
	{
		throw new RuntimeException("get object call not expected on a IComponentAssignedModel");
	}

	/**
	 * @see org.apache.wicket.model.IModel#setObject(java.lang.Object)
	 */
	public final void setObject(Object object)
	{
		throw new RuntimeException("set object call not expected on a IComponentAssignedModel");
	}

	/**
	 * @see org.apache.wicket.model.IDetachable#detach()
	 */
	public void detach()
	{
	}

	/**
	 * @see org.apache.wicket.model.IComponentAssignedModel#wrapOnAssignment(org.apache.wicket.Component)
	 */
	public IWrapModel wrapOnAssignment(Component comp)
	{
		return new WrapModel(comp);
	}

	private class WrapModel implements IWrapModel
	{
		private static final long serialVersionUID = 1L;

		private Component component;

		/**
		 * @param comp
		 */
		public WrapModel(Component comp)
		{
			component = comp;
		}

		/**
		 * @see org.apache.wicket.model.IWrapModel#getWrappedModel()
		 */
		public IModel getWrappedModel()
		{
			return ComponentModel.this;
		}

		/**
		 * @see org.apache.wicket.model.IModel#getObject()
		 */
		public Object getObject()
		{
			return ComponentModel.this.getObject(component);
		}

		/**
		 * @see org.apache.wicket.model.IModel#setObject(java.lang.Object)
		 */
		public void setObject(Object object)
		{
			ComponentModel.this.setObject(component, object);
		}

		/**
		 * @see org.apache.wicket.model.IDetachable#detach()
		 */
		public void detach()
		{
			ComponentModel.this.detach();
		}

	}

	/**
	 * @param component
	 */
	public Object getObject(Component component)
	{
		return null;
	}

	/**
	 * @param component
	 * @param object
	 */
	public void setObject(Component component, Object object)
	{
	}
}

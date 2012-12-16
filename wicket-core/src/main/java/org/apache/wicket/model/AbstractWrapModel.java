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

/**
 * Simple base class for IWrapModel objects see {@link IComponentAssignedModel} or
 * {@link IComponentInheritedModel} so that you don't have to have empty methods like detach or
 * setObject() when not used in the wrapper.
 * 
 * The detach method calls the wrapped models detach.
 * 
 * @author jcompagner
 * 
 * @param <T>
 *            The Model object type
 */
public abstract class AbstractWrapModel<T> implements IWrapModel<T>, IWriteableModel<T>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @see org.apache.wicket.model.IModel#getObject()
	 */
	@Override
	public T getObject()
	{
		return null;
	}

	/**
	 * @see org.apache.wicket.model.IModel#setObject(java.lang.Object)
	 */
	@Override
	public void setObject(T object)
	{
	}

	/**
	 * Calls getWrappedModel().detach();
	 * 
	 * @see org.apache.wicket.model.IDetachable#detach()
	 */
	@Override
	public void detach()
	{
		getWrappedModel().detach();
	}
}

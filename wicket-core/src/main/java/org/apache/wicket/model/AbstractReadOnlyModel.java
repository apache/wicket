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
 * AbstractReadOnlyModel is an adapter base class for implementing models which have no detach logic
 * and are read-only.
 * 
 * @author Igor Vaynberg ( ivaynberg )
 * 
 * @param <T>
 *            The model object
 */
public abstract class AbstractReadOnlyModel<T> implements IModel<T>
{
	private static final long serialVersionUID = 1L;

	/**
	 * @see IModel#getObject()
	 */
	@Override
	public abstract T getObject();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder("Model:classname=[");
		sb.append(getClass().getName()).append("]");
		return sb.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void detach()
	{
	}
}
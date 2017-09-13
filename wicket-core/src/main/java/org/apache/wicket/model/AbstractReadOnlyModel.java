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
 * @deprecated Use an anonymous instance of {@link IModel} instead. Since Wicket 8.0 {@link IModel}
 * doesn't require providing implementation of {@link IModel#setObject(Object)} method.
 */
@Deprecated
public abstract class AbstractReadOnlyModel<T> implements IModel<T>
{
	private static final long serialVersionUID = 1L;

	/**
	 * This default implementation of setObject unconditionally throws an
	 * UnsupportedOperationException. Since the method is final, any subclass is effectively a
	 * read-only model.
	 * 
	 * @param object
	 *            The object to set into the model
	 * @throws UnsupportedOperationException
	 */
	@Override
	public final void setObject(final T object)
	{
		throw new UnsupportedOperationException("Model " + getClass() +
			" does not support setObject(Object)");
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder("Model:classname=[");
		sb.append(getClass().getName()).append("]");
		return sb.toString();
	}
}

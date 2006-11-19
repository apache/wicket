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
package wicket.model;

import wicket.Component;

/**
 * AbstractReadOnlyModel is an adapter base class for implementing models which have no
 * detach logic and are read-only.
 * 
 * @author Igor Vaynberg ( ivaynberg )
 */
public abstract class AbstractReadOnlyModel extends AbstractModel
{

	/**
	 * @see IModel#getObject(Component)
	 */
	public abstract Object getObject(Component component);

	/**
	 * This default implementation of setObject unconditionally throws an
	 * UnsupportedOperationException. Since the method is final, any subclass is
	 * effectively a read-only model.
	 * 
	 * @param component
	 *            The component wanting to set the object
	 * @param object
	 *            The object to set into the model
	 * @throws UnsupportedOperationException
	 */
	public final void setObject(final Component component, final Object object)
	{
		throw new UnsupportedOperationException("Model " + getClass()
				+ " does not support setObject(Object)");
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return super.toString();
	}
}
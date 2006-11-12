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
 * AbstractModel is an adapter base class for implementing models which have no
 * detach logic.
 * 
 * @param <T>
 *            Type of model object this model holds
 * 
 * @author Jonathan Locke
 */
public abstract class AbstractModel<T> implements IModel<T>
{
	/**
	 * @see wicket.model.IModel#detach()
	 */
	public void detach()
	{
	}

	/**
	 * @see Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer("Model:classname=[");
		sb.append(getClass().getName()).append("]");
		return sb.toString();
	}
	
	/**
	 * @param component
	 * @return nada
	 * @deprecated THIS METHOD IS NOT SUPPORTED ANYMORE AND WILL BE REMOVED IN
	 *             WICKET 2.0.1.
	 */
	@Deprecated
	public final Object getObject(final Component component)
	{
		throw new UnsupportedOperationException("since Wicket 2.0, IModel's signature changed. "
				+ "It does not take a component argument anymore.");
	}

	/**
	 * @param component
	 * @param object
	 * @deprecated THIS METHOD IS NOT SUPPORTED ANYMORE AND WILL BE REMOVED IN
	 *             WICKET 2.0.1.
	 */
	@Deprecated
	public final void setObject(final Component component, final Object object)
	{
		throw new UnsupportedOperationException("since Wicket 2.0, IModel's signature changed. "
				+ "It does not take a component argument anymore.");
	}
}

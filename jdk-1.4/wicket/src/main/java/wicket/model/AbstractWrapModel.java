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

/**
 * Simple base class for INestedModelContainer objects see {@link IComponentAssignedModel}
 * or {@link IComponentInheritedModel} so that you don't have to have empty methods
 * like detach or setObject() when not used in the wrapper. 
 * 
 * @author jcompagner
 */
public abstract class AbstractWrapModel implements INestedModelContainer
{
	/**
	 * @see wicket.model.IModel#getObject()
	 */
	public Object getObject()
	{
		return null;
	}

	/**
	 * @see wicket.model.IModel#setObject(java.lang.Object)
	 */
	public void setObject(Object object)
	{
	}

	/**
	 * @see wicket.model.IDetachable#detach()
	 */
	public void detach()
	{
	}

}

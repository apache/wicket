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
 * Models that wish to substitute themselves with a wrapper when they are bound to a component
 * (either through IModel parameter in a constructor or via a call to
 * {@link Component#setDefaultModel(IModel)}) should implement this interface. One reason for a
 * model to want to do this is if it needs to be aware of the component it is bound to.
 * 
 * The algorithm wicket employs is similar to this:
 * 
 * <pre>
 * void Component.setModel(IModel model) 
 * {
 *     if (model instanceof IComponentAssignedModel) 
 *     {
 *        this.model = ((IComponentAssignedModel)model).wrapOnAssignment(this);
 *     } 
 *     else 
 *     {
 *        this.model = model;
 *     }
 * }
 * </pre>
 * 
 * For an example see {@link ResourceModel}
 * 
 * @author jcompagner
 * @author Igor Vaynberg (ivaynberg)
 * @param <T>
 *            The model data type
 */
public interface IComponentAssignedModel<T> extends IModel<T>
{
	/**
	 * This method is called when the component gets its model assigned.
	 * 
	 * WARNING: Because the model can be assigned in the constructor of component this method can
	 * also be called with a 'this' of a component that is not fully constructed yet.
	 * 
	 * @param component
	 * @return The WrapModel that wraps this model
	 */
	IWrapModel<T> wrapOnAssignment(Component component);
}

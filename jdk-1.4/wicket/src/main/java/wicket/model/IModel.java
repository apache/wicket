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
 * A IModel wraps the actual model Object used by a Component. IModel
 * implementations are used as a facade for the real model so that users have
 * control over the actual persistence strategy. Note that objects implementing
 * this interface will be stored in the Session. Hence, you should use
 * (non-transient) instance variables sparingly.
 * <ul>
 * <li><b>Basic Models </b>- To implement a basic (non-detachable) model which
 * holds its entire state in the Session, you can either subclass
 * {@link AbstractModel}, or use the simple model wrapper {@link Model}
 * directly.
 * 
 * <li><b>Detachable Models </b>- IModel inherits a hook,
 * {@link IDetachable#detach()}, so that interface implementers can detach
 * transient information when a model is no longer being actively used by the
 * framework. This reduces memory use and reduces the expense of replicating the
 * model in a clustered server environment. To implement a detachable model, you
 * should generally extend {@link wicket.model.AbstractDetachableModel}instead
 * of implementing IModel directly.
 * 
 * <li><b>Nested Models </b>- IModels can be nested and the innermost model is
 * also known as the "root" model since it is the model on which the outer
 * models rely. The getNestedModel() method on IModel gets any nested model
 * within the given model. This allows Component.sameRootModel() to compare two
 * models to see if they both have the same root model (the same most nested
 * model).
 * <p>
 * For example, a Form might have a Person model and then a TextField might have
 * a PropertyModel which is the "name" property of the Person model. In this
 * case, PropertyModel will implement getNestedModel(), returning the Person
 * model which is the root model of the property model.
 * 
 * <li><b>Property Models </b>- The AbstractPropertyModel class provides
 * default functionality for property models. A property model provides access
 * to a particular property of its wrapped model.
 * 
 * <li><b>Compound Property Models </b>- The IModel interface is parameterized
 * by Component, allowing a model to be shared among several Components. When
 * the {@link IModel#getObject(Component)}method is called, the value returned
 * will depend on the Component which is asking for the value. Likewise, the
 * {@link IModel#setObject(Component, Object)}method sets a different property
 * depending on which Component is doing the setting. For more information on
 * CompoundPropertyModels and model inheritance, see
 * {@link wicket.model.CompoundPropertyModel}and {@link wicket.Page}.
 * </ul>
 * 
 * @see wicket.Component#sameRootModel(wicket.Component)
 * @see wicket.Component#sameRootModel(IModel)
 * 
 * @author Chris Turner
 * @author Eelco Hillenius
 * @author Jonathan Locke
 */
public interface IModel extends IDetachable
{
	/**
	 * Gets the nested model.
	 * 
	 * @return The nested model object.
	 */
	IModel getNestedModel();

	/**
	 * Gets the model object.
	 * 
	 * @param component
	 *            The component which wants to get a model Object
	 * 
	 * @return The model object
	 */
	Object getObject(final Component component);

	/**
	 * Sets the model object.
	 * 
	 * @param component
	 *            The component which wants to set a new model Object
	 * 
	 * @param object
	 *            The model object
	 */
	void setObject(final Component component, final Object object);
}

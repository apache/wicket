/*
 * $Id$ $Revision:
 * 1.10 $ $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.model;

import wicket.Component;

/**
 * A IModel wraps the actual model objects of components. IModel implementations
 * are used as a facade for the real model so that users have control over the
 * actual persistence strategy. Note that instances of implementations of this
 * class will be stored in the session. Hence, you should use (non-transient)
 * instance variables sparingly.
 * <p>
 * IModel also provides a call back mechanism for reacting to the start/end of a
 * request. Please use the abstract class
 * {@link wicket.model.AbstractDetachableModel}for implementations instead of
 * implementing this interface directly.
 * <p>
 * IModels can be nested and the innermost model is also known as the root model
 * since it is the model on which the outer models rely. The getNestedModel()
 * method on IModel gets any nested model within the given model. This allows
 * Component.sameRootModel() to compare two models to see if they both have the
 * same root model (the same most nested model).
 * <p>
 * For example, a Form might have a Person model and then a TextField might have
 * a PropertyModel which is the "name" property of the Person model. In this
 * case, PropertyModel will implement getNestedModel(), returning the Person
 * model which is the root model of the property model.
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
	 * @return The nested model object, which may or may not be an IModel.
	 */
	public Object getNestedModel();

	/**
	 * Gets the model object.
	 * 
	 * @param component
	 *            The component which wants to get a model Object
	 * 
	 * @return The model object
	 */
	public Object getObject(final Component component);

	/**
	 * Sets the model object.
	 * 
	 * @param component
	 *            The component which wants to set a new model Object
	 * 
	 * @param object
	 *            The model object
	 */
	public void setObject(final Component component, final Object object);
}

/*
 * $Id$
 * $Revision$ $Date$
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

/**
 * Interface to get any nested model within a given model. This allows
 * Component.sameRootModel() to compare two models to see if they both have the
 * same root model. For example, a Form might have a Person model and then a
 * TextField might have a PropertyModel which is the "name" property of the
 * Person model. In this case, PropertyModel will implement INestedModel,
 * returning the Person model from getNestedModel().
 * 
 * @see wicket.Component#sameRootModel(wicket.Component)
 * @see wicket.Component#sameRootModel(IModel)
 * @author Jonathan Locke
 */
public interface INestedModel
{
	/**
	 * Gets the nested model object
	 * 
	 * @return The nested model object
	 */
	public IModel getNestedModel();
}

/*
 * $Id: org.eclipse.jdt.ui.prefs 5004 2006-03-17 20:47:08 -0800 (Fri, 17 Mar
 * 2006) eelco12 $ $Revision: 5004 $ $Date: 2006-03-17 20:47:08 -0800 (Fri, 17
 * Mar 2006) $
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
 * Models that wish to substitute themselves with a wraper when they are bound
 * to a component ( either through IModel parameter in a constructor or via a
 * call to {@link Component#setModel(IModel)} ) should implement this interface.
 * One reason for a model to want to do this is if it needs to be aware of the
 * component it is bound to.
 * 
 * The algorithm wicket employes is similar to this:
 * 
 * <pre>
 *   void Component.setModel(IModel model) {
 *     if (model instanceof IAssignementAware) {
 *        this.model=((IAssignmentAware)model).wrapOnAssignment(this);
 *     } else {
 *        this.model=model;
 *     }
 * </pre>
 * 
 * For an example see {@link ResourceModel}
 * 
 * @param <T>
 *            Type of model object this model holds
 * 
 * @author jcompagner
 * @author Igor Vaynberg (ivaynberg)
 */
public interface IAssignmentAware<T> extends IModel<T>
{
	/**
	 * @param <C>
	 * @param component
	 * @return The WrapModel that wraps this model
	 */
	<C> IWrapModel<C> wrapOnAssignment(Component<C> component);
}

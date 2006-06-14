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

/**
 * A marker interface that represents a model that serves as a wrapper for
 * another. Typically these models are produced by the following methods:
 * {@link IAssignmentAware#wrapOnAssignment(wicket.Component)} and
 * {@link IInhertanceAware#wrapOnInhertance(wicket.Component)}
 * 
 * @param <T>
 *            Type of model object this model holds
 * 
 * @author jcompagner
 */
public interface IWrapModel<T> extends IModel<T>
{

}

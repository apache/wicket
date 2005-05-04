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
package wicket;

/**
 * Interface to code that adds feedback messages, such as validation feedback,
 * to some kind of feedback display. For example, FeedbackPanel and
 * FormComponentFeedbackBorder both implement this interfaces so they can give
 * the user feedback when a Form fails to validate. But any component can
 * implement the IFeedback interface if it wishes to be a destination for
 * feedback information.
 * <p>
 * When a form is submitted and one or more validation errors occurs, logic in
 * the Form.onError() method traverses the component hierarchy of the Form being
 * submitted, calling each IFeedback interface it finds. Implementers of the
 * update() method can change their appearance depending on the state of errors
 * in a child component or in the Form as a whole.
 * 
 * @author Jonathan Locke
 * @author Eelco Hillenius
 */
public interface IFeedback
{
	/**
	 * Called to add feedback messages from a component. If the component is a
	 * container, messages will be added for all children of the container.
	 * 
	 * @param component
	 *            The component with associated feedback messages
	 * @param recurse
	 *            True if feedback messages should be added from children of the
	 *            given component
	 */
	public void addFeedbackMessages(Component component, boolean recurse);
	
	/**
	 * Called to clear the current messages
	 */
	public void clearFeedbackMessages();
}

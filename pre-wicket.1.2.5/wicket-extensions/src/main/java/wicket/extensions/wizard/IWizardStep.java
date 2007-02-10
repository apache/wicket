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
package wicket.extensions.wizard;

import java.io.Serializable;

import wicket.Component;

/**
 * Models one step in a wizard, and is the equivalent of one panel in a wizard
 * from an end-user's perspective.
 * <p>
 * Typically, you would extend {@link WizardStep panel based wizard steps} and
 * provide a custom panel for the step instead of directly implementing this
 * interface.
 * </p>
 * 
 * <p>
 * <a href="https://wizard-framework.dev.java.net/">Swing Wizard Framework</a>
 * served as a valuable source of inspiration.
 * </p>
 * 
 * @author Eelco Hillenius
 */
public interface IWizardStep extends Serializable
{
	/**
	 * This method is called whenever the user presses next while this step is
	 * active.
	 * <p>
	 * This method will only be called if {@link IWizardModel#isNextAvailable}
	 * and {@link #isComplete} return true.
	 */
	void applyState();

	/**
	 * Gets the header component for this step. This component is displayed in a
	 * special section of the wizard.
	 * 
	 * @param id
	 *            The id that the component should be created with
	 * @param parent
	 *            The parent component (for post 1.2)
	 * @param wizard
	 *            The wizard component the header will be placed on
	 * @return The header component
	 */
	Component getHeader(String id, Component parent, IWizard wizard);

	/**
	 * Returns the current view this step is displaying. This component will be
	 * displayed in the main section of the wizard with this step is active.
	 * This may changed at any time by as long as an appropriate property change
	 * event is fired.
	 * 
	 * @param id
	 *            The id that the component should be created with
	 * @param parent
	 *            The parent component (for post 1.2)
	 * @param wizard
	 *            The wizard component the header will be placed on
	 * @return The current view of the step.
	 */
	Component getView(String id, Component parent, IWizard wizard);

	/**
	 * Initializes this step with the model it will belong to.
	 * 
	 * @param wizardModel
	 *            the owning wizard model
	 */
	void init(IWizardModel wizardModel);

	/**
	 * Checks if this step is compete. This method should return true if the
	 * wizard can proceed to the next step.
	 * 
	 * @return <tt>true</tt> if the wizard can proceed from this step,
	 *         <tt>false</tt> otherwise.
	 */
	boolean isComplete();
}

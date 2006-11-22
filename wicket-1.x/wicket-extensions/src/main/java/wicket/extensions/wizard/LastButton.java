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

/**
 * Models a 'last' button in the wizard. When pressed, it calls
 * {@link IWizardStep#applyState()} on the active wizard step, and then moves to
 * the last step in the model with {@link IWizardModel#lastStep()}.
 * 
 * @author Eelco Hillenius
 */
public final class LastButton extends WizardButton
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param id
	 *            The component id
	 * @param wizard
	 *            The wizard
	 */
	public LastButton(String id, IWizard wizard)
	{
		super(id, wizard, "wicket.extensions.wizard.last");
	}

	/**
	 * @see wicket.Component#isEnabled()
	 */
	public final boolean isEnabled()
	{
		return getWizardModel().isLastAvailable();
	}

	/**
	 * @see wicket.Component#isVisible()
	 */
	public final boolean isVisible()
	{
		return getWizardModel().isLastVisible();
	}

	/**
	 * @see wicket.extensions.wizard.WizardButton#onClick()
	 */
	public final void onClick()
	{
		IWizardModel wizardModel = getWizardModel();
		wizardModel.getActiveStep().applyState();
		wizardModel.lastStep();
	}
}

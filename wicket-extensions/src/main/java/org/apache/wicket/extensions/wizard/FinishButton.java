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
package org.apache.wicket.extensions.wizard;

/**
 * Models a cancel button in the wizard. When pressed, it calls {@link IWizardStep#applyState()} on
 * the active wizard step, and then {@link Wizard#onFinish()} on the wizard.
 * 
 * @author Eelco Hillenius
 */
public class FinishButton extends WizardButton
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
	public FinishButton(final String id, final IWizard wizard)
	{
		super(id, wizard, "org.apache.wicket.extensions.wizard.finish");
	}

	/**
	 * @see org.apache.wicket.Component#isEnabled()
	 */
	@Override
	public boolean isEnabled()
	{
		IWizardStep activeStep = getWizardModel().getActiveStep();
		return (activeStep != null) && getWizardModel().isLastStep(activeStep) 
			&& super.isEnabled();
	}

	/**
	 * @see org.apache.wicket.extensions.wizard.WizardButton#onClick()
	 */
	@Override
	public void onClick()
	{
		IWizardModel wizardModel = getWizardModel();
		IWizardStep step = wizardModel.getActiveStep();

		// let the step apply any state
		step.applyState();

		// if the step completed after applying the state, notify the wizard
		if (step.isComplete())
		{
			getWizardModel().finish();
		}
		else
		{
			error(getLocalizer().getString(
				"org.apache.wicket.extensions.wizard.FinishButton.step.did.not.complete", this));
		}
	}
}

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
 * Models a next button in the wizard. When pressed, it calls {@link IWizardStep#applyState()} on
 * the active wizard step, and then moves the wizard state to the next step of the model by calling
 * {@link IWizardModel#next() next} on the wizard's model.
 * 
 * @author Eelco Hillenius
 */
public class NextButton extends WizardButton
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param id
	 * @param wizard
	 */
	public NextButton(final String id, final IWizard wizard)
	{
		super(id, wizard, "org.apache.wicket.extensions.wizard.next");
	}

	/**
	 * @see org.apache.wicket.Component#isEnabled()
	 */
	@Override
	public boolean isEnabled()
	{
		return super.isEnabled() && getWizardModel().isNextAvailable();
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

		// if the step completed after applying the state, move the
		// model onward
		if (step.isComplete())
		{
			wizardModel.next();
		}
		else
		{
			error(getLocalizer().getString(
				"org.apache.wicket.extensions.wizard.NextButton.step.did.not.complete", this));
		}
	}

	/**
	 * @see org.apache.wicket.Component#onBeforeRender()
	 */
	@Override
	protected void onBeforeRender()
	{
		getForm().setDefaultButton(this);
		super.onBeforeRender();
	}
}

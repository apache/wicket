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
package wicket.extensions.wizard;

import wicket.MarkupContainer;

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
	 * @param parent
	 * 
	 * @param id
	 *            The component id
	 * @param wizard
	 *            The wizard
	 */
	public LastButton(MarkupContainer parent, final String id, IWizard wizard)
	{
		super(parent, id, wizard, "wicket.extensions.wizard.last");
	}

	/**
	 * @see wicket.Component#isEnabled()
	 */
	@Override
	public final boolean isEnabled()
	{
		return getWizardModel().isLastAvailable();
	}

	/**
	 * @see wicket.Component#isVisible()
	 */
	@Override
	public final boolean isVisible()
	{
		return getWizardModel().isLastVisible();
	}

	/**
	 * @see wicket.extensions.wizard.WizardButton#onClick()
	 */
	@Override
	public final void onClick()
	{
		IWizardModel wizardModel = getWizardModel();
		wizardModel.getActiveStep().applyState();
		wizardModel.lastStep();
	}
}

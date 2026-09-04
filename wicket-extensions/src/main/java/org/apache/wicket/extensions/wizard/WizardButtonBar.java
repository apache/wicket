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

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;

/**
 * The default bar of button components for wizards. This should be good for 90% of the cases. If
 * not, override {@link Wizard#newButtonBar(String)} and provide your own.
 * <p>
 * The button bar holds the {@link PreviousButton previous}, [@link NextButton next},
 * {@link LastButton last}, [@link CancelButton cancel} and {@link FinishButton finish} buttons. The
 * {@link LastButton last button} is off by default. You can turn it on by having the wizard model
 * return true for {@link IWizardModel#isLastVisible() the is last visible method}.
 * </p>
 * 
 * @author Eelco Hillenius
 */
public class WizardButtonBar extends Panel
{
	private static final long serialVersionUID = 1L;

	private final IWizard wizard;

	/**
	 * Construct.
	 * 
	 * @param id
	 *            The component id
	 * @param wizard
	 *            The containing wizard
	 */
	public WizardButtonBar(final String id, final IWizard wizard)
	{
		super(id);
		
		this.wizard = wizard;
	}
	
	@Override
	protected void onInitialize()
	{
		super.onInitialize();

		add(newPreviousButton("previous", wizard));
		add(newNextButton("next", wizard));
		add(newLastButton("last", wizard));
		add(newCancelButton("cancel", wizard));
		add(newFinishButton("finish", wizard));
	}

	@Override
	protected void onBeforeRender()
	{
		super.onBeforeRender();

		WizardButton button = getDefaultButton(wizard.getWizardModel());
		if (button != null) {
			Form<?> form = button.getForm();
			if (form != null) {
				form.setDefaultButton(button);
			}
		}
	}

	public WizardButton getDefaultButton(final IWizardModel model)
	{
		if (model.isNextAvailable())
		{
			return (WizardButton)get("next");
		}
		else if (model.isLastAvailable())
		{
			return (WizardButton)get("last");
		}
		else if (model.isLastStep(model.getActiveStep()))
		{
			return (WizardButton)get("finish");
		}
		return null;
	}
	
	/**
	 * Creates a new button for {@link IWizardModel#previous()}.
	 * 
	 * @param id the button's id
	 * @param wizard the {@link IWizard}
	 * @return a new {@code PreviousButton}
	 */
	protected WizardButton newPreviousButton(final String id, final IWizard wizard)
	{
		return new PreviousButton(id, wizard);
	}

	/**
	 * Creates a new button for {@link IWizardModel#next()}.
	 * 
	 * @param id the button's id
	 * @param wizard the {@link IWizard}
	 * @return a new {@code NextButton}
	 */
	protected WizardButton newNextButton(final String id, final IWizard wizard)
	{
		return new NextButton(id, wizard);
	}

	/**
	 * Creates a new button for {@link IWizardModel#last()}.
	 * 
	 * @param id the button's id
	 * @param wizard the {@link IWizard}
	 * @return a new {@code LastButton}
	 */
	protected WizardButton newLastButton(final String id, final IWizard wizard)
	{
		return new LastButton(id, wizard);
	}

	/**
	 * Creates a new button for {@link IWizardModel#cancel()}.
	 * 
	 * @param id the button's id
	 * @param wizard the {@link IWizard}
	 * @return a new {@code CancelButton}
	 */
	protected WizardButton newCancelButton(final String id, final IWizard wizard)
	{
		return new CancelButton(id, wizard);
	}

	/**
	 * Creates a new button for {@link IWizardModel#finish()}.
	 * 
	 * @param id the button's id
	 * @param wizard the {@link IWizard}
	 * @return a new button
	 */
	protected WizardButton newFinishButton(final String id, final IWizard wizard)
	{
		return new FinishButton(id, wizard);
	}
}

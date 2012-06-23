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

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.model.ResourceModel;

/**
 * Base class for buttons that work with {@link IWizard the wizard component}. It uses resource
 * bundles to display the button label.
 * <p>
 * When wizard buttons are presses (and they pass validation if that is relevant), they pass control
 * to {@link #onClick() their action method}, which should do the real work.
 * </p>
 * 
 * @author Eelco Hillenius
 */
public abstract class WizardButton extends Button
{
	private static final long serialVersionUID = 1L;

	/**
	 * The enclosing wizard.
	 */
	private final IWizard wizard;

	/**
	 * Construct.
	 * 
	 * @param id
	 *            The component id
	 * @param wizard
	 *            The wizard
	 * @param labelResourceKey
	 *            The resource key of the button's label
	 */
	public WizardButton(final String id, final IWizard wizard, final String labelResourceKey)
	{
		super(id, new ResourceModel(labelResourceKey));
		this.wizard = wizard;
		setEscapeModelStrings(false);
	}

	/**
	 * Gets the {@link IWizard}.
	 * 
	 * @return The wizard
	 */
	protected final IWizard getWizard()
	{
		return wizard;
	}

	/**
	 * Gets the {@link IWizardModel wizard model}.
	 * 
	 * @return The wizard model
	 */
	protected final IWizardModel getWizardModel()
	{
		return getWizard().getWizardModel();
	}

	/**
	 * Called when this button is clicked.
	 */
	protected abstract void onClick();

	/**
	 * @see org.apache.wicket.markup.html.form.Button#onSubmit()
	 */
	@Override
	public final void onSubmitBeforeForm()
	{
		onClick();
	}
}

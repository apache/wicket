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
package org.apache.wicket.extensions.wizard.dynamic;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.wizard.IWizard;
import org.apache.wicket.extensions.wizard.WizardStep;
import org.apache.wicket.model.IModel;

/**
 * Default implementation of a {@link IDynamicWizardStep dynamic wizard step}.
 * 
 * @author eelcohillenius
 */
public abstract class DynamicWizardStep extends WizardStep implements IDynamicWizardStep
{
	private static final long serialVersionUID = 1L;

	private final IDynamicWizardStep previousStep;

	/**
	 * Construct without a title and a summary. Useful for when you provide a custom header by
	 * overriding {@link #getHeader(String, Component, IWizard)}.
	 * 
	 * @param previousStep
	 *            The previous step. May be null if this is the first step in the wizard
	 */
	public DynamicWizardStep(final IDynamicWizardStep previousStep)
	{
		super();
		this.previousStep = previousStep;
	}

	/**
	 * Creates a new step with the specified title and summary. The title and summary are displayed
	 * in the wizard title block while this step is active.
	 * 
	 * @param previousStep
	 *            The previous step. May be null if this is the first step in the wizard
	 * @param title
	 *            the title of this step.
	 * @param summary
	 *            a brief summary of this step or some usage guidelines.
	 */
	public DynamicWizardStep(final IDynamicWizardStep previousStep, final IModel<String> title,
		final IModel<String> summary)
	{
		super(title, summary);
		this.previousStep = previousStep;
	}

	/**
	 * Creates a new step with the specified title and summary. The title and summary are displayed
	 * in the wizard title block while this step is active.
	 * 
	 * @param previousStep
	 *            The previous step. May be null if this is the first step in the wizard
	 * @param title
	 *            the title of this step.
	 * @param summary
	 *            a brief summary of this step or some usage guidelines.
	 * @param model
	 *            Any model which is to be used for this step
	 */
	public DynamicWizardStep(final IDynamicWizardStep previousStep, final IModel<String> title,
		final IModel<String> summary, final IModel<?> model)
	{
		super(title, summary, model);
		this.previousStep = previousStep;
	}

	/**
	 * Creates a new step with the specified title and summary. The title and summary are displayed
	 * in the wizard title block while this step is active.
	 * 
	 * @param previousStep
	 *            The previous step. May be null if this is the first step in the wizard
	 * @param title
	 *            the title of this step.
	 * @param summary
	 *            a brief summary of this step or some usage guidelines.
	 */
	public DynamicWizardStep(final IDynamicWizardStep previousStep, final String title,
		final String summary)
	{
		super(title, summary);
		this.previousStep = previousStep;
	}

	/**
	 * Creates a new step with the specified title and summary. The title and summary are displayed
	 * in the wizard title block while this step is active.
	 * 
	 * @param previousStep
	 *            The previous step. May be null if this is the first step in the wizard
	 * @param title
	 *            the title of this step.
	 * @param summary
	 *            a brief summary of this step or some usage guidelines.
	 * @param model
	 *            Any model which is to be used for this step
	 */
	public DynamicWizardStep(final IDynamicWizardStep previousStep, final String title,
		final String summary, final IModel<?> model)
	{
		super(title, summary, model);
		this.previousStep = previousStep;
	}

	/**
	 * @see org.apache.wicket.extensions.wizard.dynamic.IDynamicWizardStep#isLastAvailable()
	 */
	@Override
	public boolean isLastAvailable()
	{
		return false;
	}

	/**
	 * @see org.apache.wicket.extensions.wizard.dynamic.IDynamicWizardStep#isNextAvailable()
	 */
	@Override
	public boolean isNextAvailable()
	{
		return !isLastStep();
	}

	/**
	 * @see org.apache.wicket.extensions.wizard.dynamic.IDynamicWizardStep#isPreviousAvailable()
	 */
	@Override
	public boolean isPreviousAvailable()
	{
		return (previousStep != null);
	}

	/**
	 * @see org.apache.wicket.extensions.wizard.dynamic.IDynamicWizardStep#last()
	 */
	@Override
	public IDynamicWizardStep last()
	{
		throw new IllegalStateException("if the last button is available, this step "
			+ "has to override the last() method and let it return a step");
	}

	/**
	 * @see org.apache.wicket.extensions.wizard.dynamic.IDynamicWizardStep#previous()
	 */
	@Override
	public IDynamicWizardStep previous()
	{
		return previousStep;
	}
}

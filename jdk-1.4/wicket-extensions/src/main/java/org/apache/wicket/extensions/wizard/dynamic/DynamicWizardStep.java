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
	private IDynamicWizardStep previousStep;

	/**
	 * Construct without a title and a summary. Useful for when you provide a
	 * custom header by overiding {@link #getHeader(String, Component, IWizard)}.
	 */
	public DynamicWizardStep()
	{
		super();
	}

	/**
	 * Creates a new step with the specified title and summary. The title and
	 * summary are displayed in the wizard title block while this step is
	 * active.
	 * 
	 * @param title
	 *            the title of this step.
	 * @param summary
	 *            a brief summary of this step or some usage guidelines.
	 */
	public DynamicWizardStep(IModel title, IModel summary)
	{
		super(title, summary);
	}

	/**
	 * Creates a new step with the specified title and summary. The title and
	 * summary are displayed in the wizard title block while this step is
	 * active.
	 * 
	 * @param title
	 *            the title of this step.
	 * @param summary
	 *            a brief summary of this step or some usage guidelines.
	 * @param model
	 *            Any model which is to be used for this step
	 */
	public DynamicWizardStep(IModel title, IModel summary, IModel model)
	{
		super(title, summary, model);
	}

	/**
	 * Creates a new step with the specified title and summary. The title and
	 * summary are displayed in the wizard title block while this step is
	 * active.
	 * 
	 * @param title
	 *            the title of this step.
	 * @param summary
	 *            a brief summary of this step or some usage guidelines.
	 */
	public DynamicWizardStep(String title, String summary)
	{
		super(title, summary);
	}

	/**
	 * Creates a new step with the specified title and summary. The title and
	 * summary are displayed in the wizard title block while this step is
	 * active.
	 * 
	 * @param title
	 *            the title of this step.
	 * @param summary
	 *            a brief summary of this step or some usage guidelines.
	 * @param model
	 *            Any model which is to be used for this step
	 */
	public DynamicWizardStep(String title, String summary, IModel model)
	{
		super(title, summary, model);
	}

	/**
	 * @see org.apache.wicket.extensions.wizard.dynamic.IDynamicWizardStep#isLastAvailable()
	 */
	public boolean isLastAvailable()
	{
		return false;
	}

	/**
	 * @see org.apache.wicket.extensions.wizard.dynamic.IDynamicWizardStep#isNextAvailable()
	 */
	public boolean isNextAvailable()
	{
		return !isLastStep();
	}

	/**
	 * @see org.apache.wicket.extensions.wizard.dynamic.IDynamicWizardStep#isPreviousAvailable()
	 */
	public boolean isPreviousAvailable()
	{
		return (previousStep != null);
	}

	/**
	 * @see org.apache.wicket.extensions.wizard.dynamic.IDynamicWizardStep#last()
	 */
	public IDynamicWizardStep last()
	{
		throw new IllegalStateException("if the last button is available, this step "
				+ "has to override the last() method and let it return a step");
	}

	/**
	 * @see org.apache.wicket.extensions.wizard.dynamic.IDynamicWizardStep#previous()
	 */
	public IDynamicWizardStep previous()
	{
		return previousStep;
	}
}

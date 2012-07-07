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

import java.util.Iterator;

import org.apache.wicket.extensions.wizard.AbstractWizardModel;
import org.apache.wicket.extensions.wizard.IWizardStep;
import org.apache.wicket.extensions.wizard.WizardModel;

/**
 * Wizard model that is specialized on dynamic wizards. Unlike the default, static
 * {@link WizardModel wizard model}, this model isn't very intelligent, but rather delegates much of
 * the work and knowledge to the {@link IDynamicWizardStep dynamic wizard steps} it uses.
 * 
 * @author eelcohillenius
 */
public class DynamicWizardModel extends AbstractWizardModel
{
	private static final long serialVersionUID = 1L;

	/**
	 * The current step. The only step that matters really,
	 */
	private IDynamicWizardStep activeStep;

	/**
	 * Remember the first step for resetting the wizard.
	 */
	private final IDynamicWizardStep startStep;

	/**
	 * Construct.
	 * 
	 * @param startStep
	 *            first step in the wizard
	 */
	public DynamicWizardModel(final IDynamicWizardStep startStep)
	{
		this.startStep = startStep;
	}

	/**
	 * @see org.apache.wicket.extensions.wizard.IWizardModel#getActiveStep()
	 */
	@Override
	public IWizardStep getActiveStep()
	{
		return activeStep;
	}

	/**
	 * @return the step this wizard was constructed with (starts the wizard). Will be used for
	 *         resetting the wizard, unless you override {@link #reset()}.
	 */
	public final IDynamicWizardStep getStartStep()
	{
		return startStep;
	}

	/**
	 * @see org.apache.wicket.extensions.wizard.IWizardModel#isLastAvailable()
	 */
	@Override
	public boolean isLastAvailable()
	{
		return activeStep.isLastAvailable();
	}

	/**
	 * @see org.apache.wicket.extensions.wizard.IWizardModel#isLastStep(org.apache.wicket.extensions.wizard.IWizardStep)
	 */
	@Override
	public boolean isLastStep(final IWizardStep step)
	{
		return ((IDynamicWizardStep)step).isLastStep();
	}

	/**
	 * @see org.apache.wicket.extensions.wizard.IWizardModel#isNextAvailable()
	 */
	@Override
	public boolean isNextAvailable()
	{
		return activeStep.isNextAvailable();
	}

	/**
	 * @see org.apache.wicket.extensions.wizard.IWizardModel#isPreviousAvailable()
	 */
	@Override
	public boolean isPreviousAvailable()
	{
		return activeStep.isPreviousAvailable();
	}

	/**
	 * @see org.apache.wicket.extensions.wizard.IWizardModel#last()
	 */
	@Override
	public void last()
	{
		setActiveStep(activeStep.last());
	}

	/**
	 * @see org.apache.wicket.extensions.wizard.IWizardModel#next()
	 */
	@Override
	public void next()
	{
		setActiveStep(activeStep.next());
	}

	/**
	 * @see org.apache.wicket.extensions.wizard.IWizardModel#previous()
	 */
	@Override
	public void previous()
	{
		setActiveStep(activeStep.previous());
	}

	/**
	 * @see org.apache.wicket.extensions.wizard.IWizardModel#reset()
	 */
	@Override
	public void reset()
	{
		setActiveStep(startStep);
	}

	/**
	 * @see org.apache.wicket.extensions.wizard.IWizardModel#stepIterator()
	 */
	@Override
	public Iterator<IWizardStep> stepIterator()
	{
		return null;
	}

	/**
	 * Sets the active step.
	 * 
	 * @param step
	 *            the new active step step.
	 */
	protected final void setActiveStep(final IDynamicWizardStep step)
	{
		if (step == null)
		{
			throw new IllegalArgumentException("argument step must to be not null");
		}

		step.init(this);
		activeStep = step;

		fireActiveStepChanged(step);
	}
}

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.util.io.IClusterable;
import org.apache.wicket.util.collections.ArrayListStack;


/**
 * Default implementation of {@link IWizardModel}, which models a semi-static wizard. This means
 * that all steps should be known upfront, and added to the model on construction. Steps can be
 * optional by using {@link ICondition}. The wizard is initialized with a wizard model through
 * calling method {@link Wizard#init(IWizardModel)}.
 * <p>
 * Steps can be added to this model directly using either the {@link #add(IWizardStep) normal add
 * method} or {@link #add(IWizardStep, ICondition) the conditional add method}.
 * </p>
 * 
 * <p>
 * <a href="https://wizard-framework.dev.java.net/">Swing Wizard Framework</a> served as a valuable
 * source of inspiration.
 * </p>
 * 
 * @author Eelco Hillenius
 */
public class WizardModel extends AbstractWizardModel
{
	/**
	 * Interface for conditional displaying of wizard steps.
	 */
	public interface ICondition extends IClusterable
	{
		/**
		 * Evaluates the current state and returns whether the step that is coupled to this
		 * condition is available.
		 * 
		 * @return True if the step this condition is coupled to is available, false otherwise
		 */
		public boolean evaluate();
	}

	/**
	 * Condition that always evaluates true.
	 */
	public static final ICondition TRUE = new ICondition()
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Always returns true.
		 * 
		 * @return True
		 */
		@Override
		public boolean evaluate()
		{
			return true;
		}
	};

	private static final long serialVersionUID = 1L;

	/** The currently active step. */
	private IWizardStep activeStep;

	/** Conditions with steps. */
	private final List<ICondition> conditions = new ArrayList<ICondition>();

	/** State history. */
	private final ArrayListStack<IWizardStep> history = new ArrayListStack<IWizardStep>();

	/** The wizard steps. */
	private final List<IWizardStep> steps = new ArrayList<IWizardStep>();

	/**
	 * Construct.
	 */
	public WizardModel()
	{
	}

	/**
	 * Adds the next step to the wizard. If the {@link WizardStep} implements {@link ICondition},
	 * then this method is equivalent to calling {@link #add(IWizardStep, ICondition) add(step,
	 * (ICondition)step)}.
	 * 
	 * @param step
	 *            the step to added.
	 */
	public void add(final IWizardStep step)
	{
		if (step instanceof ICondition)
		{
			add(step, (ICondition)step);
		}
		else
		{
			add(step, TRUE);
		}
	}

	/**
	 * Adds an optional step to the model. The step will only be displayed if the specified
	 * condition is met.
	 * 
	 * @param step
	 *            The step to add
	 * @param condition
	 *            the {@link ICondition} under which it should be included in the wizard.
	 */
	public void add(final IWizardStep step, final ICondition condition)
	{
		steps.add(step);
		conditions.add(condition);
	}

	/**
	 * Gets the current active step the wizard should display.
	 * 
	 * @return the active step.
	 */
	@Override
	public final IWizardStep getActiveStep()
	{
		return activeStep;
	}

	/**
	 * Checks if the last button should be enabled.
	 * 
	 * @return <tt>true</tt> if the last button should be enabled, <tt>false</tt> otherwise.
	 * @see IWizardModel#isLastVisible
	 */
	@Override
	public boolean isLastAvailable()
	{
		return allStepsComplete() && !isLastStep(activeStep);
	}

	/**
	 * @see org.apache.wicket.extensions.wizard.IWizardModel#isLastStep(org.apache.wicket.extensions.wizard.IWizardStep)
	 */
	@Override
	public boolean isLastStep(final IWizardStep step)
	{
		return findLastStep().equals(step);
	}

	/**
	 * Checks if the next button should be enabled.
	 * 
	 * @return <tt>true</tt> if the next button should be enabled, <tt>false</tt> otherwise.
	 */
	@Override
	public boolean isNextAvailable()
	{
		return activeStep.isComplete() && !isLastStep(activeStep);
	}

	/**
	 * Checks if the previous button should be enabled.
	 * 
	 * @return <tt>true</tt> if the previous button should be enabled, <tt>false</tt> otherwise.
	 */
	@Override
	public boolean isPreviousAvailable()
	{
		return !history.isEmpty();
	}

	/**
	 * @see org.apache.wicket.extensions.wizard.IWizardModel#last()
	 */
	@Override
	public void last()
	{
		history.push(getActiveStep());
		IWizardStep lastStep = findLastStep();
		setActiveStep(lastStep);
	}

	/**
	 * @see org.apache.wicket.extensions.wizard.IWizardModel#next()
	 */
	@Override
	public void next()
	{
		history.push(getActiveStep());
		IWizardStep step = findNextVisibleStep();
		setActiveStep(step);
	}

	/**
	 * @see org.apache.wicket.extensions.wizard.IWizardModel#previous()
	 */
	@Override
	public void previous()
	{
		IWizardStep step = history.pop();
		setActiveStep(step);
	}

	/**
	 * @see org.apache.wicket.extensions.wizard.IWizardModel#reset()
	 */
	@Override
	public void reset()
	{
		history.clear();
		activeStep = null;
		
		for (IWizardStep step : steps)
		{
			step.init(this);
		}

		setActiveStep(findNextVisibleStep());
	}

	/**
	 * Sets the active step.
	 * 
	 * @param step
	 *            the new active step step.
	 */
	public void setActiveStep(final IWizardStep step)
	{
		if ((activeStep != null) && (step != null) && activeStep.equals(step))
		{
			return;
		}

		activeStep = step;

		fireActiveStepChanged(step);
	}

	/**
	 * @see IWizardModel#stepIterator()
	 */
	@Override
	public final Iterator<IWizardStep> stepIterator()
	{
		return steps.iterator();
	}

	/**
	 * Returns true if all the steps in the wizard return <tt>true</tt> from
	 * {@link IWizardStep#isComplete}. This is primarily used to determine if the last button can be
	 * enabled.
	 * 
	 * @return <tt>true</tt> if all the steps in the wizard are complete, <tt>false</tt> otherwise.
	 */
	protected final boolean allStepsComplete()
	{
		for (IWizardStep step : steps)
		{
			if (!step.isComplete())
			{
				return false;
			}
		}

		return true;
	}

	/**
	 * Finds the last step in this model.
	 * 
	 * @return The last step
	 */
	protected final IWizardStep findLastStep()
	{
		for (int i = conditions.size() - 1; i >= 0; i--)
		{
			ICondition condition = conditions.get(i);
			if (condition.evaluate())
			{
				return steps.get(i);
			}
		}

		throw new IllegalStateException("Wizard contains no visible steps");
	}

	/**
	 * Finds the next visible step based on the active step.
	 * 
	 * @return The next visible step based on the active step
	 */
	protected final IWizardStep findNextVisibleStep()
	{
		int startIndex = (activeStep == null) ? 0 : steps.indexOf(activeStep) + 1;

		for (int i = startIndex; i < conditions.size(); i++)
		{
			ICondition condition = conditions.get(i);
			if (condition.evaluate())
			{
				return steps.get(i);
			}
		}

		throw new IllegalStateException("Wizard contains no more visible steps");
	}

	/**
	 * Gets conditions.
	 * 
	 * @return unmodifiable list of conditions
	 */
	public List<ICondition> getConditions()
	{
		return Collections.unmodifiableList(conditions);
	}

}
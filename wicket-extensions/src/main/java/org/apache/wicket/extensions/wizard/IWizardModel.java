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

import java.util.Iterator;

import org.apache.wicket.util.io.IClusterable;

/**
 * This interface defines the model for wizards. This model knows about the wizard's steps and the
 * transitions between them, and it holds a reference to the currently active step. It might
 * function as a generic state holder for the wizard too, though you might find it more convenient
 * to use the wizard component itself for that, or even an external model.
 * 
 * <p>
 * {@link IWizardModelListener wizard model listeners} can be registered to be notified of important
 * events (changing the active step) using the {@link #addListener(IWizardModelListener) add
 * listener} method.
 * </p>
 * 
 * <p>
 * Typically, you would use {@link WizardModel the default implementation of this interface}, but if
 * you need to do more sophisticated stuff, like branching etc, you can consider creating your own
 * implementation. In that case, it is recommended you start by extending from
 * {@link AbstractWizardModel}.
 * </p>
 * 
 * <p>
 * <a href="https://wizard-framework.dev.java.net/">Swing Wizard Framework</a> served as a valuable
 * source of inspiration.
 * </p>
 * 
 * @see AbstractWizardModel
 * @see WizardModel
 * 
 * @author Eelco Hillenius
 */
public interface IWizardModel extends IClusterable
{
	/**
	 * Adds a wizard model listener.
	 * 
	 * @param listener
	 *            The wizard model listener to add
	 */
	void addListener(IWizardModelListener listener);

	/**
	 * Cancels further processing. Implementations may clean up and reset the model. Implementations
	 * should notify the registered {@link IWizardModelListener#onCancel() model listeners}.
	 */
	void cancel();

	/**
	 * Instructs the wizard to finish succesfully. Typically, implementations check whether this
	 * option is available at all. Implementations may clean up and reset the model. Implementations
	 * should notify the registered {@link IWizardModelListener#onFinish() model listeners}.
	 */
	void finish();

	/**
	 * Gets the current active step the wizard should display.
	 * 
	 * @return the active step.
	 */
	IWizardStep getActiveStep();

	/**
	 * Gets whether the cancel button should be displayed.
	 * 
	 * @return True if the cancel button should be displayed
	 */
	boolean isCancelVisible();

	/**
	 * Checks if the last button should be enabled.
	 * 
	 * @return <tt>true</tt> if the last button should be enabled, <tt>false</tt> otherwise.
	 * @see #isLastVisible
	 */
	boolean isLastAvailable();

	/**
	 * Gets whether the specified step is the last step in the wizard.
	 * 
	 * @param step
	 *            the step to check
	 * @return True if its the final step in the wizard, false< otherwise.
	 */
	boolean isLastStep(IWizardStep step);

	/**
	 * Gets whether the last button should be displayed. This method should only return true if the
	 * {@link #isLastAvailable} will return true at any point. Returning false will prevent the last
	 * button from appearing on the wizard at all.
	 * 
	 * @return True if the last button should be displayed, False otherwise.
	 */
	boolean isLastVisible();

	/**
	 * Gets whether the next button should be enabled.
	 * 
	 * @return True if the next button should be enabled, false otherwise.
	 */
	boolean isNextAvailable();

	/**
	 * Gets whether the previous button should be enabled.
	 * 
	 * @return True if the previous button should be enabled, false otherwise.
	 */
	boolean isPreviousAvailable();

	/**
	 * Takes the model to the last step in the wizard. This method must only be called if
	 * {@link #isLastAvailable} returns <tt>true</tt>. Implementors should notify
	 * {@link IWizardModelListener listeners} through calling
	 * {@link IWizardModelListener#onActiveStepChanged(IWizardStep)}.
	 */
	void last();

	/**
	 * Increments the model to the next step. This method must only be called if
	 * {@link #isNextAvailable} returns <tt>true</tt>. Implementors should notify
	 * {@link IWizardModelListener listeners} through calling
	 * {@link IWizardModelListener#onActiveStepChanged(IWizardStep)}.
	 */
	void next();

	/**
	 * Takes the model to the previous step.This method must only be called if
	 * {@link #isPreviousAvailable} returns <tt>true</tt>. Implementors should notify
	 * {@link IWizardModelListener listeners} through calling
	 * {@link IWizardModelListener#onActiveStepChanged(IWizardStep)}.
	 */
	void previous();

	/**
	 * Removes a wizard model listener.
	 * 
	 * @param listener
	 *            The listener to remove
	 */
	void removeListener(IWizardModelListener listener);

	/**
	 * Resets the model, setting it to the first step. Implementors should notify
	 * {@link IWizardModelListener listeners} through calling
	 * {@link IWizardModelListener#onActiveStepChanged(IWizardStep)}.
	 */
	void reset();

	/**
	 * Returns an iterator over all the steps in the model. The iteration order is not guaranteed to
	 * the be the order of visit. This is an optional operation; dynamic models can just return
	 * null, and should call init the first time a step is encountered right before rendering it.
	 * 
	 * @return an iterator over all the steps of the model or null if the wizard model is not static
	 */
	Iterator<IWizardStep> stepIterator();
}

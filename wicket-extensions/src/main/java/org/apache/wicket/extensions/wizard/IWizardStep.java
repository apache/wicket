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

import org.apache.wicket.Component;
import org.apache.wicket.util.io.IClusterable;

/**
 * Models one step in a wizard, and is the equivalent of one panel in a wizard from an end-user's
 * perspective.
 * <p>
 * Typically, you would extend {@link WizardStep panel based wizard steps} and provide a custom
 * panel for the step instead of directly implementing this interface.
 * </p>
 * 
 * <p>
 * <a href="https://wizard-framework.dev.java.net/">Swing Wizard Framework</a> served as a valuable
 * source of inspiration.
 * </p>
 * 
 * @author Eelco Hillenius
 */
public interface IWizardStep extends IClusterable
{
	/**
	 * Initializes this step with the model it will belong to.
	 * <p>
	 * This method is called at least once before this step becomes the actual step.
	 * 
	 * @param wizardModel
	 *            the owning wizard model
	 */
	void init(IWizardModel wizardModel);
	
	/**
	 * Gets the header component for this step. This component is displayed in a special section of
	 * the wizard.
	 * <p>
	 * This method is called every time this step becomes the active step of the wizard.
	 * 
	 * @param id
	 *            The id that the component should be created with
	 * @param parent
	 *            The parent component (for post 1.2)
	 * @param wizard
	 *            The wizard component the header will be placed on
	 * @return The header component
	 */
	Component getHeader(String id, Component parent, IWizard wizard);

	/**
	 * Returns the current view this step is displaying. This component will be displayed in the
	 * main section of the wizard.
	 * <p>
	 * This method is called every time this step becomes the active step of the wizard.
	 * 
	 * @param id
	 *            The id that the component should be created with
	 * @param parent
	 *            The parent component (for post 1.2)
	 * @param wizard
	 *            The wizard component the header will be placed on
	 * @return The current view of the step.
	 */
	Component getView(String id, Component parent, IWizard wizard);

	/**
	 * This method is called whenever the wizard proceeds from this step to another step. It is not
	 * called when returning to a previous step.
	 * 
	 * @see IWizardModel#next() 
	 * @see IWizardModel#last() 
	 * @see IWizardModel#finish() 
	 */
	void applyState();

	/**
	 * Checks if this step is complete. This method should return {@code true} if the wizard can
	 * proceed to the next step.
	 * 
	 * @return {@code true} if the wizard can proceed from this step, {@code false} otherwise.
	 * 
	 * @see IWizardModel#next() 
	 * @see IWizardModel#last() 
	 * @see IWizardModel#finish() 
	 */
	boolean isComplete();
}

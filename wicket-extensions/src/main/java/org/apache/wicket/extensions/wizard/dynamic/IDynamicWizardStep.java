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

import org.apache.wicket.extensions.wizard.IWizardModel;
import org.apache.wicket.extensions.wizard.IWizardStep;

/**
 * Wizard step that is intelligent enough to know how to navigate to the next and previous steps.
 * Using such steps, you can build wizard that consists of steps that are linked on the fly rather
 * than in a static, pre-determined fashion. The basic idea here is that the wizard step takes over
 * much of what otherwise would be done by the wizard model. You trade simplicity for flexibility.
 * 
 * <p>
 * Warning: only use these steps with the {@link DynamicWizardModel}.
 * </p>
 * 
 * @author eelcohillenius
 */
public interface IDynamicWizardStep extends IWizardStep
{
	/**
	 * Checks if the last button should be enabled.
	 * 
	 * @return <tt>true</tt> if the last button should be enabled, <tt>false</tt> otherwise.
	 * 
	 * @see IWizardModel#isLastAvailable()
	 */
	boolean isLastAvailable();

	/**
	 * Gets whether this is the last step in the wizard.
	 * 
	 * @return True if its the final step in the wizard, false< otherwise.
	 * 
	 * @see IWizardModel#isLastStep(IWizardStep)
	 */
	boolean isLastStep();

	/**
	 * Gets whether the next button should be enabled.
	 * 
	 * @return True if the next button should be enabled, false otherwise.
	 * 
	 * @see IWizardModel#isNextAvailable()
	 */
	boolean isNextAvailable();

	/**
	 * Gets whether the previous button should be enabled.
	 * 
	 * @return True if the previous button should be enabled, false otherwise.
	 * 
	 * @see IWizardModel#isPreviousAvailable()
	 */
	boolean isPreviousAvailable();

	/**
	 * Gets whether the finish button should be enabled.
	 * 
	 * @return True if the finish button should be enabled, false otherwise.
	 * 
	 * @see IWizardModel#isFinishAvailable()
	 */
	default boolean isFinishAvailable() {
		return isLastStep();
	}

	/**
	 * Gets the next wizard step from here. Can only be called when
	 * {@link DynamicWizardModel#isLastAvailable()} returns true.
	 * 
	 * @return The next wizard step. May not be null.
	 */
	IDynamicWizardStep last();

	/**
	 * Gets the next wizard step from here. Can only be called when {@link #isNextAvailable()}
	 * returns true.
	 * 
	 * @return The next wizard step. May not be null unless this is the last step (
	 *         {@link #isLastStep()} returns true).
	 */
	IDynamicWizardStep next();

	/**
	 * Gets the previous wizard step from here. Can only be called when
	 * {@link #isPreviousAvailable()} returns true.
	 * 
	 * @return The next wizard step. May not be null unless this is the first step (in which case it
	 *         should never be called).
	 */
	IDynamicWizardStep previous();
}

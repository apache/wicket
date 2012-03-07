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

import org.apache.wicket.util.io.IClusterable;

/**
 * Recieves notifications for wizard model state changes.
 * 
 * @author Eelco Hillenius
 */
public interface IWizardModelListener extends IClusterable
{
	/**
	 * Called when the active step of this model changed.
	 * 
	 * @param newStep
	 *            The new step
	 */
	void onActiveStepChanged(IWizardStep newStep);

	/**
	 * Called when the wizard was cancelled.
	 */
	void onCancel();

	/**
	 * Called when the wizard finished succesfully.
	 */
	void onFinish();
}
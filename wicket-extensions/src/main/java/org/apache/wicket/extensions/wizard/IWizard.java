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
 * Interface for the wizard component. This interface is here for ultimate flexibility, though it
 * doesn't guarantee much. Typically, you would extend from {@link Wizard the default wizard
 * component} instead of implementing this interface directly.
 * 
 * <p>
 * Part of the contract of wizards is that {@link IWizardStep wizard steps} are initialized when the
 * wizard receives the {@link IWizardModel wizard model}.
 * </p>
 * 
 * @author Eelco Hillenius
 */
public interface IWizard extends IClusterable
{
	/**
	 * Gets the model this wizard is using. This should never be null; a wizard is supposed to have
	 * a model.
	 * 
	 * @return The wizard model.
	 */
	IWizardModel getWizardModel();
}
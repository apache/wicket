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
/**
 * 
 */
package org.apache.wicket.extensions.wizard;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IFormSubmittingComponent;

/**
 * Optional interface that can be implemented by button bars if they can provide what button should
 * be the 'default' one - the one that is selected when a user presses enter. Note that this is not
 * completely water proof as it depends on non-standard browser behavior (i.e. the fact that most
 * browsers select the first button they encounter).
 * 
 * @see Form#setDefaultButton(IFormSubmittingComponent)
 * 
 * @author eelcohillenius
 */
@Deprecated
public interface IDefaultButtonProvider
{
	/**
	 * Gets the default button - the button that is selected when a user presses enter - based on
	 * the current state.
	 * 
	 * @param model
	 *            wizard model
	 * @return the default button
	 */
	IFormSubmittingComponent getDefaultButton(IWizardModel model);
}

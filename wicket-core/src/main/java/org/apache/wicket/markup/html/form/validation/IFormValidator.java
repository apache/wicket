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
package org.apache.wicket.markup.html.form.validation;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.util.io.IClusterable;

/**
 * Interface representing validators that check multiple components. These validators are added
 * to a form and are executed only if all form components returned by
 * {@link IFormValidator#getDependentFormComponents()} have been successfully validated before this
 * validator is executed. The validator is also executed if the dependent form components
 * return null or an empty array.
 *
 * @see AbstractFormValidator
 * @see org.apache.wicket.validation.IValidator
 * @author Igor Vaynberg (ivaynberg)
 */
public interface IFormValidator extends IClusterable
{
	/**
	 * @return array of {@link FormComponent}s that this validator depends on
	 */
	FormComponent<?>[] getDependentFormComponents();

	/**
	 * This method is executed if there are no dependent form components, or if all components
	 * returned by {@link IFormValidator#getDependentFormComponents()} are valid and visible.
	 * If the dependent form components array is not empty, at least one of the components
	 * must be enabled.
	 *
	 * <p>
	 * To report validation error use
	 * {@link FormComponent#error(org.apache.wicket.validation.IValidationError)} by using any of
	 * the dependent form components or extend from AbstractFormValidator and use its
	 * {@link AbstractFormValidator#error(FormComponent, String, java.util.Map)} method.
	 * 
	 * @param form
	 *            form this validator is added to
	 */
	void validate(Form<?> form);
}

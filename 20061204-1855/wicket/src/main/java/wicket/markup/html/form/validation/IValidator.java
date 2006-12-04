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
package wicket.markup.html.form.validation;

import java.io.Serializable;

import wicket.markup.html.form.FormComponent;

/**
 * Interface for validations by form components.
 * <p>
 * Instead of subclassing IValidator, you should use one of the existing
 * validators, which cover a huge number of cases, or if none satisfies your
 * need, subclass one of the Type validators like {@link StringValidator},
 * {@link NumberValidator} or {@link DateValidator}
 * <p>
 * Interface to code that validates Form components. When the validate() method
 * of the interface is called by the framework, the IValidator implementation is
 * expected to check the input String it is passed.
 * 
 * @author Jonathan Locke
 */
public interface IValidator extends Serializable
{
	/**
	 * <p>
	 * Instead of subclassing IValidator, you should use one of the existing
	 * validators, which cover a huge number of cases, or if none satisfies your
	 * need, subclass one of the Type validators like {@link StringValidator},
	 * {@link NumberValidator} or {@link DateValidator}
	 * <p>
	 * Validates the given input. The input corresponds to the input from the
	 * request for a component.
	 * 
	 * @param component
	 *            Component to validate
	 */
	void validate(final FormComponent component);
}

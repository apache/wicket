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
package org.apache.wicket.validation;

import org.apache.wicket.util.io.IClusterable;

/**
 * Interface representing a validator that can validate an {@link IValidatable} object.
 * <p>
 * Unless the validator implements the {@link INullAcceptingValidator} interface as well, Wicket
 * will not pass <code>null</code> values to the {@link IValidator#validate(IValidatable)} method.
 * 
 * @author Jonathan Locke
 * @author Igor Vaynberg (ivaynberg)
 * @param <T>
 *            type of validatable value
 * @since 1.2.6
 */
public interface IValidator<T> extends IClusterable
{
	/**
	 * Validates the <code>IValidatable</code> instance. Validation errors should be reported using
	 * the {@link IValidatable#error(IValidationError)} method.
	 * 
	 * @param validatable
	 *            the <code>IValidatable</code> instance being validated
	 */
	void validate(IValidatable<T> validatable);
}

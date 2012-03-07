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
 * Interface representing a validation error.
 * 
 * @author Igor Vaynberg (ivaynberg)
 * @since 1.2.6
 */
public interface IValidationError extends IClusterable
{
	/**
	 * Retrieves the error message (usually user-facing).
	 * 
	 * @param messageSource
	 *            the message source
	 * @return the error message <code>String</code>
	 */
	String getErrorMessage(IErrorMessageSource messageSource);
}

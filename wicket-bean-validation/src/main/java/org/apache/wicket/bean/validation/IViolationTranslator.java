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
package org.apache.wicket.bean.validation;

import jakarta.validation.ConstraintViolation;

import org.apache.wicket.validation.ValidationError;

/**
 * Converts {@link ConstraintViolation}s into Wicket's {@link ValidationError}s
 * 
 * @author igor
 * @see BeanValidationConfiguration#setViolationTranslator(org.apache.wicket.bean.validation.IViolationTranslator)
 */
@FunctionalInterface
public interface IViolationTranslator
{
	/**
	 * Converts a {@link ConstraintViolation} into a {@link ValidationError}
	 * 
	 * @param <T>
	 * @param violation
	 *            constraint violation from bean validation
	 * @return validation validation error to be reported on the component
	 */
	<T> ValidationError convert(ConstraintViolation<T> violation);
}

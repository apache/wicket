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

import org.apache.wicket.model.IModel;


/**
 * Interface representing any object that can be validated.
 * 
 * @author Igor Vaynberg (ivaynberg)
 * @param <T>
 *            type of value
 * @since 1.2.6
 */
public interface IValidatable<T>
{
	/**
	 * Retrieves the value to be validated.
	 * 
	 * @return the value to be validated
	 */
	T getValue();

	/**
	 * Reports an error against this <code>IValidatable</code>'s value. Multiple errors can be
	 * reported by calling this method multiple times.
	 * 
	 * @param error
	 *            an <code>IValidationError</code> to be reported
	 */
	void error(IValidationError error);

	/**
	 * Queries the current state of this <code>IValidatable</code> instance.
	 * <code>IValidatable</code>s should assume they are valid until
	 * {@link #error(IValidationError)} is called.
	 * 
	 * @return <code>true</code> if the object is in a valid state, <code>false</code> if otherwise
	 */
	boolean isValid();

	/**
	 * Returns the model of the component being validated
	 * 
	 * @return component's model
	 */
	IModel<T> getModel();
}

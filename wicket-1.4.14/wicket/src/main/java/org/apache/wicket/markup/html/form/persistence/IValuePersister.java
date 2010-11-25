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
package org.apache.wicket.markup.html.form.persistence;

import org.apache.wicket.IClusterable;
import org.apache.wicket.markup.html.form.FormComponent;

/**
 * Wicket users and developers should not need to care about where or how form values are saved. An
 * implementer of IValuePersister persister is responsible for storing and retrieving FormComponent
 * values. Different means of storing values for form components may be implemented.
 * CookieValuePersister, for example, uses an HTTP cookie to persist the value of a form component.
 * Other implementations may instead persist form values to server-side storage for security
 * reasons.
 * 
 * @author Juergen Donnerstag
 * @author Jonathan Locke
 * 
 * @deprecated remove in 1.5
 */
@Deprecated
public interface IValuePersister extends IClusterable
{
	/**
	 * Saves the key/value pair
	 * 
	 * @param key
	 *            The key to identify the entry
	 * @param value
	 *            The value
	 */
	void save(String key, String value);

	/**
	 * Convenience method for FormComponent. key defaults to formComponent.getPageRelativePath() and
	 * value to formComponent.getDefaultModelAsString()
	 * 
	 * @param formComponent
	 */
	void save(FormComponent<?> formComponent);

	/**
	 * Retrieve the key from the persistence store
	 * 
	 * @param key
	 *            The key to identify the entry
	 * @return The loaded value
	 */
	String load(String key);

	/**
	 * Retrieve the persisted value and if found update the form components model
	 * 
	 * @param formComponent
	 */
	void load(FormComponent<?> formComponent);

	/**
	 * Remove the key/value pair
	 * 
	 * @param key
	 *            The key to identify the entry
	 */
	void clear(String key);

	/**
	 * Remove the key/value associated witht the formComponent
	 * 
	 * @param formComponent
	 */
	void clear(FormComponent<?> formComponent);
}

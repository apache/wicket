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
package wicket.markup.html.form.persistence;

import java.io.Serializable;

import wicket.markup.html.form.FormComponent;

/**
 * Wicket users and developers should not need to care about where or how form
 * values are saved. An implementer of IValuePersister persister is responsible
 * for storing and retrieving FormComponent values. Different means of storing
 * values for form components may be implemented. CookieValuePersister, for
 * example, uses an HTTP cookie to persist the value of a form component. Other
 * implementations may instead persist form values to server-side storage for
 * security reasons.
 * 
 * @author Juergen Donnerstag
 * @author Jonathan Locke
 */
public interface IValuePersister extends Serializable
{
	/**
	 * Saves the current value of the given form component
	 * 
	 * @param component
	 *            The form component
	 */
	void save(FormComponent component);

	/**
	 * Loads any persisted value for a given form component
	 * 
	 * @param component
	 *            The form component
	 */
	void load(FormComponent component);

	/**
	 * Remove any persisted value for a given form component.
	 * 
	 * @param component
	 *            The form component
	 */
	void clear(FormComponent component);
}


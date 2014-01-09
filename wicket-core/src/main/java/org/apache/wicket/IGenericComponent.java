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
package org.apache.wicket;

import org.apache.wicket.model.IModel;

/**
 * An interface for all {@link Component components} with type-safe accessors and mutators
 * for the model and its object.
 *
 * @param <T>
 *     the type of the model object
 */
public interface IGenericComponent<T>
{
	/**
	 * Typesafe getter for the model
	 *
	 * @return the model
	 */
	IModel<T> getModel();

	/**
	 * Typesafe setter for the model
	 *
	 * @param model
	 *            the new model
	 */
	void setModel(IModel<T> model);

	/**
	 * Typesafe setter for the model object
	 *
	 * @param object
	 *            the new model object
	 */
	void setModelObject(T object);

	/**
	 * Typesafe getter for the model's object
	 *
	 * @return the model object
	 */
	T getModelObject();
} 
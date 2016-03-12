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
package org.apache.wicket.model;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Optional interface implemented by models that are able to provide reflection information about
 * object property they interact with.
 * 
 * The model doesn't have to support all property information in this interface. It is valid to
 * return null for any method.
 * 
 * @author Matej Knopp
 * @param <T>
 */
public interface IPropertyReflectionAwareModel<T> extends IModel<T>
{
	/**
	 * Returns the field of model property or null if the field doesn't exist.
	 * 
	 * @return Field or null
	 */
	Field getPropertyField();

	/**
	 * Returns the getter method of model property or null if the method doesn't exist.
	 * 
	 * @return Method or null
	 */
	Method getPropertyGetter();

	/**
	 * Returns the setter method of model property or null if the method doesn't exist.
	 * 
	 * @return Method or null
	 */
	Method getPropertySetter();
}

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
package org.apache.wicket.injection;

import java.lang.reflect.Field;

/**
 * Factory object used by injector to generate values for fields of the object being injected.
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public interface IFieldValueFactory
{
	/**
	 * Returns the value the field will be set to
	 * 
	 * @param field
	 *            field being injected
	 * @param fieldOwner
	 *            instance of object being injected
	 * 
	 * @return new field value
	 */
	Object getFieldValue(Field field, Object fieldOwner);

	/**
	 * Returns true if the factory can generate a value for the field, false otherwise.
	 * 
	 * If this method returns false, getFieldValue() will not be called on this factory
	 * 
	 * @param field
	 *            field
	 * @return true if the factory can generate a value for the field, false otherwise
	 */
	boolean supportsField(Field field);
}

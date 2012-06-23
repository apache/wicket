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

import java.util.Map;

/**
 * Interface representing a message source that stores messages by key and can perform variable
 * substitution.
 * 
 * @author Igor Vaynberg (ivaynberg)
 * @since 1.2.6
 */
public interface IErrorMessageSource
{
	/**
	 * Retrieves a message with the given <code>key</code>.
	 * 
	 * @param key
	 *            a message key
	 * @param vars
	 *            variables to substitute
	 * @return message or null if not found
	 */
	String getMessage(String key, Map<String, Object> vars);
}
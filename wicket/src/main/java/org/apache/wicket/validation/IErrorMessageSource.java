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
	 * @return message or null if not found
	 */
	String getMessage(String key);

	/**
	 * Performs variable substitution on the given <code>String</code> using variables declared in
	 * the <code>vars</code> <code>Map</code>.
	 * <p>
	 * Variables in the message are identified using <code>${varname}</code> syntax.
	 * 
	 * @param string
	 *            a <code>String</code> to be altered
	 * @param vars
	 *            a <code>Map</code> of variables to process
	 * 
	 * @throws IllegalStateException
	 *             if a variable defined in the given <code>String</code> cannot be found in the
	 *             <code>vars</code> <code>Map</code>
	 * 
	 * @return the given <code>String</code> with the variables substituted
	 */
	// FIXME 2.0 this doesnt need to be in this interface, its just a straight var substitution
	String substitute(String string, Map<String, Object> vars) throws IllegalStateException;
}

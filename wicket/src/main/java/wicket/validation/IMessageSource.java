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
package wicket.validation;

import java.util.Map;


/**
 * Represents a message source that stores messages by a <code>key</code> and
 * can perform variable substitution.
 * 
 * @author ivaynberg
 */
public interface IMessageSource
{
	/**
	 * Retrieves a message with the given <code>key</code>.
	 * 
	 * @param key
	 *            message key
	 * @return message or null if not found
	 */
	String getMessage(String key);

	/**
	 * Performs variable substitution on the specified <code>string</code>
	 * using variables declared in the <code>vars</code> map.
	 * 
	 * Variables in the message are identified using ${varname} syntax
	 * 
	 * @param string
	 * @param vars
	 * 
	 * @throws IllegalStateException
	 *             if a variable defined in the string cannot be found in the
	 *             <code>vars</code> map
	 * 
	 * @return string with variables subsituted
	 */
	String substitute(String string, Map<String, Object> vars) throws IllegalStateException;
}

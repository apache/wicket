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
package org.apache.wicket.request;

import java.util.List;
import java.util.Set;

import org.apache.wicket.util.string.StringValue;

/**
 * Represents request parameters.
 * 
 * @author Matej Knopp
 */
public interface IRequestParameters
{
	/**
	 * Returns immutable set of all available parameter names.
	 * 
	 * @return list of parameter names
	 */
	Set<String> getParameterNames();

	/**
	 * Returns single value for parameter with specified name. This method always returns non-null
	 * result even if the parameter does not exist.
	 * 
	 * @see StringValue#isNull()
	 * 
	 * @param name
	 *            parameter name
	 * @return {@link StringValue} wrapping the actual value
	 */
	StringValue getParameterValue(String name);

	/**
	 * Returns list of values for parameter with specified name. If the parameter does not exist
	 * this method returns <code>null</code>
	 * 
	 * @param name
	 *            parameter name
	 * @return list of all values for given parameter or <code>null</code> if parameter does not
	 *         exist
	 */
	List<StringValue> getParameterValues(String name);
}

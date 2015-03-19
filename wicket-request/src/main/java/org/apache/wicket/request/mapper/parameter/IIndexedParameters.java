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
package org.apache.wicket.request.mapper.parameter;

import org.apache.wicket.util.string.StringValue;


/**
 * Container for parameters that are identified by their index
 * 
 * @author igor
 */
public interface IIndexedParameters
{
	/**
	 * Sets the indexed parameter on given index
	 * 
	 * @param index
	 *          The position of the parameter
	 * @param object
	 *          The parameter at this position
	 * @return this instance, for chaining
	 */
	IIndexedParameters set(final int index, final Object object);

	/**
	 * @param index
	 *          The position of the parameter
	 * @return indexed parameter on given index
	 */
	StringValue get(final int index);

	/**
	 * Removes indexed parameter on given index
	 * 
	 * @param index
	 *          The position of the parameter
	 * @return this instance, for chaining
	 */
	IIndexedParameters remove(final int index);

	/**
	 * Removes all indexed parameters.
	 * 
	 * @return this instance, for chaining
	 */
	IIndexedParameters clearIndexed();
}

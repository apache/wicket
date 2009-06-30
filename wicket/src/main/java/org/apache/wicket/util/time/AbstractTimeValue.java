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
package org.apache.wicket.util.time;

import org.apache.wicket.util.value.LongValue;

/**
 * Package local class for representing immutable time values in milliseconds and typical operations
 * on such values.
 * 
 * @author Jonathan Locke
 * @since 1.2.6
 */
abstract class AbstractTimeValue extends LongValue
{
	private static final long serialVersionUID = 1L;

	/**
	 * Package local constructor for package subclasses only.
	 * 
	 * @param milliseconds
	 *            the number of milliseconds in this <code>Time</code> value
	 */
	AbstractTimeValue(final long milliseconds)
	{
		super(milliseconds);
	}

	/**
	 * Retrieves the number of milliseconds in this <code>Time</code> value.
	 * 
	 * @return the number of milliseconds in this <code>Time</code> value
	 */
	public final long getMilliseconds()
	{
		return value;
	}
}

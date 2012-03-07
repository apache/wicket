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
package org.apache.wicket.examples.compref;

import org.apache.wicket.util.io.IClusterable;

/**
 * A class for counting things.
 * 
 * @author Jonathan Locke
 * @since 1.2.6
 */
final class Count implements IClusterable
{
	private static final long serialVersionUID = 1L;

	/** the count */
	private int count = 0;

	/**
	 * Retrieves the current count value.
	 * 
	 * @return the count value
	 */
	public int getCount()
	{
		return count;
	}

	/**
	 * Increases the count value by one.
	 */
	public void increment()
	{
		count++;
	}

	/**
	 * Decreases the count value by one.
	 */
	public void decrement()
	{
		count--;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return Integer.toString(count);
	}
}

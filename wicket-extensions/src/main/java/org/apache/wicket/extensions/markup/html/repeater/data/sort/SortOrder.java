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
package org.apache.wicket.extensions.markup.html.repeater.data.sort;

/**
 * Sort order for columns
 * 
 * @author pete
 * 
 */
public enum SortOrder
{
	NONE,
	ASCENDING,
	DESCENDING;

	/**
	 * return next sort order in cycle (will iterate through all
	 * possible states, then start from the beginning)
	 *
	 * @return next sort order
	 */
	public SortOrder nextOrderInCycle()
	{
		final int index = (ordinal() + 1) % values().length;
		
		return values()[index];
	}
}

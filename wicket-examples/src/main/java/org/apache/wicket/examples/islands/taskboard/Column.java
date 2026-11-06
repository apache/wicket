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
package org.apache.wicket.examples.islands.taskboard;

/**
 * The columns of a board, in order.
 */
public enum Column
{
	TODO("To do", 0),

	IN_PROGRESS("In progress", 3),

	REVIEW("Review", 0),

	DONE("Done", 0);

	private final String title;

	private final int limit;

	Column(String title, int limit)
	{
		this.title = title;
		this.limit = limit;
	}

	public String getTitle()
	{
		return title;
	}

	/**
	 * @return the most cards the column may hold, 0 for no limit
	 */
	public int getLimit()
	{
		return limit;
	}
}

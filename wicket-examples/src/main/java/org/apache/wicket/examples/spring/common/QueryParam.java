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
package org.apache.wicket.examples.spring.common;

import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;

/**
 * Encapsulates the Query Paramaters to be passed to daos
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class QueryParam
{
	private int first;

	private int count;

	private SortParam sort;

	/**
	 * Set to return <tt>count</tt> elements, starting at the <tt>first</tt> element.
	 * 
	 * @param first
	 *            First element to return.
	 * @param count
	 *            Number of elements to return.
	 */
	public QueryParam(int first, int count)
	{
		this(first, count, null);
	}

	/**
	 * Set to return <tt>count</tt> sorted elements, starting at the <tt>first</tt> element.
	 * 
	 * @param first
	 *            First element to return.
	 * @param count
	 *            Number of elements to return.
	 * @param sort
	 */
	public QueryParam(int first, int count, SortParam sort)
	{
		this.first = first;
		this.count = count;
		this.sort = sort;
	}

	public int getCount()
	{
		return count;
	}

	public int getFirst()
	{
		return first;
	}

	public SortParam getSort()
	{
		return sort;
	}

	public boolean hasSort()
	{
		return sort != null;
	}

}

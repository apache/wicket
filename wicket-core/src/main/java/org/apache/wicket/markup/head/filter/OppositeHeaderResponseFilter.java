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
package org.apache.wicket.markup.head.filter;

import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.filter.FilteringHeaderResponse.IHeaderResponseFilter;

/**
 * A filter that takes another filter and always returns the opposite of another filter. This is
 * useful where you have two filters (i.e. one for header and one for footer) and want to ensure
 * that nothing ever has false returned for both cases.
 * 
 * @author Jeremy Thomerson
 */
public class OppositeHeaderResponseFilter implements IHeaderResponseFilter
{

	private final String name;
	private final IHeaderResponseFilter other;

	/**
	 * Construct.
	 * 
	 * @param name
	 *            the name used by this filter for its bucket o' stuff
	 * @param other
	 *            the other filter to return the opposite of
	 */
	public OppositeHeaderResponseFilter(String name, IHeaderResponseFilter other)
	{
		this.name = name;
		this.other = other;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public boolean accepts(HeaderItem item)
	{
		return !other.accepts(item);
	}
}

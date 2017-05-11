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
package org.apache.wicket.markup.head;

import java.util.Collections;
import java.util.Objects;

import org.apache.wicket.request.Response;
import org.apache.wicket.util.lang.Args;

/**
 * Free form {@code HeaderItem}. No checks are performed on what is added to the header.
 * 
 * @author papegaaij
 */
public class StringHeaderItem extends HeaderItem
{
	/**
	 * Creates a {@link StringHeaderItem} for the snippet.
	 * 
	 * @param string
	 *            string to be rendered to head
	 * 
	 * @return A newly created {@link StringHeaderItem}.
	 */
	public static StringHeaderItem forString(CharSequence string)
	{
		return new StringHeaderItem(string);
	}

	private final CharSequence string;

	/**
	 * Construct.
	 * 
	 * @param string
	 */
	public StringHeaderItem(CharSequence string)
	{
		this.string = Args.notNull(string, "string");
	}

	/**
	 * @return the string that gets added to the header.
	 */
	public CharSequence getString()
	{
		return string;
	}

	@Override
	public void render(Response response)
	{
		response.write(getString());
	}

	@Override
	public Iterable<?> getRenderTokens()
	{
		return Collections.singletonList(getString());
	}

	@Override
	public String toString()
	{
		return "StringHeaderItem(" + getString() + ")";
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		StringHeaderItem that = (StringHeaderItem) o;
		return Objects.equals(string, that.string);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(string);
	}
}

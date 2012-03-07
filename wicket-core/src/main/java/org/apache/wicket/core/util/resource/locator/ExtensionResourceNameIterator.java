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
package org.apache.wicket.core.util.resource.locator;

import java.util.Arrays;
import java.util.Iterator;

/**
 * Iterate over a set of extensions. If null is provided, hasNext() will
 * successfully return once with next() returning {@code null}.
 *
 * @author Juergen Donnerstag
 */
public class ExtensionResourceNameIterator implements Iterator<String>
{
	private static final Iterable<String> NULL_ITERABLE = Arrays.asList((String)null);

	private final Iterator<String> iterator;

	private String current;

	/**
	 * Construct.
	 *
	 * @param extensions
	 *            {@code null} or iterable with extensions
	 */
	public ExtensionResourceNameIterator(final Iterable<String> extensions)
	{
		// Fail safe: hasNext() needs to return at least once with true
		if (extensions == null || !extensions.iterator().hasNext())
		{
			this.iterator = NULL_ITERABLE.iterator();
		}
		else
		{
			this.iterator = extensions.iterator();
		}
	}

	@Override
	public boolean hasNext()
	{
		return iterator.hasNext();
	}

	/**
	 * @return The next filename extension.
	 */
	@Override
	public String next()
	{
		current = iterator.next();
		return getExtension();
	}

	/**
	 * @return Assuming you've called next() already, it'll return the very same value.
	 */
	public final String getExtension()
	{
		String ext = current;

		if (ext != null)
		{
			if (ext.startsWith("."))
			{
				ext = ext.substring(1);
			}
		}
		return ext;
	}

	@Override
	public void remove()
	{
		iterator.remove();
	}
}

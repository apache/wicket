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
package org.apache.wicket.util.resource.locator;

import java.util.Iterator;

import org.apache.wicket.util.string.Strings;


/**
 * Iterate over a list of 'comma' separated strings. If an empty string is provided, hasNext() will
 * successfully return once with next() returning an empty string ("").
 * 
 * @author Juergen Donnerstag
 */
public class ExtensionResourceNameIterator implements Iterator<String>
{
	private final String[] extensions;

	private int index;

	/**
	 * Construct.
	 * 
	 * @param extension
	 * @param separatorChar
	 */
	public ExtensionResourceNameIterator(final String extension, final char separatorChar)
	{
		// Extension can be a comma separated list
		String[] extensions = Strings.split(extension, separatorChar);
		if (extensions.length == 0)
		{
			// Fail safe: hasNext() needs to return at least once with true.
			extensions = new String[] { "" };
		}
		this.extensions = extensions;

		index = 0;
	}

	/**
	 * 
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext()
	{
		return (index < extensions.length);
	}

	/**
	 * @return The next filename extension. A leading '.' will be removed.
	 */
	public String next()
	{
		index++;
		return getExtension();
	}

	/**
	 * @return Assuming you've called next() already, it'll return the very same value.
	 */
	public final String getExtension()
	{
		String ext = extensions[index - 1];
		return ext.startsWith(".") ? ext.substring(1) : ext;
	}

	/**
	 * Noop.
	 * 
	 * @see java.util.Iterator#remove()
	 */
	public void remove()
	{
	}
}

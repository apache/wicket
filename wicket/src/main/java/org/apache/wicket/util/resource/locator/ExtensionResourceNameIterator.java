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
 * Contains the logic to build the various combinations of file path, style and locale required
 * while searching for Wicket resources. The full filename will be built like:
 * &lt;path&gt;_&lt;style&gt;_&lt;locale&gt;.&lt;extension&gt;.
 * <p>
 * Resource matches will be attempted in the following order:
 * <ol>
 * <li>1. &lt;path&gt;_&lt;style&gt;_&lt;locale&gt;.&lt;extension&gt;</li>
 * <li>2. &lt;path&gt;_&lt;locale&gt;.&lt;extension&gt;</li>
 * <li>3. &lt;path&gt;_&lt;style&gt;.&lt;extension&gt;</li>
 * <li>4. &lt;path&gt;.&lt;extension&gt;</li>
 * </ol>
 * <p>
 * Locales may contain a language, a country and a region or variant. Combinations of these
 * components will be attempted in the following order:
 * <ol>
 * <li>locale.toString() see javadoc for Locale for more details</li>
 * <li>&lt;language&gt;_&lt;country&gt;</li>
 * <li>&lt;language&gt;</li>
 * </ol>
 * 
 * @author Juergen Donnerstag
 * @author Jonathan Locke
 */
public class ExtensionResourceNameIterator implements Iterator
{
	/** The base path */
	private final String path;

	private final String[] extensions;

	private int index;

	/**
	 * Construct.
	 * 
	 * @param path
	 * @param extension
	 */
	public ExtensionResourceNameIterator(String path, final String extension)
	{
		if ((extension == null) && (path.indexOf('.') != -1))
		{
			// Get the extension from the path provided
			extensions = new String[] { "." + Strings.lastPathComponent(path, '.') };
			path = Strings.beforeLastPathComponent(path, '.');
		}
		else if (extension != null)
		{
			// Extension can be a comma separated list
			extensions = Strings.split(extension, ',');
			for (int i = extensions.length - 1; i >= 0; i--)
			{
				extensions[i] = extensions[i].trim();
				if (!extensions[i].startsWith("."))
				{
					extensions[i] = "." + extensions[i];
				}
			}
		}
		else
		{
			extensions = new String[1];
			extensions[0] = ".";
		}

		this.path = path;
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
	 * 
	 * @see java.util.Iterator#next()
	 */
	public Object next()
	{
		return path + extensions[index++];
	}

	/**
	 * 
	 * @see java.util.Iterator#remove()
	 */
	public void remove()
	{
	}
}

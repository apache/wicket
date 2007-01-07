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
package wicket.util.resource.locator;

import java.util.Locale;

/**
 * Contains the logic to build the various combinations of file path, style and
 * locale required while searching for Wicket resources. The full filename will
 * be built like: &lt;path&gt;_&lt;style&gt;_&lt;locale&gt;.&lt;extension&gt;.
 * <p>
 * Resource matches will be attempted in the following order:
 * <ol>
 * <li>1. &lt;path&gt;_&lt;style&gt;_&lt;locale&gt;.&lt;extension&gt;</li>
 * <li>2. &lt;path&gt;_&lt;locale&gt;.&lt;extension&gt;</li>
 * <li>3. &lt;path&gt;_&lt;style&gt;.&lt;extension&gt;</li>
 * <li>4. &lt;path&gt;.&lt;extension&gt;</li>
 * </ol>
 * <p>
 * Locales may contain a language, a country and a region or variant.
 * Combinations of these components will be attempted in the following order:
 * <ol>
 * <li>locale.toString() see javadoc for Locale for more details</li>
 * <li>&lt;language&gt;_&lt;country&gt;</li>
 * <li>&lt;language&gt;</li>
 * </ol>
 * 
 * @author Juergen Donnerstag
 * @author Jonathan Locke
 */
public class StyleAndVariationResourceNameIterator implements IWicketResourceNameIterator
{
	/** The base path */
	private final String path;

	/** The style (see Session) */
	private final String style;

	/** The variation (see Component) */
	private final String variation;

	/** Internal state */
	private int state = 0;

	/** Internal: used to compare with previous path to avoid duplicates */
	private String currentPath;

	/**
	 * Construct.
	 * 
	 * @param path
	 * @param style
	 * @param variation
	 */
	public StyleAndVariationResourceNameIterator(final String path, final String style, final String variation)
	{
		this.path = path;
		this.style = style;
		this.variation = variation;
	}

	/**
	 * @see wicket.util.resource.locator.IWicketResourceNameIterator#getLocale()
	 */
	public Locale getLocale()
	{
		return null;
	}

	/**
	 * 
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext()
	{
		return (this.state < 3);
	}

	/**
	 * 
	 * @see java.util.Iterator#next()
	 */
	public String next()
	{
		if (state == 0)
		{
			state++;
			if ((style != null) && (variation != null))
			{
				return path + '_' + style + '_' + variation;
			}
		}
		
		if (state == 1)
		{
			state++;
			if (style != null)
			{
				return path + '_' + style;
			}
		}
		
		state = 3;
		return path;
	}

	/**
	 * 
	 * @see java.util.Iterator#remove()
	 */
	public void remove()
	{
	}
}

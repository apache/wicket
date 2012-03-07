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

import java.util.Iterator;

/**
 * Iterate over all possible combinations of style and variation
 *
 * @author Juergen Donnerstag
 */
public class StyleAndVariationResourceNameIterator implements Iterator<String>
{
	/** The style (see Session) */
	private final String style;

	/** The variation (see Component) */
	private final String variation;

	/** Internal state */
	private int state = 0;

	/**
	 * Construct.
	 *
	 * @param style
	 * @param variation
	 */
	public StyleAndVariationResourceNameIterator(final String style, final String variation)
	{
		this.style = style;
		this.variation = variation;
	}

	/**
	 *
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext()
	{
		return (state < 4);
	}

	/**
	 * The return value will always be null. Use getStyle() and getVariation() instead.
	 *
	 * @see java.util.Iterator#next()
	 */
	@Override
	public String next()
	{
		if (state == 0)
		{
			state++;
			if ((style != null) && (variation != null))
			{
				return null;
			}
		}

		if (state == 1)
		{
			state++;
			if (style != null)
			{
				return null;
			}
		}

		if (state == 2)
		{
			state++;
			if (variation != null)
			{
				return null;
			}
		}

		state = 4;
		return null;
	}

	/**
	 * @return Gets the style related to the iterator state
	 */
	public final String getStyle()
	{
		return ((state == 1) || (state == 2)) ? style : null;
	}

	/**
	 * @return Gets the variation related to the iterator state
	 */
	public final String getVariation()
	{
		return ((state == 1) || (state == 3)) ? variation : null;
	}

	/**
	 *
	 * @see java.util.Iterator#remove()
	 */
	@Override
	public void remove()
	{
	}
}

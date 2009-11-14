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
package org.apache.wicket.markup;

import java.util.Iterator;

/**
 * @author Juergen Donnerstag
 */
public class OpenTagIterator implements Iterator<ComponentTag>
{
	private final IMarkupFragment markup;

	private int index = -1;

	/**
	 * Construct.
	 * 
	 * @param markup
	 */
	public OpenTagIterator(final IMarkupFragment markup)
	{
		this.markup = markup;
	}

	/**
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext()
	{
		while (++index < markup.size())
		{
			MarkupElement elem = markup.get(index);
			if (elem instanceof ComponentTag)
			{
				ComponentTag tag = (ComponentTag)elem;
				if (tag.isOpen() || tag.isOpenClose())
				{
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * @see java.util.Iterator#next()
	 */
	public ComponentTag next()
	{
		return (index < markup.size() ? (ComponentTag)markup.get(index) : null);
	}

	/**
	 * 
	 * @return MarkupFragment
	 */
	public final IMarkupFragment getMarkupFragment()
	{
		return new MarkupFragment(markup, index);
	}

	/**
	 * @see java.util.Iterator#remove()
	 */
	public void remove()
	{
		throw new UnsupportedOperationException("remove() is not supported");
	}
}

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
 * 
 * @author Juergen Donnerstag
 */
public class MarkupIterator implements Iterator<MarkupElement>
{
	private final IMarkupFragment markup;

	private int index = -1;

	private boolean componentTagOnly;
	private boolean wicketTagOnly;
	private boolean openTagOnly;

	/**
	 * Construct.
	 * 
	 * @param markup
	 */
	public MarkupIterator(final IMarkupFragment markup)
	{
		if (markup == null)
		{
			throw new NullPointerException("Parameter 'markup' must not be null");
		}
		this.markup = markup;
	}

	/**
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext()
	{
		for (index++; index < markup.size(); index++)
		{
			MarkupElement elem = markup.get(index);
			if ((componentTagOnly && (elem instanceof ComponentTag)) ||
				(wicketTagOnly && (elem instanceof WicketTag)))
			{
				if (openTagOnly)
				{
					ComponentTag tag = (ComponentTag)elem;
					if (tag.isOpen())
					{
						return true;
					}
					else
					{
						continue;
					}
				}
				return true;
			}
			return true;
		}

		return false;
	}

	/**
	 * @see java.util.Iterator#next()
	 */
	@Override
	public MarkupElement next()
	{
		return markup.get(index);
	}

	/**
	 * @return The next element assuming it is a ComponentTag or WicketTag
	 */
	public ComponentTag nextTag()
	{
		return (ComponentTag)next();
	}

	/**
	 * @return The next element assuming it is a WicketTag
	 */
	public WicketTag nextWicketTag()
	{
		return (WicketTag)next();
	}

	/**
	 * @see java.util.Iterator#remove()
	 */
	@Override
	public void remove()
	{
		throw new UnsupportedOperationException("You can not remove markup elements");
	}

	/**
	 * Ignore raw markup and iterate over component and wicket tags only.
	 * 
	 * @param componentTagOnly
	 */
	public final void setComponentTagOnly(boolean componentTagOnly)
	{
		this.componentTagOnly = componentTagOnly;
	}

	/**
	 * Ignore raw markup and component tags, and iterate over WicketTags only
	 * 
	 * @param wicketTagOnly
	 */
	public final void setWicketTagOnly(boolean wicketTagOnly)
	{
		this.wicketTagOnly = wicketTagOnly;
	}

	/**
	 * Ignore close tag. Iterate over open and open-close tags only
	 * 
	 * @param openTagOnly
	 */
	public final void setOpenTagOnly(boolean openTagOnly)
	{
		this.openTagOnly = openTagOnly;
	}
}

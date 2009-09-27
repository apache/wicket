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
package org.apache.wicket.markup.html.internal;

import java.util.Collections;
import java.util.List;
import java.util.Stack;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.IMarkupFragment;
import org.apache.wicket.markup.MarkupElement;
import org.apache.wicket.markup.MarkupFragment;
import org.apache.wicket.markup.WicketTag;
import org.apache.wicket.util.collections.ReadOnlyIterator;

/**
 * 
 * @author Juergen Donnerstag
 */
public class MarkupTagIterator extends ReadOnlyIterator<ComponentTag>
{
	private final IMarkupFragment markup;

	private int nextIndex = -1;

	private ComponentTag next;

	private boolean noSubChilds;

	private boolean openOnly;

	/** <wicket:xxx */
	private boolean wicketTagsOnly;

	private final Stack<ComponentTag> nextStack = new Stack<ComponentTag>();;

	private Stack<ComponentTag> currentStack;

	/**
	 * Construct.
	 * 
	 * @param markup
	 * @param noSubchilds
	 * @param openOnly
	 */
	public MarkupTagIterator(final IMarkupFragment markup)
	{
		super();

		this.markup = markup;
	}

	/**
	 * 
	 * @param value
	 * @return this
	 */
	public MarkupTagIterator setWicketTagsOnly(boolean value)
	{
		wicketTagsOnly = value;
		return this;
	}

	/**
	 * 
	 * @param value
	 * @return this
	 */
	public MarkupTagIterator setNoSubChilds(boolean value)
	{
		noSubChilds = value;
		return this;
	}

	/**
	 * 
	 * @param value
	 * @return this
	 */
	public MarkupTagIterator setOpenTagOnly(boolean value)
	{
		openOnly = value;
		return this;
	}

	/**
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext()
	{
		return findNext();
	}

	/**
	 * @see java.util.Iterator#next()
	 */
	public ComponentTag next()
	{
		return next;
	}

	/**
	 * @return next wicket tag
	 */
	public WicketTag nextWicketTag()
	{
		return (WicketTag)next();
	}

	/**
	 * Gets currentIndex.
	 * 
	 * @return currentIndex
	 */
	public int getIndex()
	{
		return nextIndex;
	}

	/**
	 * 
	 * @return The parent open tags.
	 */
	public List<ComponentTag> getStack()
	{
		return Collections.unmodifiableList(currentStack);
	}

	/**
	 * @return a new markup fragment for the current open tag
	 */
	public IMarkupFragment getMarkupFragment()
	{
		if ((next != null) && (next.isOpen() || next.isOpenClose()))
		{
			return new MarkupFragment(markup, getIndex());
		}

		throw new WicketRuntimeException("Current tag is not an open tag: " + next);
	}

	/**
	 * @return true, if one more tag was found
	 */
	private boolean findNext()
	{
		// preset to not-found
		next = null;

		currentStack = new Stack<ComponentTag>();
		currentStack.addAll(nextStack);

		for (nextIndex = nextIndex + 1; nextIndex < markup.size(); nextIndex++)
		{
			final MarkupElement elem = markup.get(nextIndex);

			if (elem instanceof ComponentTag)
			{
				ComponentTag tag = (ComponentTag)elem;

				if (tag.isOpen())
				{
					nextStack.push(tag);
				}
				else if (tag.isClose())
				{
					nextStack.pop();
				}

				if ((openOnly == false) || (tag.isClose() == false))
				{
					if ((noSubChilds == false) || (nextStack.size() != 1))
					{
						if ((wicketTagsOnly == false) || (tag instanceof WicketTag))
						{
							next = tag;
							break;
						}
					}
				}
			}
		}

		return next != null;
	}

	/**
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "nextIndex=" + nextIndex + "; noSubchilds=" + noSubChilds + "; openOnly=" +
			openOnly + "; stack=" + (nextStack != null ? nextStack.size() : "null") + "; next=" +
			next;
	}
}
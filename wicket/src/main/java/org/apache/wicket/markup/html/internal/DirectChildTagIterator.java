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

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupElement;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.util.collections.ReadOnlyIterator;

/**
 * Iterator that iterates over direct child component tags of the given component tag
 */
class DirectChildTagIterator extends ReadOnlyIterator<ComponentTag>
{
	private final MarkupStream markupStream;
	private final ComponentTag parent;
	private ComponentTag next = null;
	private int nextIndex;
	private int currentIndex;
	private final int originalIndex;

	/**
	 * Construct.
	 * 
	 * @param markupStream
	 * @param parent
	 */
	public DirectChildTagIterator(MarkupStream markupStream, ComponentTag parent)
	{
		super();
		this.markupStream = markupStream;
		this.parent = parent;
		originalIndex = markupStream.getCurrentIndex();
		findNext();
	}

	/**
	 * Resets markup stream to the original position
	 */
	public void rewind()
	{
		markupStream.setCurrentIndex(originalIndex);
	}

	/**
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext()
	{
		return next != null;
	}

	/**
	 * @see java.util.Iterator#next()
	 */
	public ComponentTag next()
	{
		ComponentTag ret = next;
		currentIndex = nextIndex;
		findNext();
		return ret;
	}

	/**
	 * Gets currentIndex.
	 * 
	 * @return currentIndex
	 */
	public int getCurrentIndex()
	{
		return currentIndex;
	}

	private void findNext()
	{
		ComponentTag tag = next;
		next = null;

		if (tag != null && tag.isOpenClose())
		{
			// if current child tag is open-close look for next child
			tag = null;
		}

		while (markupStream.hasMore())
		{
			final MarkupElement cursor = markupStream.next();

			if (cursor.closes(parent))
			{
				// parent close tag found, we are done
				break;
			}

			if (tag != null && cursor.closes(tag))
			{
				// child tag is closed, next tag is either parent-close or next direct child
				tag = null;
			}
			else if (tag == null && cursor instanceof ComponentTag)
			{
				// found next child
				next = (ComponentTag)cursor;
				nextIndex = markupStream.getCurrentIndex();
				break;
			}
		}
	}
}
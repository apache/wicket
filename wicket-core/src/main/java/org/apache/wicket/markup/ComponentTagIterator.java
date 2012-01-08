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

import java.util.NoSuchElementException;
import java.util.Stack;

import org.apache.wicket.util.collections.ReadOnlyIterator;

public class ComponentTagIterator extends ReadOnlyIterator<ComponentTag>
{
	private final MarkupStream stream;
	private final Stack<ComponentTag> stack;
	private ComponentTag next;
	private ComponentTag last;
	private boolean end;

	public ComponentTagIterator(MarkupStream stream)
	{
		this.stream = stream;
		stack = new Stack<ComponentTag>();
	}

	public Stack<ComponentTag> stack()
	{
		return stack;
	}

	public void skipToCloseTag()
	{
		if (last == null)
		{
			// if no next() has been called we skip to the end of the markup
			end = true;
		}
		if (last.isClose() || last.isOpenClose())
		{
			throw new IllegalStateException("Cannot skip to the closing tag of a closed tag: " +
				last);
		}

		MarkupElement element = null;
		while ((element = stream.next()) != null)
		{
			if (element.closes(last))
			{
				next = (ComponentTag)element;
				return;
			}
		}

		end = true;
		next = null;
	}

	@Override
	public boolean hasNext()
	{
		if (end == true)
		{
			return false;
		}

		if (next != null)
		{
			return true;
		}

		findNext();

		return next != null;
	}

	private void findNext()
	{
		next = null;
		MarkupElement el = stream.get();
		while (el != null && !(el instanceof ComponentTag))
		{
			el = stream.next();
		}
		next = (ComponentTag)el;
	}

	public ComponentTag peek()
	{
		if (end == true)
		{
			return null;
		}
		if (next == null)
		{
			findNext();
		}
		return next;
	}

	@Override
	public ComponentTag next()
	{
		if (end)
		{
			throw new NoSuchElementException();
		}

		if (next == null)
		{
			findNext();
		}

		if (stack.size() > 0 && stack.peek().isOpenClose())
		{
			stack.pop();
		}

		if (next.isOpen() || next.isOpenClose())
		{
			stack.push(next);
		}
		else
		{
			stack.pop();
		}

		ComponentTag result = next;
		next = null;

		if (stream.next() == null)
		{
			end = true;
		}

		last = result;
		return result;
	}


}

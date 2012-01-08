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

		return result;
	}


}

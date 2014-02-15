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
package org.apache.wicket.queueing;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

class HasPath extends TypeSafeMatcher<Component>
{
	private final Path path;

	public HasPath(Path path)
	{
		this.path = path;
	}

	public void describeTo(Description description)
	{
		description.appendText("path ").appendText(toString(path, 0, path.size()));
	}

	@Override
	protected boolean matchesSafely(Component item)
	{
		Component cursor = item;
		for (int i = 0; i < path.size(); i++)
		{
			if (!(cursor instanceof MarkupContainer))
			{
				return false;
			}

			String id = path.get(i).getId();
			Component child = ((MarkupContainer)cursor).get(id);
			if (child== null)
			{
				return false;
			}
			
			cursor=child;
			if (!path.get(i).getType().isAssignableFrom(cursor.getClass()))
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public void describeMismatchSafely(Component item, Description desc)
	{
		Component cursor = item;

		int matched = 0;

		String error = null;

		for (int i = 0; i < path.size(); i++)
		{
			matched = i;
			if (!(cursor instanceof MarkupContainer))
			{
				error = "next component has to be at least a MarkupContainer to contain children, but was: " +
					toString(cursor);
				break;
			}

			cursor = ((MarkupContainer)cursor).get(path.get(i).getId());
			if (cursor == null)
			{
				error = "next child with id: '" + path.get(i).getId() + "' not found";
				break;
			}
			if (!path.get(i).getType().isAssignableFrom(cursor.getClass()))
			{
				error = "expected next child of type: " + path.get(i).getType().getSimpleName() +
					", but found: " + toString(cursor);
				break;
			}
		}

		desc.appendText("\n       root: ").appendText(toString(item));
		desc.appendText("\n       matched segments: ").appendText(toString(path, 0, matched));
		desc.appendText("\n       error: ").appendText(error);
	}

	private static String toString(Component c)
	{
		return toString(c.getClass(), c.getId());
	}

	private static String toString(Class<?> type, String id)
	{
		return type.getSimpleName() + "('" + id + "')";
	}

	private static String toString(Path path, int start, int end)
	{
		String str = "[";
		for (int i = start; i < end; i++)
		{
			if (i > 0)
			{
				str += ", ";
			}
			str += toString(path.get(i).getType(), path.get(i).getId());
		}
		str += "]";
		return str;
	}


	@Factory
	public static <T> Matcher<Component> hasPath(Path path)
	{
		return new HasPath(path);
	}


}
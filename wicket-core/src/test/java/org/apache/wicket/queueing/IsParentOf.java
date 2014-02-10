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
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

class IsParentOf extends TypeSafeMatcher<Component>
{
	private final Component child;

	public IsParentOf(Component child)
	{
		this.child = child;
	}

	public void describeTo(Description description)
	{
		description.appendText(toString(child.getParent()));
	}

	@Override
	protected boolean matchesSafely(Component item)
	{
		if (!(item instanceof MarkupContainer))
		{
			return false;
		}

		if (!(item instanceof MarkupContainer))
			return false;
		MarkupContainer container = (MarkupContainer)item;
		if (container.get(child.getId()) != child)
			return false;
		if (child.getParent() != container)
			return false;
		return true;
	}

	@Override
	public void describeMismatchSafely(Component item, Description description)
	{
		if (child.getParent() != item)
		{
			description.appendText("found " + toString(item));
			return;
		}

		if (!(item instanceof MarkupContainer))
		{
			description.appendText("found ")
				.appendText(toString(item))
				.appendText(" which is not a container");
			return;
		}

		if (((WebMarkupContainer)item).get(child.getId()) == null)
		{
			description.appendText(toString(item))
				.appendText(" does not contain ")
				.appendText(toString(child));
			return;
		}
		super.describeMismatchSafely(item, description);
	}

	private static String toString(Component c)
	{
		return c.getClass().getSimpleName() + "('" + c.getId() + "')";
	}

	@Factory
	public static <T> Matcher<Component> isParentOf(Component child)
	{
		return new IsParentOf(child);
	}


}
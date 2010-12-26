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

/**
 * Base class for different kinds of markup elements. Markup elements are held in a Markup container
 * object.
 * <p>
 * Wicket divides markup like (x)html, wml etc. into two types of MarkupElements:
 * <ul>
 * <li>ComponentTag, which represents a "significant" markup tag (meaning that the tag has some
 * meaning to Wicket)
 * <li>RawMarkup, which is a section of unparsed markup having no meaning to Wicket.
 * </ul>
 * 
 * @see org.apache.wicket.markup.RawMarkup
 * @see ComponentTag
 * @author Jonathan Locke
 */
public abstract class MarkupElement
{
	/**
	 * Constructor.
	 */
	public MarkupElement()
	{
	}

	/**
	 * Gets whether this element closes the given element.
	 * 
	 * @param open
	 *            The open tag
	 * @return True if this markup element closes the given open tag
	 */
	public boolean closes(final MarkupElement open)
	{
		return false;
	}

	/**
	 * This is not an implementation of equals because we don't care about hashCodes for
	 * MarkupElements yet. Also, this method only compares the namespace, name and attributes of the
	 * given MarkupElements.
	 * 
	 * @param element
	 *            The markup element to compare with
	 * @return True if the other element equals this one
	 */
	public abstract boolean equalTo(MarkupElement element);

	/**
	 * @return Gets the charsequence representation of this element
	 */
	public abstract CharSequence toCharSequence();

	/**
	 * Gets a string representation.
	 * 
	 * @return A string representation suitable for displaying to the user when something goes
	 *         wrong.
	 */
	public abstract String toUserDebugString();
}

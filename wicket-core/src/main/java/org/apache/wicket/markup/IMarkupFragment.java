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
 * Any list of MarkupElements. May be the content of complete markup file or just a portion of it.
 * 
 * @see Markup
 * @see MarkupFragment
 * @see MarkupElement
 * 
 * @author Juergen Donnerstag
 */
public interface IMarkupFragment extends Iterable<MarkupElement>
{
	/**
	 * Get the MarkupElement at the index provided.
	 * 
	 * @param index
	 *            Index into markup list
	 * @return Markup element
	 * @throws IndexOutOfBoundsException
	 *             if the index is out of range (<tt>index &lt; 0 || index &gt;= size()</tt>)
	 */
	MarkupElement get(final int index);

	/**
	 * Get the underlying markup resource stream, which might contain more than just the markup
	 * portion represented by the IMarkupFragment.
	 * 
	 * @return The underlying markup resource stream
	 */
	MarkupResourceStream getMarkupResourceStream();

	/**
	 * The number of markup elements.
	 * 
	 * @return Number of markup elements
	 */
	int size();

	/**
	 * Find the markup fragment of the component with 'path'
	 * 
	 * @param id
	 *            The component's id to search for
	 * @return -1, if not found
	 */
	IMarkupFragment find(final String id);

	/**
	 * 
	 * @param markupOnly
	 *            True if only the markup shall be returned
	 * @return markup string
	 */
	String toString(final boolean markupOnly);
}
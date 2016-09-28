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
import org.apache.wicket.markup.parser.filter.InlineEnclosureHandler;


/**
 * An InlineEnclosure are automatically created by Wicket. Do not create it yourself. An
 * InlineEnclosure container is created when &lt;tr wicket:enclosure="controllingChildId"&gt; (any
 * html tag which can contain other html tags can be used in place of &lt;tr&gt;) is found in the
 * markup. The child component (it's id defined as the value of the attribute, in the example,
 * 'controllingChildId') controls the visibility of the whole enclosure and it's children. This also
 * works in Ajax calls without extra markup or java code.
 * 
 * @see InlineEnclosureHandler
 * 
 * @author Joonas Hamalainen
 * @author Juergen Donnerstag
 */
public class InlineEnclosure extends Enclosure
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param id The component id
	 * @param childId The id of the child component that controls the visibility
	 */
	public InlineEnclosure(final String id, final String childId)
	{
		super(id, childId);

		// ensure that the Enclosure is ready for ajax updates
		setOutputMarkupPlaceholderTag(true);
	}

	@Override
	protected void onComponentTag(final ComponentTag tag)
	{
		// remove the wicket:enclosure attribute
		tag.remove(InlineEnclosureHandler.INLINE_ENCLOSURE_ATTRIBUTE_NAME);

		super.onComponentTag(tag);
	}
}

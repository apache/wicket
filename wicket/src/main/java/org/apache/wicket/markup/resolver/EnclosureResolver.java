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
package org.apache.wicket.markup.resolver;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.WicketTag;
import org.apache.wicket.markup.html.internal.Enclosure;
import org.apache.wicket.markup.html.internal.InlineEnclosure;
import org.apache.wicket.markup.parser.filter.EnclosureHandler;
import org.apache.wicket.markup.parser.filter.InlineEnclosureHandler;

/**
 * This is a tag resolver which automatically adds a Enclosure container for each
 * &lt;wicket:enclosure&gt; tag.
 * 
 * @see EnclosureHandler
 * @see Enclosure
 * 
 * @author Juergen Donnerstag
 */
public class EnclosureResolver implements IComponentResolver
{
	private static final long serialVersionUID = 1L;

	public boolean resolve(final MarkupContainer container, final MarkupStream markupStream,
		final ComponentTag tag)
	{
		boolean isEnclosureTag = (tag instanceof WicketTag) && ((WicketTag)tag).isEnclosureTag();
		String inlineEnclosureChildId = InlineEnclosureHandler.getInlineEnclosureAttribute(tag);

		if (isEnclosureTag)
		{
			String id = null;

			CharSequence wicketId = tag.getString("wicket:id");
			if (wicketId != null)
			{
				id = wicketId.toString();
			}
			if (id == null)
			{
				id = "enclosure-" + container.getPage().getAutoIndex();
			}

			CharSequence childId = tag.getString(EnclosureHandler.CHILD_ATTRIBUTE);
			Enclosure enclosure = new Enclosure(id, childId);

			container.autoAdd(enclosure, markupStream);

			return true;
		}
		else if (inlineEnclosureChildId != null)
		{
			Enclosure enclosure = new InlineEnclosure(tag.getId(), inlineEnclosureChildId);

			container.autoAdd(enclosure, markupStream);

			return true;
		}

		return false;
	}
}
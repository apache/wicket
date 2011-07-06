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
package org.apache.wicket.markup.html.panel;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.IMarkupFragment;
import org.apache.wicket.markup.MarkupException;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.markup.parser.XmlTag.TagType;

/**
 * Implements boilerplate as needed by many markup sourcing strategies.
 * 
 * @author Juergen Donnerstag
 */
public abstract class AbstractMarkupSourcingStrategy implements IMarkupSourcingStrategy
{
	/**
	 * Construct.
	 */
	public AbstractMarkupSourcingStrategy()
	{
	}

	public abstract IMarkupFragment getMarkup(final MarkupContainer container, final Component child);

	/**
	 * Make sure we open up open-close tags to open-body-close
	 */
	public void onComponentTag(final Component component, final ComponentTag tag)
	{
		if (tag.isOpenClose())
		{
			tag.setType(TagType.OPEN);
		}
	}

	/**
	 * Skip the components body which is expected to be raw markup only (no wicket components). It
	 * will be replaced by the associated markup.
	 */
	public void onComponentTagBody(final Component component, final MarkupStream markupStream,
		final ComponentTag openTag)
	{
		// Skip the components body. It will be replaced by the associated markup or fragment
		if (markupStream.getPreviousTag().isOpen())
		{
			markupStream.skipRawMarkup();
			if (markupStream.get().closes(openTag) == false)
			{
				throw new MarkupException(
					markupStream,
					"Close tag not found for tag: " +
						openTag.toString() +
						". For " +
						component.getClass().getSimpleName() +
						" Components only raw markup is allow in between the tags but not other Wicket Component." +
						". Component: " + component.toString());
			}
		}
	}

	/**
	 * Empty. Nothing to be added to the response by default.
	 */
	public void renderHead(final Component component, HtmlHeaderContainer container)
	{
	}
}

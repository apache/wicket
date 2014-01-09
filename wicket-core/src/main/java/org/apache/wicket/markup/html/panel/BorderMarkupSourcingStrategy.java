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
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.border.Border;

/**
 * The Border component's markup sourcing strategy
 * 
 * @author Juergen Donnerstag
 */
public class BorderMarkupSourcingStrategy extends AssociatedMarkupSourcingStrategy
{

	/**
	 * Constructor.
	 */
	public BorderMarkupSourcingStrategy()
	{
		super(Border.BORDER);
	}

	@Override
	public void onComponentTagBody(Component component, MarkupStream markupStream,
		ComponentTag openTag)
	{
		renderAssociatedMarkup(component);

		markupStream.skipToMatchingCloseTag(openTag);
	}

	/**
	 * Return null and thus use <code>Border.getMarkup(child)</code> to provide the Markup
	 */
	@Override
	public IMarkupFragment getMarkup(final MarkupContainer container, final Component child)
	{
		return null;
	}
}

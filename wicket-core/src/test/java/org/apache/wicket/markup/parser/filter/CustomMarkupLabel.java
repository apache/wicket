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
package org.apache.wicket.markup.parser.filter;

import java.nio.charset.Charset;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.IMarkupCacheKeyProvider;
import org.apache.wicket.markup.IMarkupFragment;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.MarkupException;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.panel.AbstractMarkupSourcingStrategy;
import org.apache.wicket.markup.html.panel.IMarkupSourcingStrategy;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;

/**
 * https://issues.apache.org/jira/browse/WICKET-4494
 * @see HtmlHandlerTest
 */
public class CustomMarkupLabel
		extends MarkupContainer
		implements IMarkupCacheKeyProvider, IMarkupResourceStreamProvider
{
	private static final String SAMPLE_MARKUP = "<img alt='logo' src='logo.png'><br>Some text<br>Some more text";
	
	public CustomMarkupLabel(final String id)
	{
		super(id); 
	}

	@Override
	protected IMarkupSourcingStrategy newMarkupSourcingStrategy()
	{
		return new MyMarkupSourcingStrategy(this);
	}
	
	@Override
	public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass)
	{
		// the markup is loaded from database in our real application
		StringResourceStream res = new StringResourceStream(SAMPLE_MARKUP);
		res.setCharset(Charset.forName("UTF-8"));
		return res;
	}

	@Override
	public String getCacheKey(MarkupContainer container, Class<?> containerClass)
	{
		return null;
	}
	
	//
	// custom markup sourcing strategy
	//
	
	private static class MyMarkupSourcingStrategy extends AbstractMarkupSourcingStrategy
	{
		private final CustomMarkupLabel markupProvider;

		public MyMarkupSourcingStrategy(final CustomMarkupLabel markupProvider)
		{
			this.markupProvider = markupProvider;
		}

		@Override
		public void onComponentTagBody(Component component, MarkupStream markupStream, ComponentTag openTag)
		{
			super.onComponentTagBody(component, markupStream, openTag);
			// 
			MarkupStream stream = new MarkupStream(getMarkup((MarkupContainer)component, null));
			component.onComponentTagBody(stream, openTag);
		}

		@Override
		public IMarkupFragment getMarkup(final MarkupContainer container, final Component child)
		{
			IMarkupFragment markup = markupProvider.getAssociatedMarkup();
			if (markup == null)
			{
				throw new MarkupException("The EntityText has no markup!");
			}
			//
			if (child == null)
			{
				return markup;
			}
			// search for the child insight the fragment markup
			return markup.find(child.getId());
		}
	}
}

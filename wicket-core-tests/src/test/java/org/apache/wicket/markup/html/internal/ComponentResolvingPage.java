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

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.resolver.IComponentResolver;

/**
 * 
 * @author svenmeier
 */
public class ComponentResolvingPage extends WebPage implements IComponentResolver
{
	private static final long serialVersionUID = 1L;

	/** onEndRequestWasCalledOnAutoAddedComponent */
	public boolean onDetachWasCalledOnAutoAddedComponent = false;

	/**
	 * Construct.
	 */
	public ComponentResolvingPage()
	{
	}

	/**
	 * @see org.apache.wicket.markup.resolver.IComponentResolver#resolve(org.apache.wicket.MarkupContainer,
	 *      org.apache.wicket.markup.MarkupStream, org.apache.wicket.markup.ComponentTag)
	 */
	@Override
	public Component resolve(final MarkupContainer container, final MarkupStream markupStream,
		final ComponentTag tag)
	{
		if ("test".equals(tag.getId()))
		{
			return new Label("test", "TEST")
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected void onDetach()
				{
					onDetachWasCalledOnAutoAddedComponent = true;
					super.onDetach();
				}
			};
		}
		return null;
	}
}
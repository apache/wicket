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
package org.apache.wicket;

import java.util.Optional;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.IMarkupCacheKeyProvider;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;

/**
 * A panel with a link. The link can be used both as normal and Ajax.
 */
public abstract class MockPanelWithLink extends Panel
	implements
		IMarkupResourceStreamProvider,
		IMarkupCacheKeyProvider
{

	/**
	 * Construct.
	 * 
	 * @param id
	 *            the component id
	 */
	public MockPanelWithLink(String id)
	{
		super(id);

		add(new AjaxFallbackLink<Void>("link")
		{
			@Override
			public void onClick(Optional<AjaxRequestTarget> targetOptional)
			{
				MockPanelWithLink.this.onLinkClick(targetOptional.orElse(null));
			}
		});
	}

	/**
	 * The callback to execute when the link is clicked.
	 * 
	 * @param target
	 *            the current Ajax request target. May be {@code null} if
	 *            {@link org.apache.wicket.util.tester.BaseWicketTester#clickLink(String, boolean false)} is used.
	 */
	protected abstract void onLinkClick(AjaxRequestTarget target);

	@Override
	public String getCacheKey(MarkupContainer container, Class<?> containerClass)
	{
		// no caching
		return null;
	}

	@Override
	public IResourceStream getMarkupResourceStream(MarkupContainer container,
		Class<?> containerClass)
	{
		return new StringResourceStream("<wicket:panel><a wicket:id='link'>Link</a></wicket:panel>");
	}


}

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
package org.apache.wicket.markup.resolver.issue5569;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.TransparentWebMarkupContainer;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.resolver.IComponentResolver;

public class HomePage extends WebPage
{
	@Override
	protected void onInitialize()
	{
		super.onInitialize();

		add(new AjaxLink<Void>("link")
		{
			@Override
			public void onClick(AjaxRequestTarget target)
			{
				target.add(getPage().get("special"));
			}
		});

		WebMarkupContainer mc1 = new TransparentWebMarkupContainer("mc1");

		add(mc1);

		mc1.add(new WebMarkupContainer("special"));

		WebMarkupContainer mc2 = new WebMarkupContainer("mc2");

		add(mc2);

		WebMarkupContainer mc3 = new ComponentResolvingWebMarkupContainer("mc3", HomePage.this);

		mc2.add(mc3);

		add(new WebMarkupContainer("special").setOutputMarkupId(true));
	}

	private static class ComponentResolvingWebMarkupContainer
		extends WebMarkupContainer
		implements IComponentResolver
	{
		private final MarkupContainer resolveTarget;

		public ComponentResolvingWebMarkupContainer(String id, MarkupContainer resolveTarget)
		{
			super(id);
			this.resolveTarget = resolveTarget;
		}

		@Override
		public Component resolve(MarkupContainer container, MarkupStream markupStream, ComponentTag tag)
		{
			Component c = resolveTarget.get(tag.getId());

			if (c == null && resolveTarget instanceof IComponentResolver)
			{
				c = ((IComponentResolver)resolveTarget).resolve(container, markupStream, tag);
			}

			if (c != null && getPage().wasRendered(c))
			{
				return null;
			}

			return c;
		}
	}
}

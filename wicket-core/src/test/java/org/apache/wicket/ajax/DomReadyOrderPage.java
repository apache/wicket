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
package org.apache.wicket.ajax;

import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;

/**
 * @author jcompagner
 */
public class DomReadyOrderPage extends WebPage
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 */
	public DomReadyOrderPage()
	{
		add(new Link("test"));
	}

	private static class Link extends AjaxFallbackLink<Void>
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 * 
		 * @param id
		 */
		public Link(String id)
		{
			super(id);
		}

		@Override
		public void internalRenderHead(HtmlHeaderContainer container)
		{
			super.internalRenderHead(container);

			container.getHeaderResponse().render(OnDomReadyHeaderItem.forScript("test1();"));
			container.getHeaderResponse().render(OnDomReadyHeaderItem.forScript("test2();"));
		}

		@Override
		public void onClick(AjaxRequestTarget target)
		{
			target.add(this);
		}
	}
}

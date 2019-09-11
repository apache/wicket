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
package org.apache.wicket.markup.head;

import org.apache.wicket.markup.head.StringHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.PriorityHeaderItem;

/**
 * See {@link HeaderResponseTest#testPriorityDepths()}.
 */
public class PriorityDepthsPage extends WebPage
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 */
	public PriorityDepthsPage()
	{
		add(new Priority("a", "x"));

		Priority b = new Priority("b", "y");
		add(b);
		
		b.add(new Priority("c", "x"));
	}

	private class Priority extends WebMarkupContainer {

		private String title;

		public Priority(String id, String title)
		{
			super(id);
			
			this.title = title;
		}
		
		@Override
		public void renderHead(IHeaderResponse response)
		{
			super.renderHead(response);
			
			response.render(new PriorityHeaderItem(
				StringHeaderItem.forString(String.format("<title>%s</title>\n", title))));
		}
	}
}

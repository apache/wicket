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
package org.apache.wicket.markup.renderStrategy;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;


/**
 * Mock page for testing.
 */
public class SimplePage2 extends WebPage
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 */
	public SimplePage2()
	{
		MarkupContainer container1 = addXXX("container1", this);
		MarkupContainer container2 = addXXX("container2", this);
		MarkupContainer container2_1 = addXXX("container2_1", container2);
		MarkupContainer container3 = addXXX("container3", this);
		MarkupContainer container3_1 = addXXX("container3_1", container3);
		MarkupContainer container3_1_1 = addXXX("container3_1_1", container3_1);

		MarkupContainer container4 = addXXX("container4", this);
		MarkupContainer container4_1 = addXXX("container4_1", container4);
		MarkupContainer container4_2 = addXXX("container4_2", container4);
		MarkupContainer container4_2_1 = addXXX("container4_2_1", container4_2);
		MarkupContainer container4_3 = addXXX("container4_3", container4);
		MarkupContainer container4_3_1 = addXXX("container4_3_1", container4_3);
		MarkupContainer container4_3_1_1 = addXXX("container4_3_1_1", container4_3_1);

		add(new SimplePanel1("panel1"));
	}

	private MarkupContainer addXXX(final String id, final MarkupContainer parent)
	{
		MarkupContainer container = new WebMarkupContainer(id);
		parent.add(container);
		container.add(new Behavior()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void renderHead(Component component, IHeaderResponse response)
			{
				response.render(CssHeaderItem.forUrl(id + ".css"));
			}
		});
		return container;
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);
		response.render(CssHeaderItem.forUrl(getClass().getSimpleName() + ".css"));
	}

}

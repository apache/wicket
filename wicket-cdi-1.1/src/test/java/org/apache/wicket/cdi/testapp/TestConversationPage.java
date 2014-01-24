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
package org.apache.wicket.cdi.testapp;

import javax.enterprise.context.Conversation;
import javax.inject.Inject;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;


/**
 * @author jsarman
 */
public class TestConversationPage extends WebPage
{
	private static final long serialVersionUID = 1L;

	@Inject
	Conversation conversation;

	@Inject
	TestConversationBean counter;

	public TestConversationPage()
	{
		this(new PageParameters());
	}

	public TestConversationPage(final PageParameters parameters)
	{
		super(parameters);

		conversation.begin();
		System.out.println("Opened Conversion with id = " + conversation.getId());

		add(new Label("count", new PropertyModel<String>(this, "counter.countStr")));

		add(new Link<Void>("increment")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick()
			{
				counter.increment();
			}
		});
		add(new Link<Void>("next")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick()
			{
				String pageType = parameters.get("pageType").toString("nonbookmarkable");
				if ("bookmarkable".equals(pageType.toLowerCase()))
					setResponsePage(TestNonConversationalPage.class);
				else
					setResponsePage(new TestNonConversationalPage());
			}
		});

	}
}

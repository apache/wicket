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
package org.apache.wicket.examples.cdi;

import jakarta.enterprise.context.Conversation;
import javax.inject.Inject;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.PropertyModel;

public class ConversationPage1 extends CdiExamplePage
{
	@Inject
	ConversationCounter counter;

	@Inject
	Conversation conversation;

	public ConversationPage1()
	{
		// make the conversation long running
		conversation.begin();

		add(new Label("count", new PropertyModel<Integer>(this, "counter.count")));

		add(new Link<Void>("increment") {
			@Override
			public void onClick()
			{
				counter.increment();
			}
		});

		add(new Link<Void>("next") {
			public void onClick() {
				setResponsePage(new ConversationPage2());
			}
		});
	}
}

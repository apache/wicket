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
package org.apache.wicket.markup.html.form.validation.innerfeedback;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.feedback.FeedbackCollector;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.validation.FormComponentFeedbackBorder;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;

@SuppressWarnings("serial")
class LocalizedFeedbackBorder extends FormComponentFeedbackBorder
{
	private final FeedbackPanel feedback;

	public LocalizedFeedbackBorder(String id)
	{
		super(id);
		WebMarkupContainer brdr = new WebMarkupContainer("border");
		brdr.add(feedback = new FeedbackPanel("feedback", getMessagesFilter()));
		brdr.add(getBodyContainer());
		brdr.add(AttributeModifier.replace("style", new IModel<String>()
		{
			@Override
			public String getObject()
			{
				final IFeedbackMessageFilter filter = feedback.getFilter();

				boolean error = new FeedbackCollector(getPage()).collect(
					new IFeedbackMessageFilter()
					{
						@Override
						public boolean accept(FeedbackMessage message)
						{
							return filter.accept(message) && message.isError();
						}
					}).size() > 0;
				return "border: 1px solid " + (error ? "red" : "green");
			}
		}));
		addToBorder(brdr);
	}

	@Override
	protected void onBeforeRender()
	{
		super.onBeforeRender();
	}
}

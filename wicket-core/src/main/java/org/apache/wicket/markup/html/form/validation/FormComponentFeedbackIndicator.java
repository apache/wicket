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
package org.apache.wicket.markup.html.form.validation;

import org.apache.wicket.Component;
import org.apache.wicket.core.util.string.CssUtils;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.feedback.FeedbackCollector;
import org.apache.wicket.feedback.IFeedback;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;

/**
 * A panel that hides or shows itself depending on whether there are feedback messages for a given
 * message filter. If a component is set using setIndicatorFor(Component), then the indicator is
 * visible when the given component has an error. The default content for this indicator is a red
 * star, but you can subclass this panel and provide your own markup to give any custom look you
 * desire.
 * 
 * @author Jonathan Locke
 */
public class FormComponentFeedbackIndicator extends Panel implements IFeedback
{
	public static final String ERROR_CSS_CLASS_KEY = CssUtils
		.key(FormComponentFeedbackIndicator.class, "error");

	private static final long serialVersionUID = 1L;

	/** The message filter for this indicator component */
	private IFeedbackMessageFilter filter;

	/**
	 * Constructor
	 * 
	 * @param id
	 *            See Component
	 */
	public FormComponentFeedbackIndicator(final String id)
	{
		super(id);
	}

	/**
	 * @param component
	 *            The component to watch for messages
	 */
	public void setIndicatorFor(final Component component)
	{
		filter = new ComponentFeedbackMessageFilter(component);
	}

	@Override
	protected void onInitialize()
	{
		super.onInitialize();
		add(new WebMarkupContainer("indicator")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onComponentTag(ComponentTag tag)
			{
				super.onComponentTag(tag);
				tag.put("class", getString(ERROR_CSS_CLASS_KEY));
			}
		});
	}

	/**
	 * Set the component's visibility according to the existence (or not) of feedback messages
	 */
	@Override
	public void onConfigure()
	{
		super.onConfigure();
		// Get the messages for the current page
		setVisible(new FeedbackCollector(getPage()).collect(getFeedbackMessageFilter()).size() > 0);
	}

	/**
	 * @return Let subclass specify some other filter
	 */
	protected IFeedbackMessageFilter getFeedbackMessageFilter()
	{
		return filter;
	}
}

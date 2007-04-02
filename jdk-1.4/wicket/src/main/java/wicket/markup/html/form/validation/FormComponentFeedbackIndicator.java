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
package wicket.markup.html.form.validation;

import wicket.Component;
import wicket.Session;
import wicket.feedback.ComponentFeedbackMessageFilter;
import wicket.feedback.IFeedback;
import wicket.feedback.IFeedbackMessageFilter;
import wicket.markup.html.panel.Panel;
import wicket.model.IModel;

/**
 * A panel that hides or shows itself depending on whether there are feedback
 * messages for a given message filter. If a component is set using
 * setIndicatorFor(Component), then the indicator is visible when the given
 * component has an error. The default content for this indicator is a red star,
 * but you can subclass this panel and provide your own markup to give any
 * custom look you desire.
 * 
 * @author Jonathan Locke
 */
public class FormComponentFeedbackIndicator extends Panel implements IFeedback
{
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
	 * @see wicket.Component#Component(String, IModel)
	 */
	public FormComponentFeedbackIndicator(final String id, IModel model)
	{
		super(id, model);
	}

	/**
	 * @param component
	 *            The component to watch for messages
	 */
	public void setIndicatorFor(final Component component)
	{
		filter = new ComponentFeedbackMessageFilter(component);
	}

	/**
	 * @see wicket.feedback.IFeedback#updateFeedback()
	 */
	public void updateFeedback()
	{
		// Get the messages for the current page
		setVisible(Session.get().getFeedbackMessages().hasMessage(getFeedbackMessageFilter()));
	}

	/**
	 * @return Let subclass specify some other filter
	 */
	protected IFeedbackMessageFilter getFeedbackMessageFilter()
	{
		return filter;
	}
}

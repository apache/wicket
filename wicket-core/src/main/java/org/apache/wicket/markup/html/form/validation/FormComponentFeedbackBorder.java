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

import org.apache.wicket.feedback.ContainerFeedbackMessageFilter;
import org.apache.wicket.feedback.FeedbackCollector;
import org.apache.wicket.feedback.IFeedback;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.border.Border;

/**
 * A border that can be placed around a form component to indicate when the bordered child/children
 * has a validation error. A child of the border named "errorIndicator" will be shown and hidden
 * depending on whether the child has an error. A typical error indicator might be a little red
 * asterisk.
 * <p>
 * <strong>Note: </strong> Since this border checks its children do not use
 * TransparentWebMarkupContainer and add the children directly into the border
 * 
 * @author Jonathan Locke
 * @author Eelco Hillenius
 */
public class FormComponentFeedbackBorder extends Border implements IFeedback
{
	private static final long serialVersionUID = 1L;

	/** Visible property cache. */
	private boolean visible;

	/**
	 * Error indicator that will be shown whenever there is an error-level message for the
	 * collecting component.
	 */
	private final class ErrorIndicator extends WebMarkupContainer
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 * 
		 * @param id
		 *            component id
		 */
		public ErrorIndicator(String id)
		{
			super(id);
		}

		/**
		 * @see org.apache.wicket.Component#isVisible()
		 */
		@Override
		public boolean isVisible()
		{
			return visible;
		}
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            See Component
	 */
	public FormComponentFeedbackBorder(final String id)
	{
		super(id);
		addToBorder(new ErrorIndicator("errorIndicator"));
	}

	/**
	 * Update the 'visible' flag to indicate the existence (or lack thereof) of feedback messages
	 */
	@Override
	protected void onBeforeRender()
	{
		super.onBeforeRender();
		// Get the messages for the current page
		visible = new FeedbackCollector(getPage()).collect(getMessagesFilter()).size() > 0;
	}

	/**
	 * @return Let subclass specify some other filter
	 */
	protected IFeedbackMessageFilter getMessagesFilter()
	{
		return new ContainerFeedbackMessageFilter(this);
	}
}

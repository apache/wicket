/*
 * $Id: FormComponentFeedbackBorder.java,v 1.3 2005/01/02 21:58:21 jonathanlocke
 * Exp $ $Revision$ $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.markup.html.form.validation;

import wicket.Component;
import wicket.FeedbackMessages;
import wicket.IFeedback;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.border.Border;

/**
 * A border that can be placed around a form component to indicate when the
 * bordered child has a validation error. A child of the border named
 * "errorIndicator" will be shown and hidden depending on whether the child has
 * an error. A typical error indicator might be a little red asterisk.
 * 
 * @author Jonathan Locke
 * @author Eelco Hillenius
 */
public class FormComponentFeedbackBorder extends Border implements IFeedback
{
	/** The error indicator child which should be shown if an error occurs. */
	private final ErrorIndicator errorIndicator;

	/**
	 * Optional collecting component. When this is not set explicitly, the children of
	 * this border will be used for searching for components.
	 */
	private Component collectingComponent;

	/** visible property cache. */
	private boolean visible;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            See Component
	 */
	public FormComponentFeedbackBorder(final String id)
	{
		super(id);
		add(errorIndicator = new ErrorIndicator("errorIndicator"));
	}

	/**
	 * @see wicket.IFeedback#setCollectingComponent(wicket.Component)
	 */
	public void setCollectingComponent(Component collectingComponent)
	{
		this.collectingComponent = collectingComponent;
	}

	/**
	 * @see wicket.Component#onBeginRequest()
	 */
	protected void onBeginRequest()
	{
		// get the messages for the current page
		final FeedbackMessages feedbackMessages = getPage().getFeedbackMessages();

		if (collectingComponent != null)
		{
			// use the one that was explicitly set
			visible = feedbackMessages.hasErrorMessageFor(collectingComponent);
		}
		else
		{
			// search through all children (typically this will be one and will be
			// a form component, but it is possible to group components) for an error message
			ErrorMessageVisitor errorMessageVisitor = new ErrorMessageVisitor(feedbackMessages);
			FormComponentFeedbackBorder.this.visitChildren(errorMessageVisitor);
			visible = errorMessageVisitor.foundErrorMessage;
		}
	}

	/**
	 * Error indicator that will be shown whenever there is an error-level message
	 * for the collecting component.
	 */
	private final class ErrorIndicator extends WebMarkupContainer
	{
		/**
		 * Construct.
		 * @param id component id
		 */
		public ErrorIndicator(String id)
		{
			super(id);
		}

		/**
		 * @see wicket.Component#isVisible()
		 */
		public boolean isVisible()
		{
			return visible;
		}
	}

	/**
	 * Visits all childs to look for any registered error messages.
	 */
	private static final class ErrorMessageVisitor implements IVisitor
	{
		final FeedbackMessages feedbackMessages;
		boolean foundErrorMessage = false;

		/**
		 * Construct.
		 * @param feedbackMessages
		 */
		public ErrorMessageVisitor(FeedbackMessages feedbackMessages)
		{
			this.feedbackMessages = feedbackMessages;
		}

		/**
		 * @see wicket.Component.IVisitor#component(wicket.Component)
		 */
		public Object component(Component component)
		{
			if (feedbackMessages.hasErrorMessageFor(component))
			{
				foundErrorMessage = true;
				return IVisitor.STOP_TRAVERSAL;
			}
			return IVisitor.CONTINUE_TRAVERSAL;
		}
	}
}

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
import wicket.IFeedback;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.border.Border;

/**
 * A border that can be placed around a form bordered to indicate when the
 * bordered child has a validation error. A child of the border named
 * "errorIndicator" will be shown and hidden depending on whether the child has
 * an error. A typical error indicator might be a little red asterisk.
 * 
 * @author Jonathan Locke
 * @author Eelco Hillenius
 */
public final class FormComponentFeedbackBorder extends Border implements IFeedback
{
	/** The error indicator child which should be shown if an error occurs. */
	private final WebMarkupContainer errorIndicator;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            See Component
	 */
	public FormComponentFeedbackBorder(final String id)
	{
		super(id);

		// Create invisible error indicator bordered that will be shown when a
		// validation error occurs
		errorIndicator = new WebMarkupContainer("errorIndicator");
		errorIndicator.setVisible(false);
		add(errorIndicator);
	}

	/**
	 * Handles feedback. If any errors were registered on child components of
	 * this feedback border, the decorated error indicator will be set to
	 * visible.
	 * 
	 * @see IFeedback#addFeedbackMessages(Component, boolean)
	 */
	public void addFeedbackMessages(final Component component, final boolean recurse)
	{
		errorIndicator.setVisible(getPage().getFeedbackMessages().messages(this, true).size() > 0);
	}

	/**
	 * @see wicket.MarkupContainer#onEndRequest()
	 */
	protected void onEndRequest()
	{
		// Clear feedback
		errorIndicator.setVisible(false);
	}
}

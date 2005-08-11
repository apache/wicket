/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.examples.compref;

import wicket.examples.WicketExamplePage;
import wicket.markup.html.form.Button;
import wicket.markup.html.form.Form;
import wicket.markup.html.panel.FeedbackPanel;

/**
 * Page with examples on {@link wicket.markup.html.form.Button}.
 *
 * @author Eelco Hillenius
 */
public class ButtonPage extends WicketExamplePage
{
	/**
	 * Constructor
	 */
	public ButtonPage()
	{
		// Add a FeedbackPanel for displaying our messages
		FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
		add(feedbackPanel);

		// Add a form with an onSumbit implementation that sets a message
		Form form = new Form("form", feedbackPanel)
		{
			protected void onSubmit()
			{
				info("Form.onSubmit executed");
			}
		};

		Button button1 = new Button("button1")
		{
			protected void onSubmit()
			{
				info("button1.onSubmit executed");
			}
		};
		form.add(button1);

		Button button2 = new Button("button2")
		{
			protected void onSubmit()
			{
				info("button2.onSubmit executed");
			}
		};
		button2.setDefaultFormProcessing(false);
		form.add(button2);

		add(form);
	}
}
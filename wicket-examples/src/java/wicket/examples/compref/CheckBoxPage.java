/*
 * $Id$ $Revision$ $Date$
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.examples.compref;

import java.io.Serializable;

import wicket.examples.WicketExamplePage;
import wicket.markup.html.form.CheckBox;
import wicket.markup.html.form.Form;
import wicket.markup.html.panel.FeedbackPanel;
import wicket.model.CompoundPropertyModel;

/**
 * Page with examples on {@link wicket.markup.html.form.TextArea}.
 * 
 * @author Eelco Hillenius
 */
public class CheckBoxPage extends WicketExamplePage
{
	/**
	 * Constructor
	 */
	public CheckBoxPage()
	{
		final Input input = new Input();
		setModel(new CompoundPropertyModel<Input>(input));

		// Add a FeedbackPanel for displaying our messages
		FeedbackPanel feedbackPanel = new FeedbackPanel(this, "feedback");

		// Add a form with an onSumbit implementation that sets a message
		Form form = new Form(this, "form")
		{
			@Override
			protected void onSubmit()
			{
				if (input.bool.booleanValue())
				{
					info("Ok, ok... we'll check it");
				}
				else
				{
					info("So you don't want it checked huh?");
				}
			}
		};

		// add a check box component that uses the model object's 'bool'
		// property.
		new CheckBox(form, "bool");
	}

	/** Simple data class that acts as a model for the input fields. */
	private static class Input implements Serializable
	{
		/** a boolean. */
		public Boolean bool = Boolean.TRUE;

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			return "bool = '" + bool + "'";
		}
	}

	/**
	 * Override base method to provide an explanation
	 */
	@Override
	protected void explain()
	{
		String html = "<input type=\"checkbox\" wicket:id=\"bool\" />";
		String code = "&nbsp;&nbsp;&nbsp;&nbsp;// add a check box component that uses the model object's 'bool' property.\n"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;form.add(new CheckBox(\"bool\"));";
		new ExplainPanel(this, html, code);

	}

}
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


import wicket.examples.WicketExamplePage;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.TextArea;
import wicket.markup.html.panel.FeedbackPanel;
import wicket.model.CompoundPropertyModel;

/**
 * Page with examples on {@link wicket.markup.html.form.TextArea}.
 * 
 * @author Eelco Hillenius
 */
public class TextAreaPage extends WicketExamplePage<TextAreaPageInput>
{
	/**
	 * Constructor
	 */
	public TextAreaPage()
	{
		final TextAreaPageInput textAreaPageInput = new TextAreaPageInput();
		setModel(new CompoundPropertyModel<TextAreaPageInput>(textAreaPageInput));

		// Add a FeedbackPanel for displaying our messages
		new FeedbackPanel(this, "feedback");

		// Add a form with an onSumbit implementation that sets a message
		Form form = new Form(this, "form")
		{
			@Override
			protected void onSubmit()
			{
				info("input: " + textAreaPageInput);
			}
		};

		// add a text area component that uses Input's 'text' property.
		new TextArea(form, "text");
	}

	/**
	 * Override base method to provide an explanation
	 */
	@Override
	protected void explain()
	{
		String html = "<textarea wicket:id=\"text\" rows=\"6\" cols=\"20\">Input comes here</textarea>";
		String code = "&nbsp;&nbsp;&nbsp;&nbsp;// add a text area component that uses the model object's 'text' property.\n"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;form.add(new TextArea(\"text\"));";
		new ExplainPanel(this, html, code);
	}

}
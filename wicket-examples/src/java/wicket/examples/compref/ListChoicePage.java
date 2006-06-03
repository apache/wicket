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
import wicket.markup.html.form.ListChoice;
import wicket.markup.html.panel.FeedbackPanel;
import wicket.model.CompoundPropertyModel;

/**
 * Page with examples on {@link wicket.markup.html.form.ListChoice}.
 * 
 * @author Eelco Hillenius
 */
public class ListChoicePage extends WicketExamplePage<ListChoicePageInput>
{
	/**
	 * Constructor
	 */
	public ListChoicePage()
	{
		final ListChoicePageInput listChoicePageInput = new ListChoicePageInput();
		setModel(new CompoundPropertyModel<ListChoicePageInput>(listChoicePageInput));

		// Add a FeedbackPanel for displaying our messages
		FeedbackPanel feedbackPanel = new FeedbackPanel(this, "feedback");

		// Add a form with an onSumbit implementation that sets a message
		Form form = new Form(this, "form")
		{
			@Override
			protected void onSubmit()
			{
				info("input: " + listChoicePageInput);
			}
		};

		// Add a list choice component that uses Input's 'site' property to
		// designate the
		// current selection, and that uses the SITES list for the available
		// options.
		ListChoice<String> listChoice = new ListChoice<String>(form, "site", ListChoicePageInput.SITES);
		listChoice.setMaxRows(4);
	}

	/**
	 * Override base method to provide an explanation
	 */
	@Override
	protected void explain()
	{
		String html = "<select wicket:id=\"site\">\n" + "    <option>site 1</option>\n"
				+ "    <option>site 2</option>\n" + "</select>";
		String code = "private static final List SITES = Arrays.asList(new String[] { \"The Server Side\", \"Java Lobby\", \"Java.Net\" });\n"
				+ "...\n"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;// Add a list choice component that uses the model object's 'site' property to designate the\n"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;// current selection, and that uses the SITES list for the available options.\n"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;ListChoice listChoice = new ListChoice(\"site\", SITES);\n"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;listChoice.setMaxRows(4);\n"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;form.add(listChoice);";
		new ExplainPanel(this, html, code);

	}

}
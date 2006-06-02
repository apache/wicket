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
import java.util.Arrays;
import java.util.List;

import wicket.examples.WicketExamplePage;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.RadioChoice;
import wicket.markup.html.panel.FeedbackPanel;
import wicket.model.CompoundPropertyModel;

/**
 * Page with examples on {@link wicket.markup.html.form.ListChoice}.
 * 
 * @author Eelco Hillenius
 */
public class RadioChoicePage extends WicketExamplePage
{
	/** available sites for selection. */
	private static final List<String> SITES = Arrays.asList(new String[] { "The Server Side", "Java Lobby",
			"Java.Net" });

	/**
	 * Constructor
	 */
	public RadioChoicePage()
	{
		final Input input = new Input();
		setModel(new CompoundPropertyModel(input));

		// Add a FeedbackPanel for displaying our messages
		FeedbackPanel feedbackPanel = new FeedbackPanel(this, "feedback");

		// Add a form with an onSumbit implementation that sets a message
		Form form = new Form(this, "form")
		{
			@Override
			protected void onSubmit()
			{
				info("input: " + input);
			}
		};

		// Add a radio choice component that uses Input's 'site' property to
		// designate the
		// current selection, and that uses the SITES list for the available
		// options.
		new RadioChoice<String>(form, "site", SITES);
	}

	/** Simple data class that acts as a model for the input fields. */
	private static class Input implements Serializable
	{
		/** the selected site. */
		public String site = (String)SITES.get(0);

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			return "site = '" + site + "'";
		}
	}

	/**
	 * Override base method to provide an explanation
	 */
	@Override
	protected void explain()
	{
		String html = "<span valign=\"top\" wicket:id=\"site\">\n"
				+ "  <input type=\"radio\">site 1</input>\n"
				+ "  <input type=\"radio\">site 2</input>\n" + "</span>";
		String code = "private static final List SITES = Arrays.asList(new String[] { \"The Server Side\", \"Java Lobby\", \"Java.Net\" });\n"
				+ "...\n"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;// Add a radio choice component that uses the model object's 'site' property to designate the\n"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;// current selection, and that uses the SITES list for the available options.\n"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;form.add(new RadioChoice(\"site\", SITES));";
		new ExplainPanel(this, html, code);

	}

}
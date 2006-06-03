/*
 * $Id: CheckBoxMultipleChoicePage.java 5838 2006-05-24 20:44:49 +0000 (Wed, 24
 * May 2006) joco01 $ $Revision$ $Date: 2006-05-24 20:44:49 +0000 (Wed,
 * 24 May 2006) $
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import wicket.examples.WicketExamplePage;
import wicket.markup.html.form.CheckBoxMultipleChoice;
import wicket.markup.html.form.Form;
import wicket.markup.html.panel.FeedbackPanel;
import wicket.model.CompoundPropertyModel;

/**
 * Page with examples on {@link wicket.markup.html.form.CheckBoxMultipleChoice}.
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class CheckBoxMultipleChoicePage extends WicketExamplePage
{
	/** available sites for selection. */
	private static final List<String> SITES = Arrays.asList(new String[] { "The Server Side", "Java Lobby",
			"Java.Net" });

	/** available choices for large selection box. */
	private static final List<String> MANY_CHOICES = Arrays.asList(new String[] { "Choice1", "Choice2",
			"Choice3", "Choice4", "Choice5", "Choice6", "Choice7", "Choice8", "Choice9", });

	/**
	 * Constructor
	 */
	public CheckBoxMultipleChoicePage()
	{
		final Input input = new Input();
		setModel(new CompoundPropertyModel<Input>(input));

		// Add a FeedbackPanel for displaying our messages
		FeedbackPanel feedbackPanel = new FeedbackPanel(this, "feedback");

		// Add a form with an onSubmit implementation that sets a message
		Form form = new Form(this, "form")
		{
			@Override
			protected void onSubmit()
			{
				info("input: " + input);
			}
		};

		// add a couple of checkbox multiple choice components, notice the model
		// used is a compound model set on the page
		CheckBoxMultipleChoice<String> listChoice = new CheckBoxMultipleChoice<String>(form, "sites", SITES);

		listChoice = new CheckBoxMultipleChoice<String>(form, "choices", MANY_CHOICES);
	}

	/** Simple data class that acts as a model for the input fields. */
	private static class Input implements Serializable
	{
		/** the selected sites. */
		public List<String> sites = new ArrayList<String>();

		/** the selected choices. */
		public List<String> choices = new ArrayList<String>();

		/** adds pre-selected items to the choices list */
		public Input()
		{
			choices.add("Choice3");
			choices.add("Choice4");
			choices.add("Choice5");
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			return "sites = '" + listAsString(sites) + "', choices='" + listAsString(choices) + "'";
		}

		private String listAsString(List list)
		{
			StringBuffer b = new StringBuffer();
			for (Iterator i = list.iterator(); i.hasNext();)
			{
				b.append(i.next());
				if (i.hasNext())
				{
					b.append(", ");
				}
			}
			return b.toString();
		}
	}

	/**
	 * Override base method to provide an explanation
	 */
	@Override
	protected void explain()
	{
		String html = "<span wicket:id=\"sites\">\n" + "</span>\n"
				+ "<span wicket:id=\"choices\">\n" + "</span>";
		String code = "&nbsp;&nbsp;&nbsp;&nbsp;CheckBoxMultipleChoice siteChoice = new CheckBoxMultipleChoice(\"sites\", SITES);\n"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;form.add(siteChoice);\n"
				+ "\n"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;ListMultipleChoice manyChoice = new CheckBoxMultipleChoice(\"choices\", MANY_CHOICES);\n"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;form.add(manyChoice);";
		new ExplainPanel(this, html, code);

	}
}

/*
 * $Id$ $Revision$
 * $Date$
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
import wicket.extensions.markup.html.form.select.IOptionRenderer;
import wicket.extensions.markup.html.form.select.Select;
import wicket.extensions.markup.html.form.select.SelectOption;
import wicket.extensions.markup.html.form.select.SelectOptions;
import wicket.markup.html.form.Form;
import wicket.markup.html.panel.FeedbackPanel;
import wicket.model.CompoundPropertyModel;
import wicket.model.IModel;
import wicket.model.Model;

/**
 * Page with examples on
 * {@link wicket.extensions.markup.html.form.select.Select}.
 * 
 * @author Igor Vaynberg (ivaynberg)
 * @author Eelco Hillenius
 */
public class SelectPage extends WicketExamplePage
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
	public SelectPage()
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

		Select site = new Select(form, "site");
		new SelectOption<String>(site, "site1", new Model<String>("tss"));
		new SelectOption<String>(site, "site2", new Model<String>("jl"));
		new SelectOption<String>(site, "site3", new Model<String>("sd"));
		new SelectOption<String>(site, "site4", new Model<String>("bn"));

		Select choices = new Select(form, "choices");
		IOptionRenderer renderer = new IOptionRenderer()
		{
			public String getDisplayValue(Object object)
			{
				return object.toString();
			}

			public IModel getModel(Object value)
			{
				return new Model(value);
			}
		};
		new SelectOptions(choices, "manychoices", new Model<List<String>>(MANY_CHOICES), renderer);
	}

	/** Simple data class that acts as a model for the input fields. */
	private static class Input implements Serializable
	{
		/** the selected sites. */
		public String site = new String("sd");

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
			return "site = '" + site + "', choices='" + listAsString(choices) + "'";
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
		String html = "<select wicket:id=\"sites\">\n" + "    <option>site 1</option>\n"
				+ "    <option>site 2</option>\n" + "</select>\n"
				+ "<select wicket:id=\"choices\">\n" + "    <option>choice 1</option>\n"
				+ "    <option>choice 2</option>\n" + "</select>";
		String code = "&nbsp;&nbsp;&nbsp;&nbsp;// Add a multiple list choice component that uses the model object's 'site'\n"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;// property to designate the current selection, and that uses the SITES\n"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;// list for the available options.\n"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;// Note that our model here holds a Collection, as we need to store\n"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;// multiple values too\n"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;ListMultipleChoice siteChoice = new ListMultipleChoice(\"sites\", SITES);\n"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;form.add(siteChoice);\n"
				+ "\n"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;ListMultipleChoice manyChoice = new ListMultipleChoice(\"choices\", MANY_CHOICES).setMaxRows(5);\n"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;form.add(manyChoice);";

		// TODO Post 1.2: General: Unknown todo
		html = "SEE INSIDE FOR NOW";
		code = "SEE INSIDE FOR NOW";

		new ExplainPanel(this, html, code);

	}
}
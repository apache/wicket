/*
 * $Id: org.eclipse.jdt.ui.prefs 5004 2006-03-17 20:47:08 -0800 (Fri, 17 Mar
 * 2006) eelco12 $ $Revision$ $Date: 2006-03-17 20:47:08 -0800 (Fri, 17
 * Mar 2006) $
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
package wicket.util.tester.apps_3;

import java.util.ArrayList;
import java.util.List;

import wicket.markup.html.WebPage;
import wicket.markup.html.form.Button;
import wicket.markup.html.form.Check;
import wicket.markup.html.form.CheckBoxMultipleChoice;
import wicket.markup.html.form.CheckGroup;
import wicket.markup.html.form.ChoiceRenderer;
import wicket.markup.html.form.DropDownChoice;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.ListChoice;
import wicket.markup.html.form.ListMultipleChoice;
import wicket.markup.html.form.Radio;
import wicket.markup.html.form.RadioChoice;
import wicket.markup.html.form.RadioGroup;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;
import wicket.model.CompoundPropertyModel;
import wicket.model.Model;
import wicket.util.tester.apps_1.Book;

/**
 * @author Ingram Chen
 */
public class ChoicePage extends WebPage
{
	private static final long serialVersionUID = 1L;

	/** test DropDownChoice */
	public Book dropDownChoice;
	
	/** test ListChoice */
	public Book listChoice;
	
	/** test RadioChoice */
	public Book radioChoice;

	/** test RadioChoice */
	public Book radioGroup;

	/** test CheckBoxMultipleChoice */
	public List checkBoxMultipleChoice = new ArrayList();

	/** test CheckGroup */
	public List checkGroup = new ArrayList();
	
	/** test ListMultipleChoice */
	public List listMultipleChoice = new ArrayList();

	/** test multiple button */
	public boolean anotherButtonPressed;

	/**
	 * Test page for FormTester.select()
	 * 
	 * @param candidateChoices
	 */
	public ChoicePage(List candidateChoices)
	{
		ChoiceRenderer bookChoiceRenderer = new ChoiceRenderer("name", "id");

		Form form = new Form("choiceForm");
		add(form);

		form.setModel(new CompoundPropertyModel(this));

		// single select family
		form.add(new DropDownChoice("dropDownChoice", candidateChoices, bookChoiceRenderer));
		form.add(new ListChoice("listChoice", candidateChoices, bookChoiceRenderer).setMaxRows(4));
		form.add(new RadioChoice("radioChoice", candidateChoices, bookChoiceRenderer));
		form.add(newRadioGroup(candidateChoices));

		// mulitple select family
		form.add(new ListMultipleChoice("listMultipleChoice", candidateChoices, bookChoiceRenderer)
				.setMaxRows(4));
		form.add(new CheckBoxMultipleChoice("checkBoxMultipleChoice", candidateChoices,
				bookChoiceRenderer));
		form.add(newCheckGroup(candidateChoices));
		form.add(new Button("anotherButton")
		{
			private static final long serialVersionUID = 1L;

			public void onSubmit()
			{
				anotherButtonPressed = true;
			}
		});
	}

	private CheckGroup newCheckGroup(List candidateChoices)
	{
		CheckGroup checkGroupComponent = new CheckGroup("checkGroup");
		ListView listView = new ListView("loop", candidateChoices)
		{
			private static final long serialVersionUID = 1L;

			protected void populateItem(ListItem item)
			{
				item.add(new Check("check", new Model((Book)item.getModelObject())));
			}

		};
		checkGroupComponent.add(listView);
		return checkGroupComponent;
	}

	private RadioGroup newRadioGroup(List candidateChoices)
	{
		RadioGroup radioGroupComponent = new RadioGroup("radioGroup");
		ListView listView = new ListView("loop", candidateChoices)
		{
			private static final long serialVersionUID = 1L;

			protected void populateItem(ListItem item)
			{
				item.add(new Radio("radio", new Model((Book)item.getModelObject())));
			}

		};
		radioGroupComponent.add(listView);
		return radioGroupComponent;
	}
}

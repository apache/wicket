/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.util.tester.apps_3;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Check;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.CheckBoxMultipleChoice;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.ListChoice;
import org.apache.wicket.markup.html.form.ListMultipleChoice;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.apps_1.Book;


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

	/** test CheckBox initial value */
	public boolean checkBox;

	/** test CheckGroup initial value */
	public List initialCheckGroup = new ArrayList();

	/** test ListMultipleChoice initial values */
	public List initialListMultipleChoice = new ArrayList();

	/** test CheckBoxMultipleChoice initial values */
	public List initialCheckBoxMultipleChoice = new ArrayList();

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

		// setting initial values
		dropDownChoice = (Book) candidateChoices.get(1);
		listChoice = (Book) candidateChoices.get(3);
		radioChoice = (Book) candidateChoices.get(2);
		checkBox = true;
		initialListMultipleChoice.add(candidateChoices.get(1));
		initialListMultipleChoice.add(candidateChoices.get(2));
		initialCheckBoxMultipleChoice.add(candidateChoices.get(0));
		initialCheckBoxMultipleChoice.add(candidateChoices.get(3));
		initialCheckGroup.add(candidateChoices.get(2));
		initialCheckGroup.add(candidateChoices.get(3));

		// single select family
		form.add(new DropDownChoice("dropDownChoice", candidateChoices, bookChoiceRenderer));
		form.add(new ListChoice("listChoice", candidateChoices, bookChoiceRenderer).setMaxRows(4));
		form.add(new RadioChoice("radioChoice", candidateChoices, bookChoiceRenderer));
		form.add(new CheckBox("checkBox"));
		form.add(newRadioGroup(candidateChoices));

		// multiple select family
		form.add(new ListMultipleChoice("initialListMultipleChoice", candidateChoices, bookChoiceRenderer));
		form.add(new CheckBoxMultipleChoice("initialCheckBoxMultipleChoice", candidateChoices, bookChoiceRenderer));
		form.add(newCheckGroup("initialCheckGroup", candidateChoices));
		form.add(new ListMultipleChoice("listMultipleChoice", candidateChoices, bookChoiceRenderer)
				.setMaxRows(4));
		form.add(new CheckBoxMultipleChoice("checkBoxMultipleChoice", candidateChoices,
				bookChoiceRenderer));
		form.add(newCheckGroup("checkGroup", candidateChoices));
		form.add(new Button("anotherButton")
		{
			private static final long serialVersionUID = 1L;

			public void onSubmit()
			{
				anotherButtonPressed = true;
			}
		});
	}

	private CheckGroup newCheckGroup(final String id, List candidateChoices)
	{
		CheckGroup checkGroupComponent = new CheckGroup(id);
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

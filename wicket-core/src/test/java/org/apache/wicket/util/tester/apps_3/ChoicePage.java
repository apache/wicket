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

import java.io.Serializable;
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
	public List<Book> initialCheckGroup = new ArrayList<Book>();

	/** test ListMultipleChoice initial values */
	public List<Book> initialListMultipleChoice = new ArrayList<Book>();

	/** test CheckBoxMultipleChoice initial values */
	public List<Book> initialCheckBoxMultipleChoice = new ArrayList<Book>();

	/** test CheckBoxMultipleChoice */
	public List<Book> checkBoxMultipleChoice = new ArrayList<Book>();

	/** test CheckGroup */
	public List<Book> checkGroup = new ArrayList<Book>();

	/** test ListMultipleChoice */
	public List<Book> listMultipleChoice = new ArrayList<Book>();

	/** test multiple button */
	public boolean buttonPressed;

	/** test multiple button */
	public boolean anotherButtonPressed;

	/**
	 * Test page for FormTester.select()
	 * 
	 * @param candidateChoices
	 */
	public ChoicePage(List<Book> candidateChoices)
	{
		ChoiceRenderer<Book> bookChoiceRenderer = new ChoiceRenderer<Book>("name", "id");

		Form<ChoicePage> form = new Form<ChoicePage>("choiceForm");
		add(form);

		form.setDefaultModel(new CompoundPropertyModel<ChoicePage>(this));

		// setting initial values
		dropDownChoice = candidateChoices.get(1);
		listChoice = candidateChoices.get(3);
		radioChoice = candidateChoices.get(2);
		checkBox = true;
		initialListMultipleChoice.add(candidateChoices.get(1));
		initialListMultipleChoice.add(candidateChoices.get(2));
		initialCheckBoxMultipleChoice.add(candidateChoices.get(0));
		initialCheckBoxMultipleChoice.add(candidateChoices.get(3));
		initialCheckGroup.add(candidateChoices.get(2));
		initialCheckGroup.add(candidateChoices.get(3));

		// single select family
		form.add(new DropDownChoice<Book>("dropDownChoice", candidateChoices, bookChoiceRenderer));
		form.add(new ListChoice<Book>("listChoice", candidateChoices, bookChoiceRenderer).setMaxRows(4));
		form.add(new RadioChoice<Book>("radioChoice", candidateChoices, bookChoiceRenderer));
		form.add(new CheckBox("checkBox"));
		form.add(newRadioGroup(candidateChoices));

		// multiple select family
		form.add(new ListMultipleChoice<Book>("initialListMultipleChoice", candidateChoices,
			bookChoiceRenderer));
		form.add(new CheckBoxMultipleChoice<Book>("initialCheckBoxMultipleChoice",
			candidateChoices, bookChoiceRenderer));
		form.add(newCheckGroup("initialCheckGroup", candidateChoices));
		form.add(new ListMultipleChoice<Book>("listMultipleChoice", candidateChoices,
			bookChoiceRenderer).setMaxRows(4));
		form.add(new CheckBoxMultipleChoice<Book>("checkBoxMultipleChoice", candidateChoices,
			bookChoiceRenderer));
		form.add(newCheckGroup("checkGroup", candidateChoices));
		form.add(new Button("buttonWithModel", Model.of("ButtonWithModel"))
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit()
			{
				buttonPressed = true;
			}
		});
		form.add(new Button("anotherButton")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit()
			{
				anotherButtonPressed = true;
			}
		});
	}

	private <S extends Serializable> CheckGroup<S> newCheckGroup(final String id,
		List<S> candidateChoices)
	{
		CheckGroup<S> checkGroupComponent = new CheckGroup<S>(id);
		ListView<S> listView = new ListView<S>("loop", candidateChoices)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<S> item)
			{
				item.add(new Check<S>("check", new Model<S>(item.getModelObject())));
			}

		};
		checkGroupComponent.add(listView);
		return checkGroupComponent;
	}

	private RadioGroup<Book> newRadioGroup(List<Book> candidateChoices)
	{
		RadioGroup<Book> radioGroupComponent = new RadioGroup<Book>("radioGroup");
		ListView<Book> listView = new ListView<Book>("loop", candidateChoices)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<Book> item)
			{
				item.add(new Radio<Book>("radio", new Model<Book>(item.getModelObject())));
			}

		};
		radioGroupComponent.add(listView);
		return radioGroupComponent;
	}
}

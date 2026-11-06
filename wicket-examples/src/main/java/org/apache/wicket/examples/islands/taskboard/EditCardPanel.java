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
package org.apache.wicket.examples.islands.taskboard;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LambdaModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.validator.StringValidator;

/**
 * The form of the edit dialog: classic Wicket fields next to two form islands, the description's
 * rich-text editor and the due date's picker. All of them are validated and submitted together.
 */
public abstract class EditCardPanel extends Panel
{
	private static final long serialVersionUID = 1L;

	/**
	 * @param id
	 *            the component id
	 * @param board
	 *            the board
	 * @param cardId
	 *            the card to edit
	 */
	public EditCardPanel(String id, IModel<Board> board, String cardId)
	{
		super(id);

		Board current = board.getObject();
		Form<CardForm> form = new Form<>("form", new CompoundPropertyModel<>(
			CardForm.of(current.findCard(cardId).orElseThrow())));
		add(form);

		form.add(new FeedbackPanel("feedback"));
		form.add(new TextField<String>("title").setRequired(true)
			.add(StringValidator.lengthBetween(2, 80))
			.setLabel(Model.of("Title")));
		form.add(new RichTextField("description", LambdaModel.of(form.getModel(),
			CardForm::getDescription, CardForm::setDescription)).setLabel(Model.of("Description")));
		form.add(new DueDateField("due",
			LambdaModel.of(form.getModel(), CardForm::getDue, CardForm::setDue))
			.setLabel(Model.of("Due")));
		form.add(new DropDownChoice<>("assigneeId", current.members()
			.stream()
			.map(Member::id)
			.toList(), new ChoiceRenderer<String>()
			{
				private static final long serialVersionUID = 1L;

				@Override
				public Object getDisplayValue(String memberId)
				{
					return current.findMember(memberId).map(Member::name).orElse(memberId);
				}
			}).setNullValid(true).setLabel(Model.of("Assignee")));

		form.add(new AjaxButton("save")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target)
			{
				boolean saved = TaskBoardApplication.get()
					.getRepository()
					.update(current.id(), cardId, form.getModelObject().toEdit());
				board.detach();
				if (saved)
				{
					onSaved(target);
				}
				else
				{
					form.error("The card is no longer on the board.");
					target.add(form);
				}
			}

			@Override
			protected void onError(AjaxRequestTarget target)
			{
				target.add(form);
			}
		});
		form.add(new AjaxLink<Void>("cancel")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				onCancel(target);
			}
		});
		form.setOutputMarkupId(true);
	}

	/**
	 * Called after the card was saved.
	 *
	 * @param target
	 *            the target of the request
	 */
	protected abstract void onSaved(AjaxRequestTarget target);

	/**
	 * Called when the user closes the dialog without saving.
	 *
	 * @param target
	 *            the target of the request
	 */
	protected abstract void onCancel(AjaxRequestTarget target);
}

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
import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalDialog;
import org.apache.wicket.extensions.ajax.markup.html.modal.theme.DefaultTheme;
import org.apache.wicket.islands.Island;
import org.apache.wicket.islands.IslandSettings;
import org.apache.wicket.islands.modules.ViteManifestModuleLoader;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.CssResourceReference;

/**
 * The board page. The board lives in an application-wide repository, not in the page: the page
 * holds only the board's id and loads it for every request.
 */
public class TaskBoardPage extends WicketExamplePage
{
	private static final long serialVersionUID = 1L;

	private final String boardId;

	private final IModel<Board> board;

	private final IModel<String> teammateNote = Model.of("");

	private final IModel<String> openedCard = Model.of();

	public TaskBoardPage()
	{
		boardId = TaskBoardApplication.get().getBoardId();
		board = LoadableDetachableModel
			.of(() -> TaskBoardApplication.get().getRepository().get(boardId));

		add(new WebMarkupContainer("noBuild")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onConfigure()
			{
				super.onConfigure();
				setVisible(IslandSettings.get()
					.getModuleLoader() instanceof ViteManifestModuleLoader loader &&
					loader.getManifest() == null);
			}
		});

		Label note = new Label("teammateNote", teammateNote);
		note.setOutputMarkupId(true);
		add(note);
		add(new AjaxLink<Void>("teammate")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				String title = TaskBoardApplication.get()
					.getRepository()
					.moveForTeammate(boardId);
				teammateNote.setObject(title == null ? "There was nothing left to move."
					: "A teammate moved ‘" + title + "’ to Review. This page does not know yet.");
				target.add(note);
			}
		});

		add(new Island<Void>("filter", "board-filter", () -> null)
			.usePageless("members", "members", () -> new BoardRef(boardId)));
		add(new Island<Void>("throughput", "cards-per-week", () -> null)
			.usePageless("throughput", "throughput", () -> new BoardRef(boardId)));

		SummaryPanel summary = new SummaryPanel("summary", board);
		add(summary);
		CardPreview preview = new CardPreview("preview", board, openedCard);
		add(preview);

		ModalDialog dialog = new ModalDialog("dialog");
		dialog.add(new DefaultTheme());
		dialog.closeOnEscape();
		dialog.trapFocus();
		add(dialog);

		add(new TaskBoard("board", board, summary)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onOpen(String cardId, AjaxRequestTarget target)
			{
				TaskBoard taskBoard = this;
				openedCard.setObject(cardId);
				target.add(preview);
				dialog.open(new EditCardPanel(ModalDialog.CONTENT_ID, board, cardId)
				{
					private static final long serialVersionUID = 1L;

					@Override
					protected void onSaved(AjaxRequestTarget target)
					{
						dialog.close(target);
						target.add(taskBoard, summary, preview);
					}

					@Override
					protected void onCancel(AjaxRequestTarget target)
					{
						dialog.close(target);
					}
				}, target);
			}
		});
	}

	@Override
	protected void onDetach()
	{
		board.detach();
		super.onDetach();
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);

		response.render(CssHeaderItem.forReference(
			new CssResourceReference(TaskBoardPage.class, "TaskBoardPage.css")));
	}
}

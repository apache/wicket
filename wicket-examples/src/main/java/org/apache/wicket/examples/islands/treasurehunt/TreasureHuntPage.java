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
package org.apache.wicket.examples.islands.treasurehunt;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.islands.IslandSettings;
import org.apache.wicket.islands.IslandTargets;
import org.apache.wicket.islands.modules.ViteManifestModuleLoader;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.EnumChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.CssResourceReference;

/**
 * The game page. The board is an island; the status line, the "new game" link and the size
 * selector are plain Wicket components that work together with it.
 */
public class TreasureHuntPage extends WicketExamplePage
{
	private static final long serialVersionUID = 1L;

	private final IModel<BoardSize> size = Model.of(BoardSize.NORMAL);

	private final IModel<Game> game = Model.of(Game.start(BoardSize.NORMAL));

	private final WebMarkupContainer gamePanel;

	private final Label status;

	private final Label probesLeft;

	private final ProbeHistory history;

	private final ScorePanel scorePanel;

	private final HighScoreList highScores;

	private TreasureBoard board;

	public TreasureHuntPage()
	{
		Form<Void> form = new Form<>("form");
		add(form);
		DropDownChoice<BoardSize> sizeChoice = new DropDownChoice<>("size", size,
			List.of(BoardSize.values()), new EnumChoiceRenderer<>(this));
		sizeChoice.add(new AjaxFormComponentUpdatingBehavior("change")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target)
			{
				changeSize(target);
			}
		});
		form.add(sizeChoice);

		gamePanel = new WebMarkupContainer("game");
		gamePanel.setOutputMarkupId(true);
		add(gamePanel);

		status = new Label("status", this::getStatusText);
		status.setOutputMarkupId(true);
		probesLeft = new Label("probesLeft", () -> game.getObject().getProbesLeft());
		probesLeft.setOutputMarkupId(true);
		history = new ProbeHistory("history", game);
		scorePanel = new ScorePanel("score", game)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onScoreSaved(AjaxRequestTarget target)
			{
				Game saved = game.getObject();
				PlayerEntry player = saved.getSavedBy();
				TreasureHuntApplication.get()
					.getHighScores()
					.add(saved.getSize(),
						new Score(player.name().strip(), player.colour(), saved.getScore()));
				IslandTargets.invalidate(target, "scores");
			}
		};
		board = new TreasureBoard("board", game, status, probesLeft, history, scorePanel);
		gamePanel.add(status, probesLeft, history, scorePanel, board);

		gamePanel.add(new AjaxLink<Void>("newGame")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				game.setObject(Game.start(size.getObject()));
				target.add(gamePanel);
			}
		});

		highScores = new HighScoreList("highScores", size);
		add(highScores);

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
	}

	/**
	 * A new board size needs a new board: the island is replaced, so the browser mounts a new
	 * React component instead of updating the old one.
	 */
	private void changeSize(AjaxRequestTarget target)
	{
		game.setObject(Game.start(size.getObject()));
		TreasureBoard newBoard = new TreasureBoard("board", game, status, probesLeft, history,
			scorePanel);
		newBoard.setMarkupId(board.getMarkupId());
		board.replaceWith(newBoard);
		board = newBoard;
		target.add(newBoard, status, probesLeft, history, scorePanel, highScores);
	}

	private String getStatusText()
	{
		Game current = game.getObject();
		return switch (current.getStatus())
		{
			case RUNNING -> current.getTreasuresLeft() + " treasures left to find.";
			case WON -> "You found all treasures! Score: " + current.getScore() + ".";
			case LOST -> "Out of probes. Score: " + current.getScore() + ".";
		};
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);

		response.render(CssHeaderItem.forReference(
			new CssResourceReference(TreasureHuntPage.class, "TreasureHuntPage.css")));
	}
}

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

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.examples.islands.taskboard.BoardRepository.MoveResult;
import org.apache.wicket.islands.Island;
import org.apache.wicket.model.IModel;

/**
 * The board, rendered by React. The browser moves a card at once and calls {@code move}; the
 * server decides. When it refuses, the island rolls the move back. A refusal because the board
 * changed in the meantime also sends the current board along. A double click on a card calls
 * {@code open}, which lets the page open its Wicket dialog.
 */
public abstract class TaskBoard extends Island<BoardProps>
{
	private static final long serialVersionUID = 1L;

	/**
	 * A move as the browser asks for it.
	 *
	 * @param cardId
	 *            the card
	 * @param column
	 *            the column to move it to
	 * @param index
	 *            the position in that column, counted without the card
	 * @param version
	 *            the version of the board the browser showed
	 */
	public record Move(String cardId, Column column, int index, long version)
	{
	}

	/**
	 * A request to open a card for editing.
	 *
	 * @param cardId
	 *            the card
	 */
	public record Open(String cardId)
	{
	}

	/**
	 * @param id
	 *            the component id
	 * @param board
	 *            the board
	 * @param summary
	 *            the component summarizing the board, updated with it
	 */
	public TaskBoard(String id, IModel<Board> board, Component summary)
	{
		super(id, "task-board", board.map(BoardProps::of));

		onCall("move", Move.class, (move, call) -> {
			if (move.cardId() == null || move.column() == null)
			{
				throw call.reject("The move names no card or column.");
			}
			MoveResult result = TaskBoardApplication.get()
				.getRepository()
				.move(board.getObject().id(), move.cardId(), move.column(), move.index(),
					move.version());
			board.detach();
			switch (result)
			{
				case MOVED -> {
					call.getTarget().add(this, summary);
					call.invalidate("throughput");
				}
				case LIMIT_REACHED -> throw call.reject("‘" + move.column().getTitle() +
					"’ allows at most " + move.column().getLimit() + " cards.");
				case STALE -> {
					call.getTarget().add(this, summary);
					throw call.reject("A teammate changed the board in the meantime. " +
						"It now shows their changes.");
				}
			}
		});

		onCall("open", Open.class, (open, call) -> {
			if (open.cardId() == null || board.getObject().findCard(open.cardId()).isEmpty())
			{
				call.getTarget().add(this);
				throw call.reject("The card is no longer on the board.");
			}
			onOpen(open.cardId(), call.getTarget());
		});
	}

	/**
	 * Called when the user opens a card on the board.
	 *
	 * @param cardId
	 *            the card, which is on the board
	 * @param target
	 *            the target of the call
	 */
	protected abstract void onOpen(String cardId, AjaxRequestTarget target);
}

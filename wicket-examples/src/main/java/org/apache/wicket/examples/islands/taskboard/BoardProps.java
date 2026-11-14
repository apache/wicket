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

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * The props of the board island: the cards per column, and the version of the board they show.
 * The description of a card is not part of them; the edit dialog shows it.
 *
 * @param version
 *            the version of the board
 * @param columns
 *            the columns, in order
 */
public record BoardProps(long version, List<ColumnProps> columns) implements Serializable
{
	/**
	 * @param id
	 *            the column
	 * @param title
	 *            its title
	 * @param limit
	 *            the most cards it may hold, 0 for no limit
	 * @param cards
	 *            its cards, in order
	 */
	public record ColumnProps(Column id, String title, int limit, List<CardProps> cards)
		implements Serializable
	{
	}

	/**
	 * @param id
	 *            the card's id
	 * @param title
	 *            its title
	 * @param due
	 *            its due date as an ISO date, {@code null} for none
	 * @param assigneeId
	 *            the id of its assignee, {@code null} for nobody
	 * @param assignee
	 *            the name of its assignee, {@code null} for nobody
	 * @param labels
	 *            its labels
	 */
	public record CardProps(String id, String title, String due, String assigneeId,
		String assignee, List<String> labels) implements Serializable
	{
	}

	static BoardProps of(Board board)
	{
		return new BoardProps(board.version(), Arrays.stream(Column.values())
			.map(column -> new ColumnProps(column, column.getTitle(), column.getLimit(),
				board.columns().get(column).stream().map(card -> cardProps(board, card)).toList()))
			.toList());
	}

	private static CardProps cardProps(Board board, Card card)
	{
		return new CardProps(card.id(), card.title(),
			card.due() == null ? null : card.due().toString(), card.assigneeId(),
			card.assigneeId() == null ? null
				: board.findMember(card.assigneeId()).map(Member::name).orElse(null),
			card.labels());
	}
}

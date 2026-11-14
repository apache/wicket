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

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Keeps the boards. It is shared by every request of the application, so each board is changed
 * under its own lock and read as an immutable {@link Board} snapshot.
 */
public class BoardRepository
{
	/**
	 * What became of a move.
	 */
	public enum MoveResult
	{
		MOVED,

		/** The move was made on an older version of the board. */
		STALE,

		/** The target column is full. */
		LIMIT_REACHED
	}

	private static final class StoredBoard
	{
		private final String id;

		private final List<Member> members;

		private final Map<Column, List<Card>> columns = new EnumMap<>(Column.class);

		/** Cards completed before the board was created, per week, the latest week first. */
		private final int[] archivedThroughput;

		private long version = 1;

		private StoredBoard(String id, List<Member> members, int[] archivedThroughput)
		{
			this.id = id;
			this.members = List.copyOf(members);
			this.archivedThroughput = archivedThroughput;
			for (Column column : Column.values())
			{
				columns.put(column, new ArrayList<>());
			}
		}

		private synchronized Board snapshot()
		{
			Map<Column, List<Card>> copy = new EnumMap<>(Column.class);
			columns.forEach((column, cards) -> copy.put(column, List.copyOf(cards)));
			return new Board(id, version, members, Map.copyOf(copy));
		}

		private Column columnOf(String cardId)
		{
			for (Map.Entry<Column, List<Card>> entry : columns.entrySet())
			{
				if (entry.getValue().stream().anyMatch(card -> card.id().equals(cardId)))
				{
					return entry.getKey();
				}
			}
			return null;
		}

		private Card remove(String cardId)
		{
			List<Card> cards = columns.get(columnOf(cardId));
			for (int i = 0; i < cards.size(); i++)
			{
				if (cards.get(i).id().equals(cardId))
				{
					return cards.remove(i);
				}
			}
			throw new IllegalStateException("No card " + cardId);
		}

		private void insert(Column column, int index, Card card)
		{
			List<Card> cards = columns.get(column);
			Card placed = column == Column.DONE
				? card.completedOn() == null ? card.completed(LocalDate.now()) : card
				: card.completed(null);
			cards.add(Math.max(0, Math.min(index, cards.size())), placed);
			version++;
		}
	}

	private final Map<String, StoredBoard> boards = new ConcurrentHashMap<>();

	/**
	 * Creates a board with members and cards to play with.
	 *
	 * @return the id of the new board
	 */
	public String createDemoBoard()
	{
		String id = UUID.randomUUID().toString();
		StoredBoard board = new StoredBoard(id,
			List.of(new Member("alice", "Alice"), new Member("bob", "Bob"),
				new Member("carol", "Carol"), new Member("dave", "Dave")),
			new int[] { 3, 5, 2, 4, 6, 3, 4, 2 });
		LocalDate today = LocalDate.now();
		Supplier<String> ids = new Supplier<>()
		{
			private int next = 1;

			@Override
			public String get()
			{
				return "c" + next++;
			}
		};
		add(board, Column.TODO, new Card(ids.get(), "Write the release notes",
			"<p>Collect the changes since the last release.</p>", today.plusDays(9), "alice",
			List.of("docs"), null));
		add(board, Column.TODO, new Card(ids.get(), "Upgrade the build plugins",
			"<p>Check the <b>enforcer</b> rules after the upgrade.</p>", null, null,
			List.of("build"), null));
		add(board, Column.TODO, new Card(ids.get(), "Answer the mailing list questions", "", null,
			"bob", List.of(), null));
		add(board, Column.TODO, new Card(ids.get(), "Fix the flaky upload test",
			"<p>Fails about once in twenty runs.</p>", today.plusDays(3), "carol",
			List.of("bug"), null));
		add(board, Column.IN_PROGRESS, new Card(ids.get(), "Islands: task board example",
			"<p>Drag cards between the columns.</p>", today.plusDays(5), "dave",
			List.of("feature"), null));
		add(board, Column.IN_PROGRESS, new Card(ids.get(), "Translate the new messages", "",
			today.plusDays(14), "alice", List.of("i18n"), null));
		add(board, Column.REVIEW, new Card(ids.get(), "Content security policy for styles",
			"<ul><li>nonce for inline styles</li><li>tests</li></ul>", today.plusDays(1), "bob",
			List.of("security"), null));
		add(board, Column.DONE, new Card(ids.get(), "Release 12.0.0-M1", "", null, "carol",
			List.of(), today.minusDays(2)));
		add(board, Column.DONE, new Card(ids.get(), "Update the user guide", "", null, "alice",
			List.of("docs"), today.minusDays(9)));
		boards.put(id, board);
		return id;
	}

	private static void add(StoredBoard board, Column column, Card card)
	{
		board.columns.get(column).add(card);
	}

	/**
	 * Forgets a board.
	 *
	 * @param id
	 *            the id of the board
	 */
	public void remove(String id)
	{
		boards.remove(id);
	}

	/**
	 * @param id
	 *            the id of the board
	 * @return the current state of the board
	 */
	public Board get(String id)
	{
		return board(id).snapshot();
	}

	/**
	 * Moves a card, if the board has not changed since the given version.
	 *
	 * @param boardId
	 *            the board
	 * @param cardId
	 *            the card
	 * @param to
	 *            the column to move it to
	 * @param index
	 *            the position in that column, counted without the card itself
	 * @param version
	 *            the version of the board the move was made on
	 * @return what became of the move
	 */
	public MoveResult move(String boardId, String cardId, Column to, int index, long version)
	{
		StoredBoard board = board(boardId);
		synchronized (board)
		{
			Column from = board.columnOf(cardId);
			if (version != board.version || from == null)
			{
				return MoveResult.STALE;
			}
			if (from != to && to.getLimit() > 0 && board.columns.get(to).size() >= to.getLimit())
			{
				return MoveResult.LIMIT_REACHED;
			}
			board.insert(to, index, board.remove(cardId));
			return MoveResult.MOVED;
		}
	}

	/**
	 * Moves the card at the top of "To do" to "Review", as a teammate working on the same board
	 * would.
	 *
	 * @param boardId
	 *            the board
	 * @return the title of the moved card, {@code null} when "To do" is empty
	 */
	public String moveForTeammate(String boardId)
	{
		StoredBoard board = board(boardId);
		synchronized (board)
		{
			List<Card> todo = board.columns.get(Column.TODO);
			if (todo.isEmpty())
			{
				return null;
			}
			Card card = todo.remove(0);
			board.insert(Column.REVIEW, 0, card);
			return card.title();
		}
	}

	/**
	 * Changes a card.
	 *
	 * @param boardId
	 *            the board
	 * @param cardId
	 *            the card
	 * @param edit
	 *            the new values
	 * @return whether the card was still on the board
	 */
	public boolean update(String boardId, String cardId, CardEdit edit)
	{
		StoredBoard board = board(boardId);
		synchronized (board)
		{
			Column column = board.columnOf(cardId);
			if (column == null)
			{
				return false;
			}
			List<Card> cards = board.columns.get(column);
			for (int i = 0; i < cards.size(); i++)
			{
				if (cards.get(i).id().equals(cardId))
				{
					cards.set(i, cards.get(i).edited(edit));
				}
			}
			board.version++;
			return true;
		}
	}

	/**
	 * Counts the cards completed per week.
	 *
	 * @param boardId
	 *            the board
	 * @param weeks
	 *            the number of weeks, ending with the current one
	 * @return the counts, the oldest week first
	 */
	public int[] throughput(String boardId, int weeks)
	{
		StoredBoard board = board(boardId);
		int[] counts = new int[weeks];
		LocalDate today = LocalDate.now();
		synchronized (board)
		{
			for (Card card : board.columns.get(Column.DONE))
			{
				long weeksAgo = ChronoUnit.WEEKS.between(card.completedOn(), today);
				if (weeksAgo < weeks)
				{
					counts[weeks - 1 - (int)weeksAgo]++;
				}
			}
			for (int weeksAgo = 0; weeksAgo < weeks &&
				weeksAgo < board.archivedThroughput.length; weeksAgo++)
			{
				counts[weeks - 1 - weeksAgo] += board.archivedThroughput[weeksAgo];
			}
		}
		return counts;
	}

	private StoredBoard board(String id)
	{
		StoredBoard board = boards.get(id);
		if (board == null)
		{
			throw new IllegalArgumentException("No board " + id);
		}
		return board;
	}
}

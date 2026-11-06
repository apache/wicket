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

/**
 * What the task board islands share, as TaskBoard.java's BoardProps sends it.
 */

export type ColumnId = 'TODO' | 'IN_PROGRESS' | 'REVIEW' | 'DONE';

export interface CardProps {
	id: string;
	title: string;
	due: string | null;
	assigneeId: string | null;
	assignee: string | null;
	labels: string[];
}

export interface ColumnProps {
	id: ColumnId;
	title: string;
	limit: number;
	cards: CardProps[];
}

export interface BoardProps {
	version: number;
	columns: ColumnProps[];
}

/**
 * A move as the board asks the server for it: the position counts the target column's cards
 * without the moved card.
 */
export interface Move {
	cardId: string;
	column: ColumnId;
	index: number;
	version: number;
}

/**
 * The columns after a move, for showing it before the server has answered.
 */
export function applyMove(columns: ColumnProps[], move: Move): ColumnProps[] {
	const card = columns.flatMap(column => column.cards).find(c => c.id === move.cardId);
	if (!card) {
		return columns;
	}
	return columns.map(column => {
		const cards = column.cards.filter(c => c.id !== move.cardId);
		if (column.id === move.column) {
			cards.splice(Math.min(move.index, cards.length), 0, card);
		}
		return { ...column, cards };
	});
}

/**
 * The move a drop asks for, or null when the card would stay where it is. The drop target is a
 * column ("column:ID", the card goes last) or a card ("card:ID", the card goes before it).
 */
export function moveFor(columns: ColumnProps[], cardId: string, target: string,
	version: number): Move | null {
	const [kind, id] = target.split(':');
	const column = kind === 'column'
		? columns.find(c => c.id === id)
		: columns.find(c => c.cards.some(card => card.id === id));
	const from = columns.find(c => c.cards.some(card => card.id === cardId));
	if (!column || !from || (kind === 'card' && id === cardId)) {
		return null;
	}
	const others = column.cards.filter(card => card.id !== cardId);
	const index = kind === 'column' ? others.length : others.findIndex(card => card.id === id);
	const current = from.cards.findIndex(card => card.id === cardId);
	if (from.id === column.id && index === current) {
		return null;
	}
	return { cardId, column: column.id, index, version };
}

/**
 * The client store holding the board filter, written by the filter island and read by the board.
 */
export const FILTER_STORE = 'board-filter';

export interface BoardFilter {
	assigneeId: string | null;
	text: string;
}

/**
 * Whether a card passes the filter.
 */
export function matches(card: CardProps, filter: BoardFilter | undefined): boolean {
	if (!filter) {
		return true;
	}
	if (filter.assigneeId && card.assigneeId !== filter.assigneeId) {
		return false;
	}
	const text = filter.text.trim().toLowerCase();
	return text === '' || card.title.toLowerCase().includes(text) ||
		card.labels.some(label => label.toLowerCase().includes(text));
}

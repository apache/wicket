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

import {
	DndContext, KeyboardSensor, PointerSensor, pointerWithin, rectIntersection, useDraggable,
	useDroppable, useSensor, useSensors, type CollisionDetection, type DragEndEvent
} from '@dnd-kit/core';
import { CSS } from '@dnd-kit/utilities';
import { Alert, Avatar, Badge, Button, Card, Group, Text, Title } from '@mantine/core';
import { startTransition, useOptimistic, useState } from 'react';
import { IslandCallError, useIsland, useStore } from 'wicket-islands-react';
import BoardTheme from './BoardTheme';
import {
	applyMove, FILTER_STORE, matches, moveFor, type BoardFilter, type BoardProps, type CardProps,
	type ColumnProps
} from './shared';
import './TaskBoard.css';

/**
 * The board. A dropped card moves at once (React's useOptimistic over the props); the server's
 * answer decides: accepted, the new props show the move; refused, the optimistic state is dropped
 * and the card is back where the props have it.
 */
export default function TaskBoard({ version, columns }: BoardProps) {
	const { call } = useIsland<BoardProps>();
	const [filter] = useStore<BoardFilter>(FILTER_STORE);
	const [shown, showMove] = useOptimistic(columns, applyMove);
	const [notice, setNotice] = useState<string | null>(null);
	const sensors = useSensors(
		useSensor(PointerSensor, { activationConstraint: { distance: 5 } }),
		useSensor(KeyboardSensor));

	function onDragEnd({ active, over }: DragEndEvent) {
		const move = over && moveFor(shown, String(active.id), String(over.id), version);
		if (!move) {
			return;
		}
		setNotice(null);
		startTransition(async () => {
			showMove(move);
			try {
				await call('move', move);
			} catch (e) {
				setNotice(e instanceof IslandCallError ? e.messages.join(' ') : String(e));
			}
		});
	}

	function open(card: CardProps) {
		call('open', { cardId: card.id }).catch((e: unknown) =>
			setNotice(e instanceof IslandCallError ? e.messages.join(' ') : String(e)));
	}

	return (
		<BoardTheme>
			<div className="task-board">
				{notice && (
					<Alert color="red" withCloseButton onClose={() => setNotice(null)} role="alert"
						className="notice">
						{notice}
					</Alert>
				)}
				<DndContext sensors={sensors} collisionDetection={underThePointer} onDragEnd={onDragEnd}>
					<div className="columns">
						{shown.map(column => (
							<BoardColumn key={column.id} column={column}
								cards={column.cards.filter(card => matches(card, filter))} onOpen={open} />
						))}
					</div>
				</DndContext>
			</div>
		</BoardTheme>
	);
}

function BoardColumn({ column, cards, onOpen }: {
	column: ColumnProps;
	cards: CardProps[];
	onOpen(card: CardProps): void;
}) {
	const { setNodeRef, isOver } = useDroppable({ id: `column:${column.id}` });
	const full = column.limit > 0 && column.cards.length >= column.limit;
	return (
		<section ref={setNodeRef} className={isOver ? 'column over' : 'column'}
			data-column={column.id}>
			<Group justify="space-between" mb="xs">
				<Title order={4}>{column.title}</Title>
				{column.limit > 0 && (
					<Badge color={full ? 'red' : 'gray'} variant="light">
						{column.cards.length} / {column.limit}
					</Badge>
				)}
			</Group>
			{cards.map(card => <BoardCard key={card.id} card={card} onOpen={onOpen} />)}
		</section>
	);
}

function BoardCard({ card, onOpen }: { card: CardProps; onOpen(card: CardProps): void }) {
	const drag = useDraggable({ id: card.id });
	const drop = useDroppable({ id: `card:${card.id}` });
	return (
		<Card ref={node => { drag.setNodeRef(node); drop.setNodeRef(node); }}
			{...drag.listeners} {...drag.attributes}
			className={drag.isDragging ? 'card dragging' : 'card'}
			style={{ transform: CSS.Translate.toString(drag.transform) }}
			withBorder padding="sm" mb="xs" data-card-id={card.id}
			onDoubleClick={() => onOpen(card)}>
			<Group gap="xs" justify="space-between" wrap="nowrap" align="flex-start">
				<Text fw={500} className="title">{card.title}</Text>
				<Button className="edit" size="compact-xs" variant="subtle"
					aria-label={`Edit ${card.title}`} onPointerDown={event => event.stopPropagation()}
					onKeyDown={event => event.stopPropagation()}
					onClick={() => onOpen(card)}>Edit</Button>
			</Group>
			<Group gap="xs" mt={6} justify="space-between">
				<Group gap={4}>
					{card.labels.map(label => <Badge key={label} size="xs" variant="outline">{label}</Badge>)}
					{card.due && <Text size="xs" c="dimmed">due {card.due}</Text>}
				</Group>
				{card.assignee && (
					<Avatar size="sm" radius="xl" title={card.assignee}>{card.assignee.slice(0, 2)}</Avatar>
				)}
			</Group>
		</Card>
	);
}

/**
 * Drops where the pointer is; the keyboard, which has no pointer, falls back to the overlap.
 */
const underThePointer: CollisionDetection = args => {
	const collisions = pointerWithin(args);
	return collisions.length > 0 ? collisions : rectIntersection(args);
};

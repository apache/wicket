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

import { IslandCallError } from 'wicket-islands';

/**
 * What the React and the Svelte poll share: the props, the client store and the logic that
 * does not depend on the framework.
 */

export interface PollOption {
	id: number;
	label: string;
	votes: number;
}

/**
 * The props of the poll, as PollIsland.java's PollProps sends them.
 */
export interface PollProps {
	question: string;
	options: PollOption[];
	total: number;
}

/**
 * The client store holding the id of the option highlighted in both polls.
 */
export const HIGHLIGHT_STORE = 'poll-highlight';

/**
 * The longest option label the server accepts.
 */
export const MAX_LABEL_LENGTH = 30;

export function percent(votes: number, total: number): number {
	return total === 0 ? 0 : Math.round(votes * 100 / total);
}

/**
 * The option with the most votes, null while none has a vote or two share the lead.
 */
export function leaderOf(options: PollOption[]): number | null {
	const ranked = [...options].sort((a, b) => b.votes - a.votes);
	if (ranked.length === 0 || ranked[0].votes === 0
		|| (ranked.length > 1 && ranked[1].votes === ranked[0].votes)) {
		return null;
	}
	return ranked[0].id;
}

export function describe(error: unknown): string {
	return error instanceof IslandCallError ? error.messages.join(' ') : String(error);
}

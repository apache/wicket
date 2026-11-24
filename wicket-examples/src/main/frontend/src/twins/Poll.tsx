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

import { useState, type FormEvent } from 'react';
import { useIsland, useStore } from 'wicket-islands-react';
import { describe, HIGHLIGHT_STORE, leaderOf, MAX_LABEL_LENGTH, percent,
	type PollProps } from './shared';
import './Poll.css';

/**
 * The poll in React. Poll.svelte is the same component in Svelte; both render the same markup.
 */
export default function Poll({ question, options, total }: PollProps) {
	const { call } = useIsland<PollProps>();
	const [highlight, setHighlight] = useStore<number | null>(HIGHLIGHT_STORE);
	const [draft, setDraft] = useState('');
	const [cast, setCast] = useState(0);
	const [busy, setBusy] = useState(false);
	const [message, setMessage] = useState<string | null>(null);
	const leader = leaderOf(options);

	async function send(name: string, payload: unknown): Promise<boolean> {
		setBusy(true);
		setMessage(null);
		try {
			await call(name, payload);
			return true;
		} catch (e) {
			setMessage(describe(e));
			return false;
		} finally {
			setBusy(false);
		}
	}

	async function vote(id: number) {
		if (await send('vote', { id })) {
			setCast(count => count + 1);
		}
	}

	async function add(event: FormEvent) {
		event.preventDefault();
		if (await send('add', { label: draft })) {
			setDraft('');
		}
	}

	return (
		<div className="poll" data-framework="react">
			<p className="poll-framework">Rendered by React</p>
			<h4 className="poll-question">{question}</h4>
			<ul className="poll-options">
				{options.map(option => (
					<li key={option.id} data-id={option.id}
						className={'poll-option' + (option.id === leader ? ' leader' : '')
							+ (option.id === highlight ? ' highlight' : '')}
						onMouseEnter={() => setHighlight(option.id)}
						onMouseLeave={() => setHighlight(null)}>
						<button type="button" className="poll-vote" disabled={busy}
							aria-label={`Vote for ${option.label}`}
							onClick={() => void vote(option.id)}>+1</button>
						<span className="poll-label">{option.label}</span>
						<span className="poll-bar">
							<span className="poll-fill"
								style={{ width: `${percent(option.votes, total)}%` }}></span>
						</span>
						<span className="poll-count">{option.votes}</span>
					</li>
				))}
			</ul>
			<form className="poll-add" onSubmit={event => void add(event)}>
				<input type="text" value={draft} maxLength={MAX_LABEL_LENGTH}
					placeholder="Another option" aria-label="Another option"
					onChange={event => setDraft(event.target.value)} />
				<button type="submit" disabled={busy}>Add</button>
			</form>
			{message && <p className="poll-message" role="alert">{message}</p>}
			<p className="poll-footer">Votes in total: {total}. Cast here: {cast}.</p>
		</div>
	);
}

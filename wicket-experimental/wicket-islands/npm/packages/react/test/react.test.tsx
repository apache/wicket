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

import { QueryClient } from '@tanstack/react-query';
import { act, useState } from 'react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { IslandRuntime, type IslandContext, type IslandInstance } from 'wicket-islands';
import { defineIsland, useFormState, useFormValue, useIsland, useIslandQuery,
	useStore } from '../src/index';

(globalThis as { IS_REACT_ACT_ENVIRONMENT?: boolean }).IS_REACT_ACT_ENVIRONMENT = true;

interface Props {
	title: string;
}

function Counter({ title }: Props) {
	const [count, setCount] = useState(0);
	const { call } = useIsland<Props>();
	return (
		<div>
			<h1>{title}</h1>
			<button id="increment" onClick={() => setCount(count + 1)}>{count}</button>
			<button id="call" onClick={() => void call('save', { count })}>save</button>
		</div>
	);
}

function Hover({ title }: Props) {
	const [hover, setHover] = useStore<string>('hover');
	return <button id={`hover-${title}`} onClick={() => setHover(title)}>{hover ?? 'none'}</button>;
}

function Scores({ title }: Props) {
	const scores = useIslandQuery<string[]>('scores', { size: title });
	return <ol id="scores">{scores.data?.map(score => <li key={score}>{score}</li>)}</ol>;
}

function SharedScores() {
	const scores = useIslandQuery<string[]>('scores');
	return <ol>{scores.data?.map(score => <li key={score}>{score}</li>)}</ol>;
}

function Name() {
	const [name, setName] = useFormValue<string>();
	const { errors } = useFormState();
	return (
		<div>
			<input id="name" value={name ?? ''} onChange={event => setName(event.target.value)} />
			<span id="errors">{errors.join(', ')}</span>
		</div>
	);
}

describe('defineIsland', () => {
	let root: HTMLElement;
	let context: IslandContext<Props>;
	let instance: IslandInstance<Props>;
	let stores: IslandRuntime['stores'];
	let queryClient: QueryClient;

	function contextFor(props: Props): IslandContext<Props> {
		return {
			props,
			key: props.title,
			signal: new AbortController().signal,
			store: name => stores.get(name),
			queryClient,
			queryKey: (name, payload) => payload === undefined
				? [name, props.title] : [name, props.title, payload],
			call: vi.fn(() => Promise.resolve(undefined)) as IslandContext<Props>['call']
		};
	}

	beforeEach(() => {
		root = document.createElement('div');
		document.body.appendChild(root);
		stores = new IslandRuntime().stores;
		queryClient = new QueryClient();
		context = contextFor({ title: 'first' });
		act(() => {
			instance = defineIsland(Counter).mount(root, context);
		});
	});

	afterEach(() => {
		root.remove();
	});

	function click(id: string) {
		act(() => {
			root.querySelector<HTMLButtonElement>(`#${id}`)!.click();
		});
	}

	it('renders the component with the island props', () => {
		expect(root.querySelector('h1')?.textContent).toBe('first');
	});

	it('re-renders with new props, keeping the component state', () => {
		click('increment');
		click('increment');

		act(() => instance.update({ title: 'second' }));

		expect(root.querySelector('h1')?.textContent).toBe('second');
		expect(root.querySelector('#increment')?.textContent).toBe('2');
	});

	it('calls the server through useIsland', () => {
		click('increment');
		click('call');

		expect(context.call).toHaveBeenCalledWith('save', { count: 1 });
	});

	it('shares a store between islands', () => {
		const [rootA, rootB] = [document.createElement('div'), document.createElement('div')];
		document.body.append(rootA, rootB);
		act(() => {
			defineIsland(Hover).mount(rootA, contextFor({ title: 'a' }));
			defineIsland(Hover).mount(rootB, contextFor({ title: 'b' }));
		});

		act(() => {
			rootA.querySelector<HTMLButtonElement>('#hover-a')!.click();
		});

		expect(rootB.querySelector('#hover-b')?.textContent).toBe('a');
		rootA.remove();
		rootB.remove();
	});

	it('edits the value of a form island', () => {
		let value: unknown = 'Ann';
		let state = { required: true, enabled: true, label: 'Name', errors: ['Too short'] };
		const listeners = new Set<() => void>();
		const formRoot = document.createElement('div');
		document.body.appendChild(formRoot);
		act(() => {
			defineIsland(Name).mount(formRoot, {
				...contextFor({ title: 'f' }),
				form: {
					getValue: () => value,
					setValue: next => {
						value = next;
						state = { ...state, errors: [] };
						listeners.forEach(listener => listener());
					},
					getState: () => state,
					subscribe: listener => {
						listeners.add(listener);
						return () => listeners.delete(listener);
					}
				}
			});
		});
		expect(formRoot.querySelector('#errors')?.textContent).toBe('Too short');

		act(() => {
			const input = formRoot.querySelector<HTMLInputElement>('#name')!;
			const setter = Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'value')!.set!;
			setter.call(input, 'Annabel');
			input.dispatchEvent(new Event('input', { bubbles: true }));
		});

		expect(value).toBe('Annabel');
		expect(formRoot.querySelector<HTMLInputElement>('#name')!.value).toBe('Annabel');
		expect(formRoot.querySelector('#errors')?.textContent).toBe('');
		formRoot.remove();
	});

	it('queries with island calls and refetches when the key is invalidated', async () => {
		let round = 0;
		const call = vi.fn(() => Promise.resolve([`score ${++round}`]));
		const scoresRoot = document.createElement('div');
		document.body.appendChild(scoresRoot);
		await act(async () => {
			defineIsland(Scores).mount(scoresRoot, {
				...contextFor({ title: 'SMALL' }),
				call: call as IslandContext<Props>['call']
			});
		});
		expect(call).toHaveBeenCalledWith('scores', { size: 'SMALL' });
		await act(() => vi.waitFor(() =>
			expect(scoresRoot.querySelector('#scores')?.textContent).toBe('score 1')));

		await act(async () => {
			await queryClient.invalidateQueries({ queryKey: ['scores'] });
		});

		await act(() => vi.waitFor(() =>
			expect(scoresRoot.querySelector('#scores')?.textContent).toBe('score 2')));
		scoresRoot.remove();
	});

	it('keeps the query results of islands apart', async () => {
		const rootA = document.createElement('div');
		const rootB = document.createElement('div');
		document.body.append(rootA, rootB);
		const answering = (answer: string) =>
			vi.fn(() => Promise.resolve([answer])) as IslandContext<Props>['call'];
		await act(async () => {
			defineIsland(SharedScores).mount(rootA, { ...contextFor({ title: 'a' }), call: answering('from a') });
			defineIsland(SharedScores).mount(rootB, { ...contextFor({ title: 'b' }), call: answering('from b') });
		});

		await act(() => vi.waitFor(() => {
			expect(rootA.textContent).toBe('from a');
			expect(rootB.textContent).toBe('from b');
		}));
		rootA.remove();
		rootB.remove();
	});

	it('unmounts the React root', () => {
		act(() => instance.unmount());

		expect(root.innerHTML).toBe('');
	});
});

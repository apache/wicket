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

import { act, createElement } from 'react';
import { flushSync } from 'svelte';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { IslandRuntime } from 'wicket-islands';
import { defineIsland as defineReactIsland, useStore } from 'wicket-islands-react';
import { defineIsland } from 'wicket-islands-svelte';
import { component, data, formHost, host, requests, respond,
	setUpWicket } from '../../runtime/test/wicket-fixture';
import Counter from './Counter.svelte';
import Hover from './Hover.svelte';
import Name from './Name.svelte';
import Scores from './Scores.svelte';

(globalThis as { IS_REACT_ACT_ENVIRONMENT?: boolean }).IS_REACT_ACT_ENVIRONMENT = true;

/**
 * Lets the answers of calls and the store updates they cause reach the components.
 */
async function settle(): Promise<void> {
	await new Promise(resolve => setTimeout(resolve, 0));
	flushSync();
}

function click(selector: string): void {
	document.querySelector<HTMLElement>(selector)!.click();
	flushSync();
}

function text(selector: string): string | null | undefined {
	return document.querySelector(selector)?.textContent;
}

describe('Svelte islands', () => {
	let runtime: IslandRuntime;

	beforeEach(() => {
		setUpWicket();
		runtime = new IslandRuntime();
	});

	afterEach(() => {
		document.body.innerHTML = '';
	});

	it('mounts the component with the island props', () => {
		document.body.innerHTML = host('counter1', 'counter', 'k1', { label: 'one' });

		runtime.register('counter', defineIsland(Counter));

		expect(text('#counter1 h1')).toBe('one');
	});

	it('keeps the component state when Wicket re-renders the island with new props', () => {
		document.body.innerHTML = `<div id="panel">${host('counter1', 'counter', 'k1',
			{ label: 'one' })}</div>`;
		runtime.register('counter', defineIsland(Counter));
		click('#increment');
		click('#increment');
		click('#call');

		respond(requests[0], component('panel',
			`<div id="panel">${host('counter1', 'counter', 'k1', { label: 'two' })}</div>`));
		flushSync();

		expect(text('#counter1 h1')).toBe('two');
		expect(text('#increment')).toBe('2');
	});

	it('calls the island on the server as a Wicket Ajax request', () => {
		document.body.innerHTML = host('counter1', 'counter', 'k1', { label: 'one' });
		runtime.register('counter', defineIsland(Counter));
		click('#increment');

		click('#call');

		expect(requests).toHaveLength(1);
		expect(requests[0].attributes.ep).toMatchObject({
			'island-call': 'probe',
			'island-key': 'k1',
			'island-payload': '{"count":1}'
		});
	});

	it('destroys the component when Wicket removes the island', () => {
		document.body.innerHTML = `<div id="panel">${host('counter1', 'counter', 'k1',
			{ label: 'one' })}</div>`;
		runtime.register('counter', defineIsland(Counter));
		const root = document.querySelector('#counter1 [data-wicket-island-root]')!;
		click('#call');

		respond(requests[0], component('panel', '<div id="panel"></div>'));
		flushSync();

		expect(root.childElementCount).toBe(0);
	});

	it('shares a client store with the React islands of the page', () => {
		document.body.innerHTML = host('svelte1', 'svelte-hover', 'k1', { label: 'svelte' }) +
			host('react1', 'react-hover', 'k2', { label: 'react' });
		runtime.register('svelte-hover', defineIsland(Hover));
		act(() => {
			runtime.register('react-hover', defineReactIsland(function ReactHover({ label }:
				{ label: string }) {
				const [hover, setHover] = useStore<string>('hover');
				return createElement('button', { id: `hover-${label}`, onClick: () => setHover(label) },
					hover ?? 'none');
			}));
		});

		act(() => click('#hover-svelte'));
		expect(text('#hover-react')).toBe('svelte');

		act(() => click('#hover-react'));
		expect(text('#hover-svelte')).toBe('react');
	});

	it('fetches a query, and again when the server marks its name as stale', async () => {
		document.body.innerHTML = host('scores1', 'scores', 'k1', { label: 'scores' }) +
			host('counter1', 'counter', 'k2', { label: 'one' });
		runtime.register('scores', defineIsland(Scores));
		runtime.register('counter', defineIsland(Counter));
		expect(document.querySelector('#scores')?.getAttribute('data-loading')).toBe('true');

		respond(requests[0], data({ call: '1', ok: true, value: ['a'], invalidate: [] }));
		await settle();
		expect(text('#scores')?.trim()).toBe('a');

		click('#call');
		respond(requests[1], data({ call: '2', ok: true, value: null, invalidate: ['probe'] }));
		await settle();
		expect(requests).toHaveLength(3);
		expect(document.querySelector('#scores')?.getAttribute('data-loading')).toBe('true');
		expect(text('#scores')?.trim()).toBe('a');

		respond(requests[2], data({ call: '3', ok: true, value: ['a', 'b'], invalidate: [] }));
		await settle();
		expect([...document.querySelectorAll('#scores li')].map(li => li.textContent))
			.toEqual(['a', 'b']);
	});

	it('reports a failed query with the kind of its error', async () => {
		document.body.innerHTML = host('scores1', 'scores', 'k1', { label: 'scores' });
		runtime.register('scores', defineIsland(Scores));

		respond(requests[0], data({ call: '1', ok: false,
			error: { kind: 'forbidden', messages: ['no'] }, invalidate: [] }));
		await settle();

		expect(text('#error')).toBe('forbidden');
	});

	it('binds the value of a form island and shows its feedback', () => {
		document.body.innerHTML = formHost('name1', 'name', 'k1', 'Ann',
			{ required: true, enabled: true, label: 'Name', errors: ['Too short'] });
		runtime.register('name', defineIsland(Name));
		const input = document.querySelector<HTMLInputElement>('#name')!;
		expect(input.value).toBe('Ann');
		expect(text('#errors')).toBe('Too short');

		input.value = 'Anna';
		input.dispatchEvent(new Event('input', { bubbles: true }));
		flushSync();

		expect(document.querySelector<HTMLInputElement>('input[data-wicket-island-value]')?.value)
			.toBe('"Anna"');
	});
});

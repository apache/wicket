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

import { act, useState } from 'react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import type { IslandContext, IslandInstance } from 'wicket-islands';
import { defineIsland, useIsland } from '../src/index';

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

describe('defineIsland', () => {
	let root: HTMLElement;
	let context: IslandContext<Props>;
	let instance: IslandInstance<Props>;

	beforeEach(() => {
		root = document.createElement('div');
		document.body.appendChild(root);
		context = {
			props: { title: 'first' },
			key: 'k1',
			signal: new AbortController().signal,
			call: vi.fn(() => Promise.resolve(undefined)) as IslandContext<Props>['call']
		};
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

	it('unmounts the React root', () => {
		act(() => instance.unmount());

		expect(root.innerHTML).toBe('');
	});
});

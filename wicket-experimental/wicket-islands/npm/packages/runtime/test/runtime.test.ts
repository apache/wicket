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

import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { IslandCallError, IslandRuntime, type IslandContext, type IslandModule } from '../src/index';
import { component, data, failRequest, formHost, host, requests, respond, setUpWicket } from './wicket-fixture';

interface Props {
	label: string;
}

/**
 * Fires a call whose outcome the test does not care about.
 */
function ignore(call: Promise<unknown>): void {
	call.catch(() => undefined);
}

/**
 * An island module that records what the runtime does with it.
 */
class Recorder {
	mounts: { root: HTMLElement; context: IslandContext<Props> }[] = [];
	updates: Props[] = [];
	unmounts = 0;

	module(): IslandModule<Props> {
		return {
			mount: (root, context) => {
				root.textContent = context.props.label;
				this.mounts.push({ root, context });
				return {
					update: props => {
						root.textContent = props.label;
						this.updates.push(props);
					},
					unmount: () => {
						this.unmounts++;
					}
				};
			}
		};
	}

	get context(): IslandContext<Props> {
		return this.mounts[this.mounts.length - 1].context;
	}
}

describe('IslandRuntime', () => {
	let runtime: IslandRuntime;
	let recorder: Recorder;

	beforeEach(() => {
		setUpWicket();
		runtime = new IslandRuntime();
		recorder = new Recorder();
		document.body.innerHTML = `<div id="panel">${host('board1', 'board', 'k1', { label: 'one' })}</div>` +
			'<span id="label">before</span>';
	});

	it('mounts the islands on the page when their module registers', () => {
		runtime.register('board', recorder.module());

		expect(recorder.mounts).toHaveLength(1);
		const { root, context } = recorder.mounts[0];
		expect(root.parentElement?.id).toBe('board1');
		expect(root.hasAttribute('data-wicket-island-root')).toBe(true);
		expect(context.props).toEqual({ label: 'one' });
		expect(context.key).toBe('k1');
	});

	it('ignores islands of other types', () => {
		runtime.register('chart', recorder.module());

		expect(recorder.mounts).toHaveLength(0);
	});

	it('sends a call as a Wicket Ajax request', () => {
		runtime.register('board', recorder.module());

		ignore(recorder.context.call('probe', { x: 1, y: 2 }));

		expect(requests).toHaveLength(1);
		const { attributes } = requests[0];
		expect(attributes.u).toBe('./call?board1');
		expect(attributes.m).toBe('POST');
		expect(attributes.ep).toEqual({
			'island-call': 'probe',
			'island-call-id': '1',
			'island-key': 'k1',
			'island-payload': '{"x":1,"y":2}'
		});
	});

	it('omits the payload of a call without one', () => {
		runtime.register('board', recorder.module());

		ignore(recorder.context.call('reset'));

		expect(requests[0].attributes.ep).toEqual({
			'island-call': 'reset',
			'island-call-id': '1',
			'island-key': 'k1'
		});
	});

	it('resolves a call after the components of the response are updated', async () => {
		runtime.register('board', recorder.module());
		const result = recorder.context.call('probe', {}).then(value => ({
			value,
			label: document.getElementById('label')?.textContent
		}));

		respond(requests[0], component('label', '<span id="label">after</span>') +
			data({ call: '1', ok: true, value: { distance: 3 }, invalidate: [] }));

		await expect(result).resolves.toEqual({ value: { distance: 3 }, label: 'after' });
	});

	it('rejects a rejected call with its kind and messages, after its updates', async () => {
		runtime.register('board', recorder.module());
		const result = recorder.context.call('probe', {}).catch((error: IslandCallError) => ({
			error,
			label: document.getElementById('label')?.textContent
		}));

		respond(requests[0], component('label', '<span id="label">after</span>') +
			data({ call: '1', ok: false, error: { kind: 'invalid', messages: ['The game is over.'] } }));

		const { error, label } = await result as { error: IslandCallError; label: string };
		expect(error).toBeInstanceOf(IslandCallError);
		expect(error.kind).toBe('invalid');
		expect(error.messages).toEqual(['The game is over.']);
		expect(label).toBe('after');
	});

	it('rejects a call that does not reach the server', async () => {
		runtime.register('board', recorder.module());
		const result = recorder.context.call('probe', {});

		failRequest(requests[0]);

		await expect(result).rejects.toMatchObject({ kind: 'network' });
	});

	it('rejects a call the server does not answer', async () => {
		runtime.register('board', recorder.module());
		const result = recorder.context.call('probe', {});

		respond(requests[0], '');

		await expect(result).rejects.toMatchObject({ kind: 'error' });
	});

	it('keeps an island alive when Wicket re-renders it, and passes the new props', () => {
		runtime.register('board', recorder.module());
		const { root } = recorder.mounts[0];
		ignore(recorder.context.call('probe', {}));

		respond(requests[0], component('panel',
			`<div id="panel">${host('board1', 'board', 'k1', { label: 'two' }, './call?v2')}</div>`));

		expect(recorder.mounts).toHaveLength(1);
		expect(recorder.unmounts).toBe(0);
		expect(recorder.updates).toEqual([{ label: 'two' }]);
		expect(recorder.context.props).toEqual({ label: 'two' });
		expect(root.isConnected).toBe(true);
		expect(document.getElementById('board1')?.querySelector('[data-wicket-island-root]')).toBe(root);
		expect(root.textContent).toBe('two');

		ignore(recorder.context.call('probe', {}));
		expect(requests[1].attributes.u).toBe('./call?v2');
	});

	it('remounts when Wicket renders a new island in its place', () => {
		runtime.register('board', recorder.module());
		ignore(recorder.context.call('probe', {}));

		respond(requests[0], component('panel',
			`<div id="panel">${host('board2', 'board', 'k2', { label: 'new' })}</div>`));

		expect(recorder.mounts).toHaveLength(2);
		expect(recorder.unmounts).toBe(1);
		expect(recorder.mounts[0].context.signal.aborted).toBe(true);
		expect(recorder.context.props).toEqual({ label: 'new' });
	});

	it('unmounts an island Wicket removed, after the call', () => {
		runtime.register('board', recorder.module());
		ignore(recorder.context.call('probe', {}));

		respond(requests[0], component('panel', '<div id="panel"></div>'));

		expect(recorder.unmounts).toBe(1);
	});

	it('aborts the signal of a removed island before its call settles', async () => {
		runtime.register('board', recorder.module());
		const { context } = recorder.mounts[0];
		const result = context.call('probe', {}).then(() => context.signal.aborted);

		respond(requests[0], component('panel', '<div id="panel"></div>') +
			data({ call: '1', ok: true, value: null }));

		await expect(result).resolves.toBe(true);
	});

	it('rejects calls of an unmounted island without sending them', async () => {
		runtime.register('board', recorder.module());
		const { context } = recorder.mounts[0];
		ignore(context.call('probe', {}));
		respond(requests[0], component('panel', '<div id="panel"></div>'));

		await expect(context.call('probe', {})).rejects.toMatchObject({ kind: 'error' });
		expect(requests).toHaveLength(1);
	});

	it('mounts an island added later, once its module is registered', () => {
		runtime.register('board', recorder.module());
		const charts = new Recorder();
		ignore(recorder.context.call('probe', {}));

		respond(requests[0], component('label', `<span id="label">${host('chart1', 'chart', 'k9', { label: 'c' })}</span>`));
		expect(charts.mounts).toHaveLength(0);

		runtime.register('chart', charts.module());
		expect(charts.mounts).toHaveLength(1);
	});

	it('shares client stores between the islands of a page, beyond their lifetime', () => {
		document.body.innerHTML += host('board2', 'board', 'k2', { label: 'two' });
		runtime.register('board', recorder.module());
		const [first, second] = recorder.mounts.map(mount => mount.context);
		const seen: unknown[] = [];
		second.store<string>('hover').subscribe(() => seen.push(second.store('hover').get()));

		first.store<string>('hover').set('cell');
		first.store<string>('hover').set('cell');

		expect(second.store('hover').get()).toBe('cell');
		expect(seen).toEqual(['cell']);

		ignore(first.call('probe', {}));
		respond(requests[0], component('panel', '<div id="panel"></div>'));
		expect(recorder.unmounts).toBe(1);
		expect(runtime.stores.get('hover').get()).toBe('cell');
	});

	it('binds the value and state of a form island to its hidden input', () => {
		document.body.innerHTML = `<form id="form"><div id="panel">${formHost('point1', 'board', 'k5',
			{ x: 1 }, { required: true, enabled: true, label: 'Point', errors: [] })}</div></form>`;
		runtime.register('board', recorder.module());
		const form = recorder.context.form!;
		const events: string[] = [];
		document.getElementById('form')!.addEventListener('change', () => events.push('change'));
		document.getElementById('form')!.addEventListener('input', () => events.push('input'));

		expect(form.getValue()).toEqual({ x: 1 });
		expect(form.getState().label).toBe('Point');

		form.setValue({ x: 2 });

		expect(document.querySelector<HTMLInputElement>('input[data-wicket-island-value]')!.value)
			.toBe('{"x":2}');
		expect(form.getValue()).toEqual({ x: 2 });
		expect(events).toEqual(['input', 'change']);
	});

	it('reads the value and state of a re-rendered form island from its new host', () => {
		document.body.innerHTML = `<div id="panel">${formHost('point1', 'board', 'k5',
			{ x: 1 }, { required: true, enabled: true, label: 'Point', errors: [] })}</div>`;
		runtime.register('board', recorder.module());
		const form = recorder.context.form!;
		let notified = 0;
		form.subscribe(() => notified++);
		ignore(recorder.context.call('probe', {}));

		respond(requests[0], component('panel', `<div id="panel">${formHost('point1', 'board', 'k5',
			{ x: 9 }, { required: true, enabled: true, label: 'Point', errors: ['Too far'] })}</div>`));

		expect(recorder.mounts).toHaveLength(1);
		expect(form.getValue()).toEqual({ x: 9 });
		expect(form.getState().errors).toEqual(['Too far']);
		expect(notified).toBe(1);
	});

	it('gives islands that are no form components no form binding', () => {
		runtime.register('board', recorder.module());

		expect(recorder.context.form).toBeUndefined();
	});

	describe('page-less calls', () => {
		let fetched: { url: string; init: RequestInit }[];

		function answer(status: number, body: unknown) {
			fetched = [];
			vi.stubGlobal('fetch', (url: string, init: RequestInit) => {
				fetched.push({ url, init });
				return Promise.resolve(new Response(JSON.stringify(body), { status }));
			});
		}

		beforeEach(() => {
			document.body.innerHTML = `<div id="panel">${host('board1', 'board', 'k1', { label: 'one' },
				'./call?board1', { scores: './wicket-islands-call/scores' })}</div>`;
			runtime.register('board', recorder.module());
		});

		afterEach(() => {
			vi.unstubAllGlobals();
		});

		it('posts the payload as JSON with the islands header, outside Wicket Ajax', async () => {
			answer(200, { ok: true, value: [1, 2], invalidate: [] });

			await expect(recorder.context.call('scores', { size: 'SMALL' })).resolves.toEqual([1, 2]);

			expect(requests).toHaveLength(0);
			expect(fetched[0].url).toBe('./wicket-islands-call/scores');
			expect(fetched[0].init.method).toBe('POST');
			expect(fetched[0].init.body).toBe('{"size":"SMALL"}');
			expect((fetched[0].init.headers as Record<string, string>)['X-Wicket-Islands']).toBe('1');
		});

		it('rejects a failed page-less call', async () => {
			answer(200, { ok: false, error: { kind: 'invalid', messages: ['no'] }, invalidate: [] });

			await expect(recorder.context.call('scores')).rejects.toMatchObject({
				kind: 'invalid', messages: ['no']
			});
		});

		it('rejects a refused page-less call as forbidden', async () => {
			answer(403, {});

			await expect(recorder.context.call('scores')).rejects.toMatchObject({ kind: 'forbidden' });
		});

		it('applies the invalidations of a page-less answer', async () => {
			answer(200, { ok: true, value: null, invalidate: ['scores'] });
			const invalidated: string[][] = [];
			runtime.onInvalidate(keys => invalidated.push([...keys]));

			await recorder.context.call('scores');

			expect(invalidated).toEqual([['scores']]);
		});
	});

	it('invalidates the queries of the page under the keys the server names', () => {
		runtime.register('board', recorder.module());
		const queries = runtime.queryClient.getQueryCache();
		runtime.queryClient.setQueryData(['scores', { size: 'SMALL' }], [1]);
		runtime.queryClient.setQueryData(['other'], [2]);
		ignore(recorder.context.call('probe', {}));

		respond(requests[0], data({ invalidate: ['scores'] }));

		expect(queries.find({ queryKey: ['scores', { size: 'SMALL' }] })?.state.isInvalidated).toBe(true);
		expect(queries.find({ queryKey: ['other'] })?.state.isInvalidated).toBe(false);
	});

	it('tells listeners which query keys were invalidated', () => {
		runtime.register('board', recorder.module());
		const invalidated: string[][] = [];
		runtime.onInvalidate(keys => invalidated.push([...keys]));
		ignore(recorder.context.call('probe', {}));

		respond(requests[0], data({ call: '1', ok: true, value: null, invalidate: ['scores'] }));

		expect(invalidated).toEqual([['scores']]);
	});
});

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

// @vitest-environment node

import { describe, expect, it } from 'vitest';
import wicketIslands from '../src/index.js';

type Hook = (...args: unknown[]) => unknown;

function hook(plugin: object, name: string): Hook {
	return (plugin as Record<string, Hook>)[name];
}

function plugin(command: 'build' | 'serve', devPreamble?: string, adapter?: string) {
	const instance = wicketIslands({
		islands: { 'treasure-board': 'src/islands/TreasureBoard.tsx' },
		adapter,
		devPreamble
	});
	const config = hook(instance, 'config')({}, { command, mode: 'development' });
	hook(instance, 'configResolved')({ root: '/app' });
	return { instance, config };
}

describe('wicketIslands', () => {
	it('builds one entry per island type, named after it, with a manifest', () => {
		const { config } = plugin('build');

		expect(config).toEqual({
			base: './',
			build: {
				manifest: true,
				rollupOptions: { input: { 'treasure-board': 'virtual:wicket-island:treasure-board' } }
			},
			optimizeDeps: { entries: ['src/islands/TreasureBoard.tsx'] }
		});
	});

	it('keeps the base the application sets, and the dev server\'s', () => {
		const instance = wicketIslands({ islands: { demo: 'src/demo.js' } });

		expect(hook(instance, 'config')({ base: '/static/' }, { command: 'build' })).not.toHaveProperty('base');
		expect(hook(instance, 'config')({}, { command: 'serve' })).not.toHaveProperty('base');
	});

	it('wraps the default export with the adapter\'s defineIsland', () => {
		const { instance } = plugin('build', undefined, 'wicket-islands-react');

		expect(hook(instance, 'load')('\0wicket-island:treasure-board')).toBe([
			'import island from "/app/src/islands/TreasureBoard.tsx";',
			'import { registerIsland } from \'wicket-islands\';',
			'import { defineIsland } from "wicket-islands-react";',
			'registerIsland("treasure-board", defineIsland(island));'
		].join('\n'));
	});

	it('generates an entry that registers the island module', () => {
		const { instance } = plugin('build', 'preamble();');
		const id = hook(instance, 'resolveId')('virtual:wicket-island:treasure-board');

		expect(id).toBe('\0wicket-island:treasure-board');
		expect(hook(instance, 'load')(id)).toBe([
			'import island from "/app/src/islands/TreasureBoard.tsx";',
			'import { registerIsland } from \'wicket-islands\';',
			'registerIsland("treasure-board", island);'
		].join('\n'));
	});

	it('runs the dev preamble first when serving', () => {
		const { instance } = plugin('serve', 'preamble();');

		const entry = hook(instance, 'load')('\0wicket-island:treasure-board') as string;

		expect(entry.split('\n')[0]).toBe('import "virtual:wicket-islands-preamble";');
		expect(hook(instance, 'load')(hook(instance, 'resolveId')('virtual:wicket-islands-preamble')))
			.toBe('preamble();');
	});

	it('serves the entry of an island type under /@wicket-islands/', () => {
		const { instance } = plugin('serve');
		const middlewares: Hook[] = [];
		hook(instance, 'configureServer')({ middlewares: { use: (m: Hook) => middlewares.push(m) } });

		const known = { url: '/@wicket-islands/treasure-board?t=1' };
		const unknown = { url: '/@wicket-islands/other' };
		let nexts = 0;
		middlewares[0](known, {}, () => nexts++);
		middlewares[0](unknown, {}, () => nexts++);

		expect(known.url).toBe('/@id/__x00__wicket-island:treasure-board');
		expect(unknown.url).toBe('/@wicket-islands/other');
		expect(nexts).toBe(2);
	});

	it('rejects island types Wicket would not accept', () => {
		expect(() => wicketIslands({ islands: { 'a b': 'x.tsx' } })).toThrow(/Invalid island type/);
	});
});

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

import path from 'node:path';

const TYPE_PATTERN = /^[A-Za-z0-9_-]+$/;
const ENTRY_PREFIX = 'virtual:wicket-island:';
const RESOLVED_ENTRY_PREFIX = '\0wicket-island:';
const PREAMBLE_ID = 'virtual:wicket-islands-preamble';
const RESOLVED_PREAMBLE_ID = '\0wicket-islands-preamble';
const DEV_PATH = '/@wicket-islands/';

/**
 * Builds the islands of a Wicket application, one entry per island type, and serves them to a
 * Wicket application running in development mode.
 *
 * Each island type becomes an input of the build named after the type, so Wicket finds its
 * entry in the manifest. Its entry module imports the island's module, turns its default export
 * into an island with the adapter's `defineIsland` when an adapter is given, and registers it with
 * the runtime. In development, the entry for a type is served at `/@wicket-islands/<type>`.
 *
 * @param {import('./index').WicketIslandsOptions} options
 * @returns {import('vite').Plugin}
 */
export default function wicketIslands(options) {
	const islands = options.islands;
	for (const type of Object.keys(islands)) {
		if (!TYPE_PATTERN.test(type)) {
			throw new Error(`Invalid island type '${type}': use letters, digits, '-' and '_'.`);
		}
	}

	let root = process.cwd();
	let serving = false;

	return {
		name: 'wicket-islands',

		config(config, env) {
			serving = env.command === 'serve';
			/** @type {import('vite').UserConfig} */
			const result = {
				build: {
					manifest: true,
					rollupOptions: {
						input: Object.fromEntries(Object.keys(islands)
							.map(type => [type, ENTRY_PREFIX + type]))
					}
				},
				optimizeDeps: {
					entries: Object.values(islands)
				}
			};
			if (!serving && config.base === undefined) {
				// Wicket serves the build under its own mount, so URLs resolve against the module
				result.base = './';
			}
			return result;
		},

		configResolved(config) {
			root = config.root;
		},

		resolveId(id) {
			if (id.startsWith(ENTRY_PREFIX)) {
				return RESOLVED_ENTRY_PREFIX + id.slice(ENTRY_PREFIX.length);
			}
			if (id === PREAMBLE_ID) {
				return RESOLVED_PREAMBLE_ID;
			}
			return null;
		},

		load(id) {
			if (id === RESOLVED_PREAMBLE_ID) {
				return options.devPreamble ?? '';
			}
			if (!id.startsWith(RESOLVED_ENTRY_PREFIX)) {
				return null;
			}
			const type = id.slice(RESOLVED_ENTRY_PREFIX.length);
			const file = islands[type];
			if (!file) {
				throw new Error(`Unknown island type '${type}'.`);
			}
			const lines = [];
			if (serving && options.devPreamble) {
				lines.push(`import ${JSON.stringify(PREAMBLE_ID)};`);
			}
			lines.push(
				`import island from ${JSON.stringify(toPosixPath(path.resolve(root, file)))};`,
				`import { registerIsland } from 'wicket-islands';`);
			if (options.adapter) {
				lines.push(
					`import { defineIsland } from ${JSON.stringify(options.adapter)};`,
					`registerIsland(${JSON.stringify(type)}, defineIsland(island));`);
			} else {
				lines.push(`registerIsland(${JSON.stringify(type)}, island);`);
			}
			return lines.join('\n');
		},

		configureServer(server) {
			server.middlewares.use((request, _response, next) => {
				if (request.url?.startsWith(DEV_PATH)) {
					const type = decodeURIComponent(request.url.slice(DEV_PATH.length).split('?')[0]);
					if (Object.hasOwn(islands, type)) {
						request.url = '/@id/__x00__wicket-island:' + type;
					}
				}
				next();
			});
		}
	};
}

/**
 * @param {string} file
 * @returns {string}
 */
function toPosixPath(file) {
	return file.split(path.sep).join('/');
}

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

import path from 'node:path';
import { build, type Rollup } from 'vite';
import { describe, expect, it } from 'vitest';
import wicketIslands from '../src/index.js';

describe('wicketIslands in a Vite build', () => {
	it('produces a manifest entry per island type that Wicket can find by name', async () => {
		const output = await build({
			root: path.resolve(process.cwd(), 'packages/vite-plugin/test/fixture'),
			logLevel: 'silent',
			plugins: [wicketIslands({ islands: { demo: 'src/demo.js' } })],
			build: { write: false, minify: false }
		}) as Rollup.RollupOutput;

		const manifestAsset = output.output.find(file => file.fileName === '.vite/manifest.json');
		expect(manifestAsset).toBeDefined();
		const manifest = JSON.parse((manifestAsset as Rollup.OutputAsset).source as string) as
			Record<string, { file: string; name?: string; isEntry?: boolean }>;
		const entry = Object.values(manifest).find(chunk => chunk.name === 'demo' && chunk.isEntry);
		expect(entry).toBeDefined();

		const code = (output.output.find(file => file.fileName === entry!.file) as Rollup.OutputChunk).code;
		expect(code).toContain('registerIsland("demo"');
		expect(code).toContain('root.textContent = context.props.label');
	});
});

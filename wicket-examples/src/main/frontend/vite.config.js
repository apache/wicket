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

import { fileURLToPath } from 'node:url';
import react from '@vitejs/plugin-react';
import { defineConfig } from 'vite';
import wicketIslands from 'vite-plugin-wicket-islands';
import { reactIslands } from 'wicket-islands-react/vite';

const islandPackages = fileURLToPath(
	new URL('../../../../wicket-experimental/wicket-islands/npm/packages', import.meta.url));

export default defineConfig({
	plugins: [
		react(),
		wicketIslands({
			islands: {
				'treasure-board': 'src/treasurehunt/TreasureBoard.tsx',
				'probe-history': 'src/treasurehunt/ProbeHistory.tsx',
				'player-entry': 'src/treasurehunt/PlayerEntryForm.tsx',
				'highscore-list': 'src/treasurehunt/HighScoreList.tsx'
			},
			...reactIslands
		})
	],
	resolve: {
		// the island packages are linked from the Wicket source tree; their imports resolve here
		dedupe: ['react', 'react-dom', 'wicket-islands', '@tanstack/query-core',
			'@tanstack/react-query']
	},
	server: {
		port: 5173,
		strictPort: true,
		origin: 'http://localhost:5173',
		fs: {
			allow: ['.', islandPackages]
		}
	},
	build: {
		outDir: '../../../target/generated-resources/wicket-islands/META-INF/wicket-islands',
		emptyOutDir: true
	}
});

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
import { svelte } from '@sveltejs/vite-plugin-svelte';
import { configDefaults, defineConfig } from 'vitest/config';

export default defineConfig({
	test: {
		projects: [
			{
				test: {
					name: 'islands',
					environment: 'jsdom',
					include: ['packages/*/test/**/*.test.{ts,tsx}'],
					exclude: [...configDefaults.exclude, 'packages/svelte/**']
				}
			},
			{
				plugins: [svelte()],
				resolve: {
					// Svelte's server build cannot mount components
					conditions: ['browser']
				},
				test: {
					name: 'svelte',
					environment: 'jsdom',
					include: ['packages/svelte/test/**/*.test.ts']
				}
			}
		]
	}
});

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

import type { Plugin } from 'vite';

export interface WicketIslandsOptions {
	/**
	 * The island modules by island type, relative to Vite's root. With an adapter, each module's
	 * default export is a component of the adapter's framework; without one, it is an island
	 * module made by an adapter's `defineIsland`.
	 */
	islands: Record<string, string>;

	/**
	 * The framework adapter whose `defineIsland` turns a module's default export into an island,
	 * e.g. `wicket-islands-react`. Exporting the plain component keeps the module eligible for the
	 * framework's hot reload.
	 */
	adapter?: string;

	/**
	 * Code run before any island module in development, e.g. React Fast Refresh's preamble.
	 */
	devPreamble?: string;
}

/**
 * Builds the islands of a Wicket application and serves them to it in development. A framework
 * adapter provides the `adapter` and `devPreamble` options as a preset, e.g. `reactIslands` of
 * wicket-islands-react/vite.
 */
export default function wicketIslands(options: WicketIslandsOptions): Plugin;

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

import { IslandRuntime } from './runtime';
import type { IslandModule } from './types';

export { IslandRuntime } from './runtime';
export type { FormBinding, FormState } from './form';
export type { ClientStore } from './store';
export { IslandCallError } from './types';
export type { IslandCallErrorKind, IslandContext, IslandInstance, IslandModule } from './types';

const GLOBAL_KEY = '__wicketIslandsRuntime';

/**
 * The runtime of the page. It is kept on the window, so a page that ends up with two copies of
 * this package still has a single runtime.
 */
export function runtime(): IslandRuntime {
	const scope = globalThis as { [GLOBAL_KEY]?: IslandRuntime };
	return scope[GLOBAL_KEY] ??= new IslandRuntime();
}

/**
 * Registers the module for an island type and mounts every island of that type on the page.
 * Called by the entry modules the Vite plugin generates.
 */
export function registerIsland<P>(type: string, module: IslandModule<P>): void {
	runtime().register(type, module as IslandModule<unknown>);
}

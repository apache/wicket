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

import type { QueryClient } from '@tanstack/query-core';
import type { FormBinding } from './form';
import type { ClientStore } from './store';

/**
 * The client-side half of an island: what a framework adapter provides for an island type.
 */
export interface IslandModule<P> {
	/**
	 * Mounts the island into its root element.
	 */
	mount(root: HTMLElement, context: IslandContext<P>): IslandInstance<P>;
}

/**
 * A mounted island.
 */
export interface IslandInstance<P> {
	/**
	 * Delivers new props from the server. The island keeps its client-side state.
	 */
	update(props: P): void;

	/**
	 * Unmounts the island; it is not used again.
	 */
	unmount(): void;
}

/**
 * What the runtime gives a mounted island.
 */
export interface IslandContext<P> {
	/**
	 * The current props.
	 */
	readonly props: P;

	/**
	 * The key identifying the island across re-renders.
	 */
	readonly key: string;

	/**
	 * Aborted when the island is unmounted, before any of its pending calls settles.
	 */
	readonly signal: AbortSignal;

	/**
	 * The value and form state of a form island; absent for other islands.
	 */
	readonly form?: FormBinding<unknown>;

	/**
	 * The query client of the page, shared by every island on the page. Query keys the server marks
	 * as stale are invalidated in it.
	 */
	readonly queryClient: QueryClient;

	/**
	 * The key to cache the results of a call under in the query client: the name first, so the
	 * server's invalidation of the name reaches it, then who shares the results, then the payload.
	 */
	queryKey(name: string, payload?: unknown): readonly unknown[];

	/**
	 * The page's client store of the given name, shared by every island on the page.
	 */
	store<S>(name: string): ClientStore<S>;

	/**
	 * Calls the island on the server. The returned promise settles after every update the
	 * response carries has been applied: components, props of other islands, invalidations. A
	 * name the island registered as page-less is called with a plain request instead, outside
	 * the page lock; its answer carries invalidations but no component updates.
	 */
	call<R = unknown>(name: string, payload?: unknown): Promise<R>;
}

/**
 * Why a call failed.
 */
export type IslandCallErrorKind = 'invalid' | 'forbidden' | 'expired' | 'error' | 'network';

/**
 * The rejection of a failed island call.
 */
export class IslandCallError extends Error {
	readonly kind: IslandCallErrorKind;

	readonly messages: readonly string[];

	constructor(kind: IslandCallErrorKind, messages: readonly string[]) {
		super(messages.length > 0 ? messages.join('; ') : kind);
		this.name = 'IslandCallError';
		this.kind = kind;
		this.messages = messages;
	}
}

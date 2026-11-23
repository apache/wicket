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

import { getContext, mount, unmount, type Component } from 'svelte';
import { readable, type Readable, type Writable } from 'svelte/store';
import type { FormBinding, FormState, IslandCallError, IslandContext,
	IslandModule } from 'wicket-islands';

export { IslandCallError, cspNonce } from 'wicket-islands';
export type { FormState, IslandCallErrorKind } from 'wicket-islands';

const BINDING = Symbol('wicket-islands-svelte');

type Props = Record<string, unknown>;

/**
 * Makes a Svelte component the client-side peer of a Wicket island. The component receives the
 * island's props as its own props, and keeps its state when the server sends new ones.
 *
 * ```ts
 * export default defineIsland(TreasureHint);
 * ```
 */
export function defineIsland<P extends Props>(component: Component<P>): IslandModule<P> {
	return {
		mount(root, context) {
			const props = $state(copy(context.props));
			const instance = mount(component, {
				target: root,
				props: props as P,
				context: new Map([[BINDING, context]])
			});
			return {
				update(next) {
					for (const name of Object.keys(props)) {
						if (!next || !Object.hasOwn(next, name)) {
							delete props[name];
						}
					}
					Object.assign(props, next);
				},
				unmount() {
					void unmount(instance);
				}
			};
		}
	};
}

function copy(props: unknown): Props {
	return { ...(props as Props | null) };
}

/**
 * What a component inside an island can use.
 */
export interface Island {
	/**
	 * The key identifying the island across re-renders.
	 */
	readonly key: string;

	/**
	 * Aborted when the island is unmounted.
	 */
	readonly signal: AbortSignal;

	/**
	 * Calls the island on the server. The promise settles after every update the response carries
	 * has been applied, so the props are current when it does.
	 */
	call<R = unknown>(name: string, payload?: unknown): Promise<R>;
}

/**
 * The island of the component being initialized. Like every function of this module that reads
 * the island, it can only be called while a component inside an island initializes.
 */
export function getIsland(): Island {
	const context = islandContext('getIsland');
	return {
		key: context.key,
		signal: context.signal,
		call: context.call
	};
}

/**
 * The page's client store of the given name as a Svelte store. Every island on the page using the
 * same name shares the value, whatever framework the island is written in; it is not sent to the
 * server unless a call includes it.
 *
 * ```svelte
 * const hover = islandStore<Cell>('treasure-hover');
 * ...
 * {$hover?.row}
 * ```
 */
export function islandStore<S>(name: string): Writable<S | undefined> {
	const store = islandContext('islandStore').store<S>(name);
	return {
		subscribe: run => {
			run(store.get());
			return store.subscribe(() => run(store.get()));
		},
		set: value => store.set(value as S),
		update: updater => store.set(updater(store.get()) as S)
	};
}

/**
 * The state of a call made by {@link islandQuery}.
 */
export interface IslandQueryState<R> {
	readonly data?: R;
	readonly error?: IslandCallError;
	readonly loading: boolean;
}

/**
 * Fetches data with a call of the island while the store has subscribers, and fetches it again
 * whenever the server marks `name` as stale (`invalidate(name)`). The data of the previous call is
 * kept while the next one runs. Nothing is cached across islands.
 */
export function islandQuery<R>(name: string, payload?: unknown): Readable<IslandQueryState<R>> {
	const context = islandContext('islandQuery');
	return readable<IslandQueryState<R>>({ loading: true }, set => {
		let latest = 0;
		let state: IslandQueryState<R> = { loading: true };
		const change = (next: IslandQueryState<R>) => {
			state = next;
			set(next);
		};
		const fetch = () => {
			const call = ++latest;
			if (!state.loading) {
				change({ ...state, loading: true });
			}
			context.call<R>(name, payload).then(
				data => call === latest && change({ data, loading: false }),
				(error: IslandCallError) => call === latest
					&& change({ data: state.data, error, loading: false }));
		};
		fetch();
		const stop = context.onInvalidate(names => {
			if (names.includes(name)) {
				fetch();
			}
		});
		return () => {
			latest++;
			stop();
		};
	});
}

/**
 * The value of a form island as a Svelte store. Setting it updates the hidden input Wicket submits
 * with the form, and fires its input and change events.
 */
export function formValue<T>(): Writable<T | undefined> {
	const form = formBinding('formValue') as FormBinding<T>;
	return {
		subscribe: run => {
			run(form.getValue());
			return form.subscribe(() => run(form.getValue()));
		},
		set: value => form.setValue(value),
		update: updater => form.setValue(updater(form.getValue()))
	};
}

/**
 * What the server says about a form island, as a Svelte store: required, enabled, its label and
 * its feedback messages, as of the last time Wicket rendered it.
 */
export function formState(): Readable<FormState> {
	const form = formBinding('formState');
	return {
		subscribe: run => {
			run(form.getState());
			return form.subscribe(() => run(form.getState()));
		}
	};
}

function formBinding(name: string): FormBinding<unknown> {
	const form = islandContext(name).form;
	if (!form) {
		throw new Error(`${name}() can only be used inside a form island.`);
	}
	return form;
}

function islandContext(name: string): IslandContext<unknown> {
	const context = getContext<IslandContext<unknown> | undefined>(BINDING);
	if (!context) {
		throw new Error(
			`${name}() can only be used while a component passed to defineIsland() initializes.`);
	}
	return context;
}

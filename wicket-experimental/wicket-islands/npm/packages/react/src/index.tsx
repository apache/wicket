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

import { QueryClientProvider, useMutation, useQuery, type UseMutationOptions,
	type UseMutationResult, type UseQueryOptions, type UseQueryResult } from '@tanstack/react-query';
import { createContext, useContext, useSyncExternalStore, type ComponentType } from 'react';
import { createRoot } from 'react-dom/client';
import type { FormBinding, FormState, IslandCallError, IslandContext,
	IslandModule } from 'wicket-islands';

export { IslandCallError } from 'wicket-islands';
export type { FormState, IslandCallErrorKind } from 'wicket-islands';

interface PropsStore<P> {
	get(): P;
	set(props: P): void;
	subscribe(listener: () => void): () => void;
}

function createPropsStore<P>(initial: P): PropsStore<P> {
	let current = initial;
	const listeners = new Set<() => void>();
	return {
		get: () => current,
		set(props) {
			current = props;
			listeners.forEach(listener => listener());
		},
		subscribe(listener) {
			listeners.add(listener);
			return () => listeners.delete(listener);
		}
	};
}

interface Binding {
	context: IslandContext<unknown>;
	props: PropsStore<unknown>;
}

const BindingContext = createContext<Binding | null>(null);

/**
 * Makes a React component the client-side peer of a Wicket island. The component receives the
 * island's props as its own props, and is re-rendered in place, keeping its state, whenever the
 * server sends new ones.
 *
 * ```tsx
 * export default defineIsland(TreasureBoard);
 * ```
 */
export function defineIsland<P extends object>(Component: ComponentType<P>): IslandModule<P> {
	function IslandView({ binding }: { binding: Binding }) {
		const props = useSyncExternalStore(binding.props.subscribe, binding.props.get) as P;
		return (
			<QueryClientProvider client={binding.context.queryClient}>
				<BindingContext.Provider value={binding}>
					<Component {...props} />
				</BindingContext.Provider>
			</QueryClientProvider>
		);
	}

	return {
		mount(root, context) {
			const binding: Binding = {
				context: context as IslandContext<unknown>,
				props: createPropsStore<unknown>(context.props)
			};
			const reactRoot = createRoot(root);
			reactRoot.render(<IslandView binding={binding} />);
			return {
				update: props => binding.props.set(props),
				unmount: () => reactRoot.unmount()
			};
		}
	};
}

/**
 * What a component inside an island can use.
 */
export interface UseIsland<P> {
	/**
	 * The island's current props.
	 */
	props: P;

	/**
	 * The key identifying the island across re-renders.
	 */
	key: string;

	/**
	 * Aborted when the island is unmounted.
	 */
	signal: AbortSignal;

	/**
	 * Calls the island on the server. The promise settles after every update the response carries
	 * has been applied, so the props are current when it does.
	 */
	call<R = unknown>(name: string, payload?: unknown): Promise<R>;
}

/**
 * Reads and writes the page's client store of the given name. Every island on the page using the
 * same name shares the value; it is not sent to the server unless a call includes it.
 *
 * ```tsx
 * const [hover, setHover] = useStore<Cell | null>('treasure-hover');
 * ```
 */
export function useStore<S>(name: string): [S | undefined, (value: S) => void] {
	const binding = useContext(BindingContext);
	if (!binding) {
		throw new Error('useStore() can only be used inside a component passed to defineIsland().');
	}
	const store = binding.context.store<S>(name);
	const value = useSyncExternalStore(store.subscribe, store.get);
	return [value, store.set];
}

/**
 * Fetches data with a call of the island, cached in the page's query client under
 * `[name, payload]`. When the server marks `name` as stale (`invalidate(name)`), every island
 * showing it fetches it again.
 *
 * ```tsx
 * const scores = useIslandQuery<Score[]>('scores', { size });
 * ```
 */
export function useIslandQuery<R>(name: string, payload?: unknown,
	options?: Omit<UseQueryOptions<R, IslandCallError>, 'queryKey' | 'queryFn'>
): UseQueryResult<R, IslandCallError> {
	const { call } = useIsland();
	return useQuery<R, IslandCallError>({
		...options,
		queryKey: payload === undefined ? [name] : [name, payload],
		queryFn: () => call<R>(name, payload)
	});
}

/**
 * Changes data with a call of the island, e.g. for optimistic updates with TanStack Query's
 * mutation callbacks.
 */
export function useIslandMutation<Q, R = unknown>(name: string,
	options?: Omit<UseMutationOptions<R, IslandCallError, Q>, 'mutationFn'>
): UseMutationResult<R, IslandCallError, Q> {
	const { call } = useIsland();
	return useMutation<R, IslandCallError, Q>({
		...options,
		mutationFn: payload => call<R>(name, payload)
	});
}

/**
 * The value of a form island, and the function to change it. Changing it updates the hidden input
 * Wicket submits with the form, and fires its input and change events.
 *
 * ```tsx
 * const [player, setPlayer] = useFormValue<PlayerEntry>();
 * ```
 */
export function useFormValue<T>(): [T | undefined, (value: T | undefined) => void] {
	const form = formBinding('useFormValue');
	const value = useSyncExternalStore(form.subscribe, form.getValue) as T | undefined;
	return [value, form.setValue as (value: T | undefined) => void];
}

/**
 * What the server says about a form island: required, enabled, its label and its feedback
 * messages, as of the last time Wicket rendered it.
 */
export function useFormState(): FormState {
	const form = formBinding('useFormState');
	return useSyncExternalStore(form.subscribe, form.getState);
}

function formBinding(hook: string): FormBinding<unknown> {
	const binding = useContext(BindingContext);
	if (!binding?.context.form) {
		throw new Error(`${hook}() can only be used inside a form island.`);
	}
	return bound(binding.context.form);
}

const boundBindings = new WeakMap<FormBinding<unknown>, FormBinding<unknown>>();

/**
 * The binding with its methods bound, so they can be passed around; the same object every time,
 * so hooks don't resubscribe on every render.
 */
function bound(form: FormBinding<unknown>): FormBinding<unknown> {
	let result = boundBindings.get(form);
	if (!result) {
		result = {
			getValue: () => form.getValue(),
			setValue: value => form.setValue(value),
			getState: () => form.getState(),
			subscribe: listener => form.subscribe(listener)
		};
		boundBindings.set(form, result);
	}
	return result;
}

/**
 * Gives a component anywhere inside an island access to it.
 */
export function useIsland<P>(): UseIsland<P> {
	const binding = useContext(BindingContext);
	if (!binding) {
		throw new Error('useIsland() can only be used inside a component passed to defineIsland().');
	}
	const props = useSyncExternalStore(binding.props.subscribe, binding.props.get) as P;
	const { context } = binding;
	return {
		props,
		key: context.key,
		signal: context.signal,
		call: context.call
	};
}

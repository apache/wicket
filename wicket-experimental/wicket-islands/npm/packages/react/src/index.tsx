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

import { createContext, useContext, useSyncExternalStore, type ComponentType } from 'react';
import { createRoot } from 'react-dom/client';
import type { IslandContext, IslandModule } from 'wicket-islands';

export { IslandCallError } from 'wicket-islands';
export type { IslandCallErrorKind } from 'wicket-islands';

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
			<BindingContext.Provider value={binding}>
				<Component {...props} />
			</BindingContext.Provider>
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

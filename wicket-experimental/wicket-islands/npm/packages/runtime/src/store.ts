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

/**
 * A named value shared by the islands of a page, without a round trip to the server.
 */
export interface ClientStore<S> {
	/**
	 * The current value, `undefined` until one is set.
	 */
	get(): S | undefined;

	/**
	 * Sets the value and notifies the subscribers when it changed.
	 */
	set(value: S): void;

	/**
	 * Notifies the listener of every change; returns the function that stops it.
	 */
	subscribe(listener: () => void): () => void;
}

/**
 * The stores of a page, by name. A store lives as long as the page in the browser, so it outlasts
 * the islands that use it.
 */
export class ClientStores {
	private readonly stores = new Map<string, ClientStore<unknown>>();

	get<S>(name: string): ClientStore<S> {
		let store = this.stores.get(name);
		if (!store) {
			store = createStore<unknown>();
			this.stores.set(name, store);
		}
		return store as ClientStore<S>;
	}
}

function createStore<S>(): ClientStore<S> {
	let value: S | undefined;
	const listeners = new Set<() => void>();
	return {
		get: () => value,
		set(next) {
			if (Object.is(value, next)) {
				return;
			}
			value = next;
			for (const listener of [...listeners]) {
				listener();
			}
		},
		subscribe(listener) {
			listeners.add(listener);
			return () => listeners.delete(listener);
		}
	};
}

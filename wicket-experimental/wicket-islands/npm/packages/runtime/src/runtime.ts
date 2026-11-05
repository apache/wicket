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

import { IslandCallError, type IslandCallErrorKind, type IslandContext, type IslandInstance,
	type IslandModule } from './types';
import { QueryClient } from '@tanstack/query-core';
import { HostFormBinding, type FormState } from './form';
import { ClientStores } from './store';
import { TOPIC_AJAX_CALL_DATA, TOPIC_AJAX_CALL_DONE, TOPIC_DOM_NODE_ADDED, wicket,
	type WicketAjaxAttributes, type WicketApi } from './wicket';

const ATTR_TYPE = 'data-wicket-island';
const ATTR_KEY = 'data-wicket-island-key';
const SELECTOR_STATE = ':scope > script[data-wicket-island-state]';
const SELECTOR_ROOT = ':scope > [data-wicket-island-root]';

const DATA_NAME = 'wicket-islands';
const PARAM_CALL = 'island-call';
const PARAM_CALL_ID = 'island-call-id';
const PARAM_PAYLOAD = 'island-payload';
const PARAM_KEY = 'island-key';
const PAGELESS_HEADER = 'X-Wicket-Islands';

interface IslandState {
	props: unknown;
	calls: string[];
	ajax: WicketAjaxAttributes;
	pageless?: Record<string, string>;
	form?: FormState;
}

interface MountedIsland {
	readonly type: string;
	readonly key: string;
	readonly root: HTMLElement;
	readonly abort: AbortController;
	host: HTMLElement;
	state: IslandState;
	form?: HostFormBinding;
	instance?: IslandInstance<unknown>;
}

interface PendingCall {
	resolve(value: unknown): void;
	reject(error: IslandCallError): void;
}

interface Envelope {
	call?: string;
	ok?: boolean;
	value?: unknown;
	error?: { kind: IslandCallErrorKind; messages: string[] };
	invalidate?: string[];
}

/**
 * Finds island hosts in the page, mounts the module registered for their type, keeps them alive
 * when Wicket re-renders them, unmounts them when they are gone, and carries their calls.
 */
export class IslandRuntime {
	private readonly modules = new Map<string, IslandModule<unknown>>();

	private readonly mounted = new Map<string, MountedIsland>();

	private readonly pending = new Map<string, PendingCall>();

	private readonly invalidationListeners = new Set<(keys: readonly string[]) => void>();

	readonly stores = new ClientStores();

	readonly queryClient = new QueryClient({
		defaultOptions: {
			queries: {
				retry: (failures, error) => failures < 2 && error instanceof IslandCallError &&
					(error.kind === 'network' || error.kind === 'error')
			}
		}
	});

	constructor() {
		this.onInvalidate(keys => {
			for (const key of keys) {
				void this.queryClient.invalidateQueries({ queryKey: [key] });
			}
		});
	}

	private api?: WicketApi;

	private callSequence = 0;

	register(type: string, module: IslandModule<unknown>): void {
		this.modules.set(type, module);
		this.connect();
		this.scan(document.documentElement);
	}

	/**
	 * Listens to the query keys the server marks as stale.
	 */
	onInvalidate(listener: (keys: readonly string[]) => void): () => void {
		this.invalidationListeners.add(listener);
		return () => this.invalidationListeners.delete(listener);
	}

	private connect(): void {
		if (this.api) {
			return;
		}
		const api = wicket();
		this.api = api;
		api.Event.subscribe(TOPIC_DOM_NODE_ADDED,
			(_event, element: Element) => this.scan(element));
		api.Event.subscribe(TOPIC_AJAX_CALL_DATA,
			(_event, _attributes: unknown, name: string, value: string) => {
				if (name === DATA_NAME) {
					this.receive(value);
				}
			});
		api.Event.subscribe(TOPIC_AJAX_CALL_DONE, () => this.sweep());
	}

	private scan(element: Element): void {
		const hosts: Element[] = [];
		if (element.matches(`[${ATTR_TYPE}]`)) {
			hosts.push(element);
		}
		hosts.push(...element.querySelectorAll(`[${ATTR_TYPE}]`));
		for (const host of hosts) {
			this.attach(host as HTMLElement);
		}
	}

	private attach(host: HTMLElement): void {
		const key = host.getAttribute(ATTR_KEY);
		const type = host.getAttribute(ATTR_TYPE);
		if (!key || !type) {
			return;
		}

		const existing = this.mounted.get(key);
		if (existing) {
			if (existing.host !== host && !existing.host.isConnected) {
				this.transfer(existing, host);
			}
			return;
		}

		const module = this.modules.get(type);
		if (!module) {
			return;
		}

		const island: MountedIsland = {
			type,
			key,
			host,
			root: findRoot(host),
			state: readState(host),
			abort: new AbortController()
		};
		if (island.state.form) {
			island.form = new HostFormBinding(() => island.host, () => island.state.form!);
		}
		this.mounted.set(key, island);
		island.instance = module.mount(island.root, this.createContext(island));
	}

	/**
	 * Moves a mounted island into the host Wicket rendered in place of its old one.
	 */
	private transfer(island: MountedIsland, host: HTMLElement): void {
		findRoot(host).replaceWith(island.root);
		island.host = host;
		island.state = readState(host);
		island.instance?.update(island.state.props);
		island.form?.refresh();
	}

	private sweep(): void {
		for (const island of [...this.mounted.values()]) {
			if (!island.host.isConnected) {
				this.unmount(island);
			}
		}
	}

	private unmount(island: MountedIsland): void {
		this.mounted.delete(island.key);
		island.abort.abort();
		try {
			island.instance?.unmount();
		} catch (e) {
			console.error(`Wicket Islands: unmounting island ${island.type} failed`, e);
		}
	}

	private createContext(island: MountedIsland): IslandContext<unknown> {
		return {
			get props() {
				return island.state.props;
			},
			key: island.key,
			signal: island.abort.signal,
			form: island.form,
			queryClient: this.queryClient,
			store: name => this.stores.get(name),
			call: <R>(name: string, payload?: unknown) => this.call(island, name, payload) as Promise<R>
		};
	}

	private call(island: MountedIsland, name: string, payload: unknown): Promise<unknown> {
		if (island.abort.signal.aborted) {
			return Promise.reject(new IslandCallError('error', ['The island is no longer mounted.']));
		}

		const pageless = island.state.pageless?.[name];
		if (pageless) {
			return this.callPageless(pageless, payload);
		}

		const id = String(++this.callSequence);
		const parameters: Record<string, string> = {
			[PARAM_CALL]: name,
			[PARAM_CALL_ID]: id,
			[PARAM_KEY]: island.key
		};
		if (payload !== undefined) {
			parameters[PARAM_PAYLOAD] = JSON.stringify(payload);
		}

		return new Promise((resolve, reject) => {
			this.pending.set(id, { resolve, reject });
			const ajax = island.state.ajax;
			this.api!.Ajax.ajax({
				...ajax,
				ep: parameters,
				fh: [...(ajax.fh ?? []), () => this.fail(id, 'network',
					'The call did not complete.')],
				dh: [...(ajax.dh ?? []), () => this.fail(id, 'error',
					'The server did not answer the call.')]
			});
		});
	}

	private async callPageless(url: string, payload: unknown): Promise<unknown> {
		let response: Response;
		try {
			response = await fetch(url, {
				method: 'POST',
				credentials: 'same-origin',
				headers: { 'Content-Type': 'application/json', [PAGELESS_HEADER]: '1' },
				body: payload === undefined ? '' : JSON.stringify(payload)
			});
		} catch {
			throw new IslandCallError('network', ['The call did not complete.']);
		}
		if (!response.ok) {
			throw response.status === 403
				? new IslandCallError('forbidden', ['The call is not allowed.'])
				: new IslandCallError('error', [`The server answered ${response.status}.`]);
		}

		let envelope: Envelope;
		try {
			envelope = await response.json() as Envelope;
		} catch {
			throw new IslandCallError('error', ['The server did not answer the call.']);
		}
		this.invalidate(envelope);
		if (!envelope.ok) {
			throw new IslandCallError(envelope.error?.kind ?? 'error', envelope.error?.messages ?? []);
		}
		return envelope.value;
	}

	private invalidate(envelope: Envelope): void {
		if (envelope.invalidate && envelope.invalidate.length > 0) {
			for (const listener of this.invalidationListeners) {
				listener(envelope.invalidate);
			}
		}
	}

	private receive(value: string): void {
		let envelope: Envelope;
		try {
			envelope = JSON.parse(value) as Envelope;
		} catch (e) {
			console.error('Wicket Islands: cannot read the answer of a call', e);
			return;
		}

		this.sweep();
		this.invalidate(envelope);

		if (envelope.call === undefined) {
			return;
		}
		const pending = this.pending.get(envelope.call);
		if (!pending) {
			return;
		}
		this.pending.delete(envelope.call);
		if (envelope.ok) {
			pending.resolve(envelope.value);
		} else {
			pending.reject(new IslandCallError(envelope.error?.kind ?? 'error',
				envelope.error?.messages ?? []));
		}
	}

	private fail(id: string, kind: IslandCallErrorKind, message: string): void {
		const pending = this.pending.get(id);
		if (pending) {
			this.pending.delete(id);
			pending.reject(new IslandCallError(kind, [message]));
		}
	}
}

function findRoot(host: HTMLElement): HTMLElement {
	const root = host.querySelector<HTMLElement>(SELECTOR_ROOT);
	if (!root) {
		throw new Error(`Wicket Islands: island host ${host.id} has no root element.`);
	}
	return root;
}

function readState(host: HTMLElement): IslandState {
	const script = host.querySelector(SELECTOR_STATE);
	if (!script?.textContent) {
		throw new Error(`Wicket Islands: island host ${host.id} has no state.`);
	}
	return JSON.parse(script.textContent) as IslandState;
}

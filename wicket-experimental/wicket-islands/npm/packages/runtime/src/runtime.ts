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
const HANDLE_HEADER = 'X-Wicket-Islands-Handle';

interface IslandState {
	props: unknown;
	calls: string[];
	ajax?: WicketAjaxAttributes;
	pageless?: Record<string, PagelessCall>;
	form?: FormState;
}

/**
 * A page-less endpoint as the island state names it: its URL, and the handle standing for the
 * context the server keeps for the island's calls.
 */
interface PagelessCall {
	url: string;
	handle?: string;
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
	form?: { key: string; state: FormState };
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
					error.kind === 'network',
				refetchOnWindowFocus: query => !isPageBound(query.queryKey)
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
			try {
				this.attach(host as HTMLElement);
			} catch (e) {
				console.error(`Wicket Islands: cannot mount island host ${host.id}`, e);
			}
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
		try {
			island.instance = module.mount(island.root, this.createContext(island));
		} catch (e) {
			this.mounted.delete(key);
			island.abort.abort();
			throw e;
		}
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
			queryKey: (name, payload) => {
				const key: unknown[] = [name, this.queryScope(island, name)];
				return payload === undefined ? key : [...key, payload];
			},
			store: name => this.stores.get(name),
			call: <R>(name: string, payload?: unknown) => this.call(island, name, payload) as Promise<R>
		};
	}

	/**
	 * Who shares the cached results of a call: the island itself for a page-bound call, whose
	 * answer can depend on the island; every island with the same handle, or without one the same
	 * endpoint, for a page-less call.
	 */
	private queryScope(island: MountedIsland, name: string): string {
		const pageless = island.state.pageless?.[name];
		if (!pageless) {
			return `island:${island.key}`;
		}
		return pageless.handle ? `handle:${pageless.handle}` : `endpoint:${pageless.url}`;
	}

	private call(island: MountedIsland, name: string, payload: unknown): Promise<unknown> {
		if (island.abort.signal.aborted) {
			return Promise.reject(unmounted());
		}

		const pageless = island.state.pageless?.[name];
		if (pageless) {
			return this.callPageless(pageless, payload, island.abort.signal);
		}
		const ajax = island.state.ajax;
		if (!ajax) {
			return Promise.reject(new IslandCallError('invalid', [`Unknown call '${name}'.`]));
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
			this.api!.Ajax.ajax({
				...ajax,
				ep: parameters,
				fh: [(_attributes: unknown, xhr: { status?: number } | null) => this.fail(id,
					...(xhr ? failureOf(xhr.status) : unreadable()))],
				dh: [() => this.fail(id, 'error', 'The server did not answer the call.')]
			});
		});
	}

	private async callPageless(pageless: PagelessCall, payload: unknown,
		signal: AbortSignal): Promise<unknown> {
		const headers: Record<string, string> = {
			'Content-Type': 'application/json',
			[PAGELESS_HEADER]: '1'
		};
		if (pageless.handle) {
			headers[HANDLE_HEADER] = pageless.handle;
		}
		let response: Response;
		try {
			response = await fetch(pageless.url, {
				method: 'POST',
				credentials: 'same-origin',
				headers,
				body: payload === undefined ? '' : JSON.stringify(payload),
				signal
			});
		} catch {
			throw signal.aborted ? unmounted() : new IslandCallError('network',
				['The call did not complete.']);
		}
		if (!response.ok) {
			const [kind, message] = failureOf(response.status);
			throw new IslandCallError(kind, [message]);
		}

		let envelope: Envelope;
		try {
			envelope = await response.json() as Envelope;
		} catch {
			throw signal.aborted ? unmounted()
				: new IslandCallError('error', ['The server did not answer the call.']);
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
		if (envelope.form) {
			this.applyFormState(envelope.form.key, envelope.form.state);
		}

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

	private applyFormState(key: string, state: FormState): void {
		const island = this.mounted.get(key);
		if (island?.form) {
			island.state = { ...island.state, form: state };
			island.form.refreshState();
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

function unmounted(): IslandCallError {
	return new IslandCallError('error', ['The island is no longer mounted.']);
}

/**
 * Why a request failed, from the HTTP status of its response; none, or 0, when no response came.
 */
function failureOf(status: number | undefined): [IslandCallErrorKind, string] {
	if (status === 403) {
		return ['forbidden', 'The call is not allowed.'];
	}
	if (status) {
		return ['error', `The server answered ${status}.`];
	}
	return ['network', 'The call did not complete.'];
}

function unreadable(): [IslandCallErrorKind, string] {
	return ['error', 'The answer of the server could not be read.'];
}

function isPageBound(queryKey: readonly unknown[]): boolean {
	return typeof queryKey[1] === 'string' && queryKey[1].startsWith('island:');
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

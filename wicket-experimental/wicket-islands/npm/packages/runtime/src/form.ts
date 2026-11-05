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

const SELECTOR_VALUE = ':scope > input[data-wicket-island-value]';

/**
 * What the server says about a form island.
 */
export interface FormState {
	readonly required: boolean;
	readonly enabled: boolean;
	readonly label: string | null;

	/**
	 * The feedback messages of the form island, e.g. validation errors.
	 */
	readonly errors: readonly string[];
}

/**
 * The value of a form island, kept in the hidden input Wicket submits with the form.
 */
export interface FormBinding<T> {
	/**
	 * The current value; the same object until it changes, as framework stores require.
	 */
	getValue(): T | undefined;

	/**
	 * Writes the value into the hidden input and fires its input and change events, so Wicket
	 * Ajax behaviors on the form island react as they would to any other form component.
	 */
	setValue(value: T | undefined): void;

	/**
	 * The current state; the same object until it changes.
	 */
	getState(): FormState;

	/**
	 * Notifies the listener of every change of the value or the state.
	 */
	subscribe(listener: () => void): () => void;
}

/**
 * The binding of a mounted form island. Its host can change when Wicket re-renders the island;
 * {@link refresh} reads the value and state from the current host.
 */
export class HostFormBinding implements FormBinding<unknown> {
	private value: unknown;

	private state: FormState;

	private readonly listeners = new Set<() => void>();

	constructor(private readonly host: () => HTMLElement, state: () => FormState) {
		this.readState = state;
		this.state = state();
		this.value = parse(this.input().value);
	}

	private readonly readState: () => FormState;

	getValue(): unknown {
		return this.value;
	}

	setValue(value: unknown): void {
		const input = this.input();
		input.value = value === undefined ? '' : JSON.stringify(value);
		this.value = value;
		this.notify();
		input.dispatchEvent(new Event('input', { bubbles: true }));
		input.dispatchEvent(new Event('change', { bubbles: true }));
	}

	getState(): FormState {
		return this.state;
	}

	subscribe(listener: () => void): () => void {
		this.listeners.add(listener);
		return () => this.listeners.delete(listener);
	}

	refresh(): void {
		this.value = parse(this.input().value);
		this.state = this.readState();
		this.notify();
	}

	private input(): HTMLInputElement {
		const input = this.host().querySelector<HTMLInputElement>(SELECTOR_VALUE);
		if (!input) {
			throw new Error(`Wicket Islands: form island ${this.host().id} has no value input.`);
		}
		return input;
	}

	private notify(): void {
		for (const listener of [...this.listeners]) {
			listener();
		}
	}
}

function parse(json: string): unknown {
	if (json === '') {
		return undefined;
	}
	try {
		return JSON.parse(json) as unknown;
	} catch {
		return undefined;
	}
}

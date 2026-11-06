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

import { readFileSync } from 'node:fs';
import path from 'node:path';

/**
 * Loads Wicket's plain JavaScript Ajax library into the test window, with a fake transport: a
 * test sees the requests the runtime makes and answers them with an <ajax-response>, which the
 * real library then processes.
 */

const WICKET_AJAX = path.resolve(process.cwd(),
	'../../../wicket-core/src/main/java/org/apache/wicket/ajax/res/js/wicket-ajax.js');

// eslint-disable-next-line @typescript-eslint/no-explicit-any
type Any = any;

export interface Request {
	attributes: Any;
}

let loaded = false;

export const requests: Request[] = [];

export function wicket(): Any {
	return (globalThis as Any).Wicket;
}

export function setUpWicket(): void {
	if (!loaded) {
		(0, eval)(readFileSync(WICKET_AJAX, 'utf8'));
		loaded = true;
	}
	wicket().Event.unsubscribe();
	wicket().Ajax.ajax = (attributes: Any) => {
		requests.push({ attributes });
	};
	requests.length = 0;
}

function run(attributes: Any, fill: (call: Any, context: Any) => void): void {
	const W = wicket();
	const call = new W.Ajax.Call();
	const context = { attrs: attributes, steps: [] as unknown[] };
	fill(call, context);
	context.steps.push(() => {
		call.done(attributes);
		return W.ChannelManager.FunctionsExecuter.DONE;
	});
	new W.ChannelManager.FunctionsExecuter(context.steps).start();
}

/**
 * Answers a request with the given children of <ajax-response>.
 */
export function respond(request: Request, content: string): void {
	run(request.attributes, (call, context) => call.loadedCallback(
		wicket().Xml.parse(`<?xml version="1.0" encoding="UTF-8"?><ajax-response>${content}</ajax-response>`),
		context));
}

/**
 * Fails a request, as when the server cannot be reached.
 */
export function failRequest(request: Request): void {
	run(request.attributes, (call, context) => call.failure(context, null, 'unreachable', 'error'));
}

export function component(id: string, markup: string): string {
	return `<component id="${id}"><![CDATA[${markup}]]></component>`;
}

export function data(value: unknown): string {
	return `<data name="wicket-islands"><![CDATA[${JSON.stringify(value)}]]></data>`;
}

/**
 * The markup of an island host, as Island renders it.
 */
export function host(id: string, type: string, key: string, props: unknown, url = `./call?${id}`,
	pageless?: Record<string, { url: string; handle?: string }>): string {
	return hostWith(id, type, key, { props, pageless }, '', url);
}

/**
 * The markup of a form island host, as FormIsland renders it.
 */
export function formHost(id: string, type: string, key: string, value: unknown,
	form: { required: boolean; enabled: boolean; label: string | null; errors: string[] }): string {
	const input = `<input type="hidden" data-wicket-island-value name="${id}" ` +
		`value="${value === undefined ? '' : JSON.stringify(value).replace(/"/g, '&quot;')}">`;
	return hostWith(id, type, key, { props: { label: 'form' }, form }, input, `./call?${id}`);
}

function hostWith(id: string, type: string, key: string, state: object, before: string,
	url: string): string {
	const json = JSON.stringify({
		...state,
		calls: ['probe'],
		ajax: { u: url, m: 'POST', c: id, ch: `wicket-island-${id}|s` }
	}).replace(/</g, '\\u003c');
	return `<div id="${id}" data-wicket-island="${type}" data-wicket-island-key="${key}">` +
		before + `<script type="application/json" data-wicket-island-state>${json}</script>` +
		'<div data-wicket-island-root></div></div>';
}

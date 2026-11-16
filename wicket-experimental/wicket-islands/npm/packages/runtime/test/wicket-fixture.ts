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

export const redirects: string[] = [];

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
	wicket().Ajax.redirect = (url: string) => {
		redirects.push(url);
	};
	requests.length = 0;
	redirects.length = 0;
}

function xhr(status: number, headers: Record<string, string> = {}): Any {
	return { readyState: 4, status, responseText: '', getResponseHeader: (name: string) => headers[name] ?? null };
}

/**
 * Runs a request as the library does once it is sent: the before send handlers, then the given
 * processing, then the complete and done handlers.
 */
function run(attributes: Any, response: Any, fill: (call: Any, context: Any) => void): void {
	const W = wicket();
	const call = new W.Ajax.Call();
	const context = { attrs: attributes, steps: [] as unknown[] };
	call._executeHandlers(attributes.bsh, attributes, response, {});
	fill(call, context);
	context.steps.push(() => {
		call._executeHandlers(attributes.coh, attributes, response, 'success');
		call.done(attributes);
		return W.ChannelManager.FunctionsExecuter.DONE;
	});
	new W.ChannelManager.FunctionsExecuter(context.steps).start();
}

/**
 * Answers a request with the given children of <ajax-response>.
 */
export function respond(request: Request, content: string): void {
	run(request.attributes, xhr(200), (call, context) => call.loadedCallback(
		wicket().Xml.parse(`<?xml version="1.0" encoding="UTF-8"?><ajax-response>${content}</ajax-response>`),
		context));
}

/**
 * Answers a request with a redirect, as Wicket does when a page-bound request ends on another
 * page; the library hands the URL to {@link redirects} instead of leaving the test page.
 */
export function redirect(request: Request, url: string): void {
	const response = xhr(200, { 'Ajax-Location': url });
	run(request.attributes, response, (call, context) => call.processAjaxResponse(
		null, 'success', response, context));
}

/**
 * Fails a request: with status 0 as when the server cannot be reached, with an HTTP status as when
 * it answers with an error, or without a response as when its answer cannot be parsed.
 */
export function failRequest(request: Request, status: number | null = 0): void {
	const response = status === null ? null : xhr(status);
	run(request.attributes, response, (call, context) => call.failure(context, response, 'failed',
		'error'));
}

/**
 * Ends a request a precondition stopped: the library calls the done handlers without sending it.
 */
export function stop(request: Request): void {
	new (wicket().Ajax.Call)().done(request.attributes);
}

export function component(id: string, markup: string): string {
	return `<component id="${id}"><![CDATA[${markup}]]></component>`;
}

export function data(value: unknown): string {
	return `<data name="wicket-islands"><![CDATA[${JSON.stringify(value)}]]></data>`;
}

/**
 * The markup of an island host, as Island renders it; without Ajax attributes when the URL is
 * {@code null}, as for an island without page-bound calls.
 */
export function host(id: string, type: string, key: string, props: unknown,
	url: string | null = `./call?${id}`,
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
	url: string | null): string {
	const json = JSON.stringify({
		...state,
		calls: url === null ? [] : ['probe'],
		ajax: url === null ? undefined : { u: url, m: 'POST', ch: `wicket-island-${id}|s` }
	}).replace(/</g, '\\u003c');
	return `<div id="${id}" data-wicket-island="${type}" data-wicket-island-key="${key}">` +
		before + `<script type="application/json" data-wicket-island-state>${json}</script>` +
		'<div data-wicket-island-root></div></div>';
}

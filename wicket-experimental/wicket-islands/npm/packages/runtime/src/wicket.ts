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
 * The part of Wicket's client-side API the runtime uses. Both Ajax libraries,
 * wicket-ajax-jquery.js and wicket-ajax.js, provide it.
 */
export interface WicketApi {
	Event: {
		subscribe(topic: string, subscriber: (event: unknown, ...args: never[]) => void): void;
	};
	Ajax: {
		ajax(attributes: WicketAjaxAttributes): void;
	};
}

/**
 * Wicket's Ajax request attributes, as rendered by the island's endpoint behavior.
 */
export interface WicketAjaxAttributes {
	u: string;
	ep?: Record<string, string>;
	fh?: unknown[];
	dh?: unknown[];
	[name: string]: unknown;
}

export const TOPIC_DOM_NODE_ADDED = '/dom/node/added';

export const TOPIC_AJAX_CALL_DATA = '/ajax/call/data';

export const TOPIC_AJAX_CALL_DONE = '/ajax/call/done';

export function wicket(): WicketApi {
	const api = (globalThis as { Wicket?: WicketApi }).Wicket;
	if (!api?.Event || !api.Ajax) {
		throw new Error('Wicket Islands needs Wicket\'s Ajax library on the page.');
	}
	return api;
}

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

import { describe, expect, it, vi } from 'vitest';
import { IslandCallError, type IslandContext } from 'wicket-islands';
import { createQueryClient, queryClientOf } from '../src/query';

type Listener = (names: readonly string[]) => void;

function subscription(): { onInvalidate: IslandContext<unknown>['onInvalidate']; send: Listener } {
	const listeners = new Set<Listener>();
	return {
		onInvalidate: listener => {
			listeners.add(listener);
			return () => listeners.delete(listener);
		},
		send: names => listeners.forEach(listener => listener(names))
	};
}

function contextWith(onInvalidate: IslandContext<unknown>['onInvalidate']): IslandContext<unknown> {
	return { onInvalidate } as IslandContext<unknown>;
}

describe('the query client of a page', () => {
	it('is one per page, told apart by the invalidation subscription', () => {
		const page = subscription();
		const other = subscription();

		const client = queryClientOf(contextWith(page.onInvalidate));

		expect(queryClientOf(contextWith(page.onInvalidate))).toBe(client);
		expect(queryClientOf(contextWith(other.onInvalidate))).not.toBe(client);
	});

	it('invalidates the queries under the names the server marks as stale', () => {
		const page = subscription();
		const client = createQueryClient(page.onInvalidate);
		client.setQueryData(['scores', 'island:k1', { size: 'SMALL' }], [1]);
		client.setQueryData(['other', 'island:k1'], [2]);
		const queries = client.getQueryCache();

		page.send(['scores']);

		expect(queries.find({ queryKey: ['scores', 'island:k1', { size: 'SMALL' }] })?.state.isInvalidated)
			.toBe(true);
		expect(queries.find({ queryKey: ['other', 'island:k1'] })?.state.isInvalidated).toBe(false);
	});

	it('does not refetch page-bound queries when the window gets the focus again', () => {
		const options = createQueryClient(subscription().onInvalidate).getDefaultOptions().queries!;
		const refetch = options.refetchOnWindowFocus as unknown as
			(query: { queryKey: unknown[] }) => boolean;

		expect(refetch({ queryKey: ['scores', 'island:k1'] })).toBe(false);
		expect(refetch({ queryKey: ['scores', 'endpoint:./scores'] })).toBe(true);
	});

	it('retries a query only after a network failure', () => {
		const retry = createQueryClient(subscription().onInvalidate).getDefaultOptions().queries!
			.retry as (failures: number, error: unknown) => boolean;

		expect(retry(0, new IslandCallError('network', []))).toBe(true);
		expect(retry(0, new IslandCallError('error', []))).toBe(false);
		expect(retry(2, new IslandCallError('network', []))).toBe(false);
	});

	it('retries after a network failure reported by the runtime of another build', async () => {
		const retry = createQueryClient(subscription().onInvalidate).getDefaultOptions().queries!
			.retry as (failures: number, error: unknown) => boolean;
		vi.resetModules();
		const other = await import('wicket-islands');
		expect(other.IslandCallError).not.toBe(IslandCallError);

		expect(retry(0, new other.IslandCallError('network', []))).toBe(true);
	});
});

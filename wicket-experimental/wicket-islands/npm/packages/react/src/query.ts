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

import { QueryClient } from '@tanstack/react-query';
import { IslandCallError, type IslandContext } from 'wicket-islands';

type Subscription = IslandContext<unknown>['onInvalidate'];

const clients = new WeakMap<Subscription, QueryClient>();

/**
 * The query client of the page the island belongs to, shared by every React island on it. The
 * runtime hands every island of a page the same invalidation subscription, so it identifies the
 * page.
 */
export function queryClientOf(context: IslandContext<unknown>): QueryClient {
	let client = clients.get(context.onInvalidate);
	if (!client) {
		client = createQueryClient(context.onInvalidate);
		clients.set(context.onInvalidate, client);
	}
	return client;
}

/**
 * A query client that refetches what the server marks as stale, retries only network failures,
 * and refetches page-less data, but not the island's own, when the window gets the focus again.
 */
export function createQueryClient(onInvalidate: Subscription): QueryClient {
	const client = new QueryClient({
		defaultOptions: {
			queries: {
				retry: (failures, error) => failures < 2 && error instanceof IslandCallError &&
					error.kind === 'network',
				refetchOnWindowFocus: query => !isPageBound(query.queryKey)
			}
		}
	});
	onInvalidate(names => {
		for (const name of names) {
			void client.invalidateQueries({ queryKey: [name] });
		}
	});
	return client;
}

function isPageBound(queryKey: readonly unknown[]): boolean {
	return typeof queryKey[1] === 'string' && queryKey[1].startsWith('island:');
}

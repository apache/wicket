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

import { Group, Select, TextInput } from '@mantine/core';
import { useState } from 'react';
import { useIslandQuery, useStore } from 'wicket-islands-react';
import BoardTheme from './BoardTheme';
import { FILTER_STORE, type BoardFilter } from './shared';

interface Member {
	id: string;
	name: string;
}

const NO_FILTER: BoardFilter = { assigneeId: null, text: '' };

/**
 * The filter above the board. It writes the client store the board reads, so filtering never
 * involves the server; only the assignee suggestions come from the page-less members endpoint.
 */
export default function BoardFilterBar() {
	const [stored, setFilter] = useStore<BoardFilter>(FILTER_STORE);
	const filter = stored ?? NO_FILTER;
	const [search, setSearch] = useState('');
	const members = useIslandQuery<Member[]>('members', { prefix: search },
		{ placeholderData: previous => previous });

	return (
		<BoardTheme>
			<Group className="board-filter" align="flex-end" mb="sm">
				<Select label="Assignee" placeholder="Anyone" searchable clearable
					data={(members.data ?? []).map(member => ({ value: member.id, label: member.name }))}
					value={filter.assigneeId} onChange={assigneeId => setFilter({ ...filter, assigneeId })}
					searchValue={search} onSearchChange={setSearch}
					nothingFoundMessage={members.isFetching ? 'Searching…' : 'Nobody found'}
					comboboxProps={{ withinPortal: false }} />
				<TextInput label="Title or label" placeholder="Anything" value={filter.text}
					onChange={event => setFilter({ ...filter, text: event.currentTarget.value })} />
			</Group>
		</BoardTheme>
	);
}

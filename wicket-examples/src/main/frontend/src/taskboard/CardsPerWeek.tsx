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

import { BarChart } from '@mantine/charts';
import { Text } from '@mantine/core';
import { useIslandQuery } from 'wicket-islands-react';
import BoardTheme from './BoardTheme';
import '@mantine/charts/styles.css';

interface Week {
	week: string;
	cards: number;
}

/**
 * The cards completed per week, from the page-less throughput endpoint. A move on the board marks
 * "throughput" as stale, so the chart fetches it again without the board knowing about it.
 */
export default function CardsPerWeek() {
	const weeks = useIslandQuery<Week[]>('throughput', { weeks: 8 });
	const thisWeek = weeks.data?.at(-1);

	if (weeks.isError) {
		return <BoardTheme><Text c="red" size="sm">{weeks.error.messages.join(' ')}</Text></BoardTheme>;
	}
	return (
		<BoardTheme>
			<div className="cards-per-week">
				<BarChart h={160} data={weeks.data ?? []} dataKey="week"
					series={[{ name: 'cards', label: 'Cards done', color: 'blue.6' }]}
					withTooltip={false} tickLine="none" gridAxis="y" />
				{thisWeek && (
					<Text size="xs" c="dimmed" className="this-week">
						This week: {thisWeek.cards} cards done
					</Text>
				)}
			</div>
		</BoardTheme>
	);
}

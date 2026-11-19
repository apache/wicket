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

import { useIslandQuery } from 'wicket-islands-react';
import './HighScoreList.css';

/**
 * An entry, as Score.java declares it.
 */
interface Score {
	name: string;
	colour: string;
	score: number;
}

/**
 * The props of the list, as HighScoreList.java's ListProps sends them.
 */
interface ListProps {
	size: string;
}

export default function HighScoreList({ size }: ListProps) {
	const scores = useIslandQuery<Score[]>('scores', { size });

	return (
		<div className="highscore-list">
			<h3>High scores ({size.toLowerCase()} board)</h3>
			{scores.isPending && <p>Loading…</p>}
			{scores.isError && <p className="error">{scores.error.messages.join(' ')}</p>}
			{scores.data && scores.data.length === 0 && <p>No scores yet.</p>}
			{scores.data && scores.data.length > 0 && (
				<ol>
					{scores.data.map((score, index) => (
						<li key={index}>
							<span className="dot" style={{ backgroundColor: score.colour }} />
							<span className="name">{score.name}</span>
							<span className="score">{score.score}</span>
						</li>
					))}
				</ol>
			)}
		</div>
	);
}

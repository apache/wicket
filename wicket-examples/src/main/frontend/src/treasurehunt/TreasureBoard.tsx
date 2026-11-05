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

import { useState, type CSSProperties } from 'react';
import { IslandCallError, useIsland } from 'wicket-islands-react';
import './TreasureBoard.css';

type GameStatus = 'RUNNING' | 'WON' | 'LOST';

interface Cell {
	x: number;
	y: number;
}

interface ProbeOutcome extends Cell {
	treasure: boolean;
	distance: number;
}

/**
 * The props of the board, as TreasureBoard.java's BoardProps sends them.
 */
interface BoardProps {
	width: number;
	height: number;
	status: GameStatus;
	probes: ProbeOutcome[];
	treasures: Cell[];
}

function cellKey(x: number, y: number): string {
	return `${x},${y}`;
}

/**
 * Colours a miss from hot, close to a treasure, to cold, far away.
 */
function heat(distance: number, maxDistance: number): string {
	const ratio = Math.min(distance / maxDistance, 1);
	return `hsl(${Math.round(ratio * 220)}, 75%, 55%)`;
}

export default function TreasureBoard({ width, height, status, probes, treasures }: BoardProps) {
	const { call } = useIsland<BoardProps>();
	const [pending, setPending] = useState<string | null>(null);
	const [message, setMessage] = useState<string | null>(null);
	const [expired, setExpired] = useState(false);

	const probed = new Map(probes.map(outcome => [cellKey(outcome.x, outcome.y), outcome]));
	const treasureCells = new Set(treasures.map(treasure => cellKey(treasure.x, treasure.y)));
	const running = status === 'RUNNING';
	const maxDistance = Math.ceil((width + height) / 2);

	async function probe(x: number, y: number) {
		setPending(cellKey(x, y));
		setMessage(null);
		try {
			await call('probe', { x, y });
		} catch (e) {
			setMessage(e instanceof IslandCallError ? e.messages.join(' ') : String(e));
			setExpired(e instanceof IslandCallError && e.kind === 'expired');
		} finally {
			setPending(null);
		}
	}

	const cells = [];
	for (let y = 0; y < height; y++) {
		for (let x = 0; x < width; x++) {
			const key = cellKey(x, y);
			const outcome = probed.get(key);
			let className = 'cell';
			let label = '';
			let style: CSSProperties | undefined;
			if (treasureCells.has(key)) {
				className += outcome ? ' found' : ' missed';
				label = '★';
			} else if (outcome) {
				className += ' probed';
				label = String(outcome.distance);
				style = { backgroundColor: heat(outcome.distance, maxDistance) };
			}
			if (pending === key) {
				className += ' pending';
			}
			cells.push(
				<button key={key} type="button" className={className} style={style}
					data-x={x} data-y={y} aria-label={`Cell ${x}, ${y}`}
					disabled={!running || expired || outcome !== undefined || pending !== null}
					onClick={() => void probe(x, y)}>
					{label}
				</button>);
		}
	}

	return (
		<div className="treasure-board">
			<div className="grid" style={{ gridTemplateColumns: `repeat(${width}, 2.2em)` }}>
				{cells}
			</div>
			{message && (
				<p className="message" role="alert">
					{message}
					{expired && <button type="button" onClick={() => location.reload()}>Reload</button>}
				</p>
			)}
		</div>
	);
}


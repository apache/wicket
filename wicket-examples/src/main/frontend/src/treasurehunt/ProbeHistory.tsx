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

import { useStore } from 'wicket-islands-react';
import { HOVER_STORE, type Cell, type ProbeOutcome } from './shared';
import './ProbeHistory.css';

/**
 * The props of the history, as ProbeHistory.java's HistoryProps sends them.
 */
interface HistoryProps {
	probes: ProbeOutcome[];
}

export default function ProbeHistory({ probes }: HistoryProps) {
	const [hover, setHover] = useStore<Cell | null>(HOVER_STORE);

	if (probes.length === 0) {
		return <p className="probe-history empty">No probes yet.</p>;
	}

	return (
		<ol className="probe-history" reversed>
			{probes.map(probe => {
				const highlighted = hover?.x === probe.x && hover?.y === probe.y;
				return (
					<li key={`${probe.x},${probe.y}`} className={highlighted ? 'hover' : undefined}
						data-x={probe.x} data-y={probe.y}
						onMouseEnter={() => setHover({ x: probe.x, y: probe.y })}
						onMouseLeave={() => setHover(null)}>
						{probe.x}, {probe.y}: {probe.treasure ? 'treasure!' : `distance ${probe.distance}`}
					</li>
				);
			})}
		</ol>
	);
}

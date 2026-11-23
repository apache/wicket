<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
<script lang="ts">
	import { islandStore } from 'wicket-islands-svelte';
	import { HOVER_STORE, type Cell, type ProbeOutcome } from './shared';
	import './ProbeHistory.css';

	/**
	 * The props of the history, as ProbeHistory.java's HistoryProps sends them.
	 */
	let { probes }: { probes: ProbeOutcome[] } = $props();

	const hover = islandStore<Cell | null>(HOVER_STORE);
</script>

{#if probes.length === 0}
	<p class="probe-history empty">No probes yet.</p>
{:else}
	<ol class="probe-history" reversed>
		{#each probes as probe (`${probe.x},${probe.y}`)}
			<li class={{ hover: $hover?.x === probe.x && $hover?.y === probe.y }}
				data-x={probe.x} data-y={probe.y}
				onmouseenter={() => hover.set({ x: probe.x, y: probe.y })}
				onmouseleave={() => hover.set(null)}>
				{probe.x}, {probe.y}: {probe.treasure ? 'treasure!' : `distance ${probe.distance}`}
			</li>
		{/each}
	</ol>
{/if}

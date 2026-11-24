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
<!--
  @component
  The poll in Svelte. Poll.tsx is the same component in React; both render the same markup.
-->
<script lang="ts">
	import { getIsland, islandStore } from 'wicket-islands-svelte';
	import { describe, HIGHLIGHT_STORE, leaderOf, MAX_LABEL_LENGTH, percent,
		type PollProps } from './shared';
	import './Poll.css';

	let { question, options, total }: PollProps = $props();

	const { call } = getIsland();
	const highlight = islandStore<number | null>(HIGHLIGHT_STORE);
	let draft = $state('');
	let cast = $state(0);
	let busy = $state(false);
	let message = $state<string | null>(null);
	const leader = $derived(leaderOf(options));

	async function send(name: string, payload: unknown): Promise<boolean> {
		busy = true;
		message = null;
		try {
			await call(name, payload);
			return true;
		} catch (e) {
			message = describe(e);
			return false;
		} finally {
			busy = false;
		}
	}

	async function vote(id: number) {
		if (await send('vote', { id })) {
			cast++;
		}
	}

	async function add(event: SubmitEvent) {
		event.preventDefault();
		if (await send('add', { label: draft })) {
			draft = '';
		}
	}
</script>

<div class="poll" data-framework="svelte">
	<p class="poll-framework">Rendered by Svelte</p>
	<h4 class="poll-question">{question}</h4>
	<ul class="poll-options">
		{#each options as option (option.id)}
			<li data-id={option.id}
				class={['poll-option', { leader: option.id === leader },
					{ highlight: option.id === $highlight }]}
				onmouseenter={() => highlight.set(option.id)}
				onmouseleave={() => highlight.set(null)}>
				<button type="button" class="poll-vote" disabled={busy}
					aria-label="Vote for {option.label}"
					onclick={() => void vote(option.id)}>+1</button>
				<span class="poll-label">{option.label}</span>
				<span class="poll-bar">
					<span class="poll-fill" style:width="{percent(option.votes, total)}%"></span>
				</span>
				<span class="poll-count">{option.votes}</span>
			</li>
		{/each}
	</ul>
	<form class="poll-add" onsubmit={event => void add(event)}>
		<input type="text" bind:value={draft} maxlength={MAX_LABEL_LENGTH}
			placeholder="Another option" aria-label="Another option" />
		<button type="submit" disabled={busy}>Add</button>
	</form>
	{#if message}
		<p class="poll-message" role="alert">{message}</p>
	{/if}
	<p class="poll-footer">Votes in total: {total}. Cast here: {cast}.</p>
</div>

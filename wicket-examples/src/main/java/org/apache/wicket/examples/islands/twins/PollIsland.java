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
package org.apache.wicket.examples.islands.twins;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.islands.Island;
import org.apache.wicket.islands.IslandCall;
import org.apache.wicket.model.IModel;

/**
 * The poll as an island. The class does not know which framework renders it: the page places it
 * twice, once with the type of the React module and once with the type of the Svelte one. Both
 * get the same props and make the same calls.
 */
public class PollIsland extends Island<PollIsland.PollProps>
{
	private static final long serialVersionUID = 1L;

	/**
	 * An option as the browser sees it.
	 */
	public record OptionProps(int id, String label, int votes)
	{
	}

	/**
	 * The props of the island.
	 */
	public record PollProps(String question, List<OptionProps> options, int total)
	{
		static PollProps of(Poll poll)
		{
			return new PollProps(poll.getQuestion(),
				poll.getOptions()
					.stream()
					.map(option -> new OptionProps(option.getId(), option.getLabel(),
						option.getVotes()))
					.toList(),
				poll.getTotal());
		}
	}

	/**
	 * The payload of the <code>vote</code> call.
	 */
	public record Vote(int id)
	{
	}

	/**
	 * The payload of the <code>add</code> call.
	 */
	public record NewOption(String label)
	{
	}

	/**
	 * @param id
	 *            the component id
	 * @param type
	 *            the island type naming the module that renders the poll
	 * @param poll
	 *            the poll
	 */
	public PollIsland(String id, String type, IModel<Poll> poll)
	{
		super(id, type, poll.map(PollProps::of));

		onCall("vote", Vote.class,
			(vote, call) -> change(call, () -> poll.getObject().vote(vote.id())));
		onCall("add", NewOption.class,
			(option, call) -> change(call, () -> poll.getObject().add(option.label())));
	}

	private void change(IslandCall call, Runnable change)
	{
		try
		{
			change.run();
		}
		catch (IllegalPollChangeException e)
		{
			throw call.reject(e.getMessage());
		}
		onPollChanged(call.getTarget());
	}

	/**
	 * Called after a call of this island changed the poll.
	 *
	 * @param target
	 *            the target of the call
	 */
	protected void onPollChanged(AjaxRequestTarget target)
	{
		target.add(this);
	}
}

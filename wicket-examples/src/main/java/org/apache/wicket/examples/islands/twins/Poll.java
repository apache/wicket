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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.apache.wicket.util.io.IClusterable;

/**
 * A poll: a question, its options and their votes. Each page has its own.
 */
public class Poll implements IClusterable
{
	private static final long serialVersionUID = 1L;

	static final int MAX_OPTIONS = 6;

	static final int MAX_LABEL_LENGTH = 30;

	/**
	 * An option of the poll.
	 */
	public static class Option implements IClusterable
	{
		private static final long serialVersionUID = 1L;

		private final int id;

		private final String label;

		private int votes;

		Option(int id, String label)
		{
			this.id = id;
			this.label = label;
		}

		public int getId()
		{
			return id;
		}

		public String getLabel()
		{
			return label;
		}

		public int getVotes()
		{
			return votes;
		}
	}

	private final String question;

	private final List<Option> options = new ArrayList<>();

	private int nextId = 1;

	public Poll(String question)
	{
		this.question = question;
	}

	/**
	 * @return the poll every page starts with
	 */
	public static Poll start()
	{
		Poll poll = new Poll("Which framework renders your next island?");
		poll.add("React");
		poll.add("Svelte");
		poll.add("Plain Wicket");
		return poll;
	}

	public String getQuestion()
	{
		return question;
	}

	public List<Option> getOptions()
	{
		return List.copyOf(options);
	}

	public int getTotal()
	{
		return options.stream().mapToInt(Option::getVotes).sum();
	}

	/**
	 * @return the option with the most votes, empty while no option has a vote or two share the
	 *         lead
	 */
	public Optional<Option> getLeader()
	{
		List<Option> ranked = options.stream()
			.sorted(Comparator.comparingInt(Option::getVotes).reversed())
			.toList();
		if (ranked.isEmpty() || ranked.get(0).getVotes() == 0 ||
			(ranked.size() > 1 && ranked.get(1).getVotes() == ranked.get(0).getVotes()))
		{
			return Optional.empty();
		}
		return Optional.of(ranked.get(0));
	}

	/**
	 * Adds a vote to an option.
	 *
	 * @param id
	 *            the id of the option
	 * @throws IllegalPollChangeException
	 *             when the poll has no option with that id
	 */
	public void vote(int id)
	{
		options.stream()
			.filter(option -> option.id == id)
			.findFirst()
			.orElseThrow(() -> new IllegalPollChangeException("This option no longer exists."))
			.votes++;
	}

	/**
	 * Adds an option.
	 *
	 * @param label
	 *            the label of the new option, stripped of surrounding white space
	 * @throws IllegalPollChangeException
	 *             when the label is empty or too long, the poll has it already or is full
	 */
	public void add(String label)
	{
		String stripped = label == null ? "" : label.strip();
		if (stripped.isEmpty())
		{
			throw new IllegalPollChangeException("Enter the option to add.");
		}
		if (stripped.length() > MAX_LABEL_LENGTH)
		{
			throw new IllegalPollChangeException(
				"An option has at most " + MAX_LABEL_LENGTH + " characters.");
		}
		if (options.size() >= MAX_OPTIONS)
		{
			throw new IllegalPollChangeException(
				"The poll has room for " + MAX_OPTIONS + " options.");
		}
		String key = stripped.toLowerCase(Locale.ROOT);
		if (options.stream().anyMatch(option -> option.label.toLowerCase(Locale.ROOT).equals(key)))
		{
			throw new IllegalPollChangeException("'" + stripped + "' is an option already.");
		}
		options.add(new Option(nextId++, stripped));
	}

	/**
	 * Takes every vote back, and keeps the options.
	 */
	public void reset()
	{
		options.forEach(option -> option.votes = 0);
	}
}

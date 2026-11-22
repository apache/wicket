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
package org.apache.wicket.examples.islands.taskboard;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.islands.IPagelessEndpoint;
import org.apache.wicket.islands.PagelessCall;

/**
 * The number of cards completed per week, for the chart. Runs without the page lock; the board
 * comes from the call's handle. Moving a card invalidates it, so the chart fetches it again.
 */
public class ThroughputEndpoint
	implements IPagelessEndpoint<ThroughputEndpoint.ThroughputQuery, List<ThroughputEndpoint.Week>>
{
	/**
	 * @param weeks
	 *            the number of weeks, 1 to 26, ending with the current one
	 */
	public record ThroughputQuery(int weeks)
	{
	}

	/**
	 * @param week
	 *            the Monday the week starts on
	 * @param cards
	 *            the number of cards completed in it
	 */
	public record Week(String week, int cards)
	{
	}

	private static final DateTimeFormatter LABEL = DateTimeFormatter.ofPattern("MM-dd");

	private final BoardRepository repository;

	public ThroughputEndpoint(BoardRepository repository)
	{
		this.repository = repository;
	}

	@Override
	public List<Week> handle(ThroughputQuery query, PagelessCall call)
	{
		if (query.weeks() < 1 || query.weeks() > 26)
		{
			throw call.reject("Ask for 1 to 26 weeks.");
		}
		int[] counts = repository.throughput(call.getHandle(BoardRef.class).boardId(),
			query.weeks());
		LocalDate monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
		List<Week> weeks = new ArrayList<>();
		for (int i = 0; i < counts.length; i++)
		{
			weeks.add(new Week(monday.minusWeeks(counts.length - 1 - i).format(LABEL), counts[i]));
		}
		return weeks;
	}
}

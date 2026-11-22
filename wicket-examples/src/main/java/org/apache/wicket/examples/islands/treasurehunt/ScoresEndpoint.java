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
package org.apache.wicket.examples.islands.treasurehunt;

import java.util.List;

import org.apache.wicket.islands.IPagelessEndpoint;
import org.apache.wicket.islands.PagelessCall;

/**
 * The page-less endpoint behind the high-score list. It reads application-wide data, so it needs
 * no page: its calls run in parallel with the page's, without its lock.
 */
public class ScoresEndpoint implements IPagelessEndpoint<ScoresEndpoint.ScoreQuery, List<Score>>
{
	/**
	 * @param size
	 *            the board size to list the scores of
	 */
	public record ScoreQuery(BoardSize size)
	{
	}

	private final HighScores highScores;

	public ScoresEndpoint(HighScores highScores)
	{
		this.highScores = highScores;
	}

	@Override
	public List<Score> handle(ScoreQuery query, PagelessCall call)
	{
		if (query.size() == null)
		{
			throw call.reject("Name a board size.");
		}
		return highScores.top(query.size());
	}
}

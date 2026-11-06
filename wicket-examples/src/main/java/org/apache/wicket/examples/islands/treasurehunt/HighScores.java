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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * The best scores per board size, shared by every session of the application.
 */
public class HighScores
{
	private static final int KEPT = 10;

	private final Map<BoardSize, List<Score>> scores = new EnumMap<>(BoardSize.class);

	public synchronized void add(BoardSize size, Score score)
	{
		List<Score> list = scores.computeIfAbsent(size, unused -> new ArrayList<>());
		list.add(score);
		list.sort(Comparator.comparingInt(Score::score).reversed());
		if (list.size() > KEPT)
		{
			list.subList(KEPT, list.size()).clear();
		}
	}

	public synchronized List<Score> top(BoardSize size)
	{
		return List.copyOf(scores.getOrDefault(size, List.of()));
	}
}

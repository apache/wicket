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

import org.apache.wicket.islands.Island;
import org.apache.wicket.model.IModel;

/**
 * The high-score list, rendered by the React component <code>HighScoreList.tsx</code>. It fetches
 * the scores itself through the page-less <code>scores</code> endpoint, and fetches them again
 * when the server marks them as stale.
 */
public class HighScoreList extends Island<HighScoreList.ListProps>
{
	private static final long serialVersionUID = 1L;

	/**
	 * @param size
	 *            the board size to list the scores of
	 */
	public record ListProps(BoardSize size)
	{
	}

	public HighScoreList(String id, IModel<BoardSize> size)
	{
		super(id, "highscore-list", size.map(ListProps::new));
		usePageless("scores");
	}
}

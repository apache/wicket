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

/**
 * The props of the board: what the browser may know about a game. The hidden treasures are not
 * part of it until the game is over, because props are written into the page.
 *
 * @param width
 *            the number of columns
 * @param height
 *            the number of rows
 * @param status
 *            the state of the game
 * @param probes
 *            the probes so far
 * @param treasures
 *            the treasures found, or all of them once the game is over
 */
public record BoardProps(int width, int height, GameStatus status, List<ProbeOutcome> probes,
	List<Cell> treasures)
{
	static BoardProps of(Game game)
	{
		return new BoardProps(game.getSize().getWidth(), game.getSize().getHeight(),
			game.getStatus(), List.copyOf(game.getProbes()), game.getVisibleTreasures());
	}
}

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
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.wicket.util.io.IClusterable;

/**
 * A game of Treasure Hunt: treasures hidden on a board, found by probing cells. A probe that
 * misses tells the distance to the nearest treasure still hidden.
 */
public class Game implements IClusterable
{
	private static final long serialVersionUID = 1L;

	private final BoardSize size;

	private final Set<Cell> treasures;

	private final Set<Cell> found = new LinkedHashSet<>();

	private final List<ProbeOutcome> probes = new ArrayList<>();

	Game(BoardSize size, Set<Cell> treasures)
	{
		this.size = size;
		this.treasures = Set.copyOf(treasures);
	}

	/**
	 * Starts a game with randomly hidden treasures.
	 */
	public static Game start(BoardSize size)
	{
		Random random = new Random();
		Set<Cell> treasures = new LinkedHashSet<>();
		while (treasures.size() < size.getTreasures())
		{
			treasures.add(new Cell(random.nextInt(size.getWidth()), random.nextInt(size.getHeight())));
		}
		return new Game(size, treasures);
	}

	public BoardSize getSize()
	{
		return size;
	}

	/**
	 * Probes a cell.
	 *
	 * @throws IllegalProbeException
	 *             if the game is over, or the cell does not exist or was probed already
	 */
	public ProbeOutcome probe(int x, int y)
	{
		if (getStatus() != GameStatus.RUNNING)
		{
			throw new IllegalProbeException("The game is over.");
		}
		if (x < 0 || y < 0 || x >= size.getWidth() || y >= size.getHeight())
		{
			throw new IllegalProbeException("There is no cell " + x + ", " + y + ".");
		}
		Cell cell = new Cell(x, y);
		if (probes.stream().anyMatch(probe -> probe.x() == x && probe.y() == y))
		{
			throw new IllegalProbeException("That cell was probed already.");
		}

		boolean treasure = treasures.contains(cell);
		if (treasure)
		{
			found.add(cell);
		}
		ProbeOutcome outcome = new ProbeOutcome(x, y, treasure, treasure ? 0 : distanceToHidden(cell));
		probes.add(outcome);
		return outcome;
	}

	private int distanceToHidden(Cell cell)
	{
		return treasures.stream()
			.filter(treasure -> !found.contains(treasure))
			.mapToInt(cell::distanceTo)
			.min()
			.orElse(0);
	}

	public GameStatus getStatus()
	{
		if (found.size() == treasures.size())
		{
			return GameStatus.WON;
		}
		return getProbesLeft() == 0 ? GameStatus.LOST : GameStatus.RUNNING;
	}

	public int getProbesLeft()
	{
		return size.getProbes() - probes.size();
	}

	public int getTreasuresLeft()
	{
		return treasures.size() - found.size();
	}

	/**
	 * @return 100 for every treasure found, and 10 for every probe left once all are found
	 */
	public int getScore()
	{
		int score = 100 * found.size();
		return getStatus() == GameStatus.WON ? score + 10 * getProbesLeft() : score;
	}

	public List<ProbeOutcome> getProbes()
	{
		return Collections.unmodifiableList(probes);
	}

	/**
	 * @return the treasures found while the game runs, all of them once it is over
	 */
	public List<Cell> getVisibleTreasures()
	{
		return List.copyOf(getStatus() == GameStatus.RUNNING ? found : treasures);
	}
}

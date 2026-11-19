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

/**
 * The sizes of the board.
 */
public enum BoardSize
{
	/** 8 by 8, 3 treasures, 16 probes. */
	SMALL(8, 8, 3, 16),

	/** 10 by 10, 3 treasures, 20 probes. */
	NORMAL(10, 10, 3, 20),

	/** 14 by 14, 5 treasures, 30 probes. */
	LARGE(14, 14, 5, 30);

	private final int width;

	private final int height;

	private final int treasures;

	private final int probes;

	BoardSize(int width, int height, int treasures, int probes)
	{
		this.width = width;
		this.height = height;
		this.treasures = treasures;
		this.probes = probes;
	}

	public int getWidth()
	{
		return width;
	}

	public int getHeight()
	{
		return height;
	}

	public int getTreasures()
	{
		return treasures;
	}

	public int getProbes()
	{
		return probes;
	}
}

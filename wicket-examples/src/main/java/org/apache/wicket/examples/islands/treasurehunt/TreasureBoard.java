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

import org.apache.wicket.Component;
import org.apache.wicket.islands.Island;
import org.apache.wicket.model.IModel;

/**
 * The board, rendered by the React component <code>TreasureBoard.tsx</code>. Its
 * <code>probe</code> call probes a cell of the game and sends the board and the given components
 * back with the outcome.
 */
public class TreasureBoard extends Island<BoardProps>
{
	private static final long serialVersionUID = 1L;

	public TreasureBoard(String id, IModel<Game> game, Component... dependents)
	{
		super(id, "treasure-board", game.map(BoardProps::of));

		onCall("probe", Probe.class, ProbeOutcome.class, (probe, call) -> {
			try
			{
				ProbeOutcome outcome = game.getObject().probe(probe.x(), probe.y());
				call.getTarget().add(this);
				call.getTarget().add(dependents);
				return outcome;
			}
			catch (IllegalProbeException e)
			{
				throw call.reject(e.getMessage());
			}
		});
	}
}

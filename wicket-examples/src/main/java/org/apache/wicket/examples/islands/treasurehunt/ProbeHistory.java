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

import org.apache.wicket.islands.Island;
import org.apache.wicket.model.IModel;

/**
 * The list of probes, rendered by the React component <code>ProbeHistory.tsx</code>. It has no
 * calls of its own: the board's probe call sends it new props, and it shares the highlighted cell
 * with the board through a client store, without involving the server.
 */
public class ProbeHistory extends Island<ProbeHistory.HistoryProps>
{
	private static final long serialVersionUID = 1L;

	/**
	 * @param probes
	 *            the probes, newest first
	 */
	public record HistoryProps(List<ProbeOutcome> probes)
	{
		static HistoryProps of(Game game)
		{
			return new HistoryProps(game.getProbes().reversed());
		}
	}

	public ProbeHistory(String id, IModel<Game> game)
	{
		super(id, "probe-history", game.map(HistoryProps::of));
	}
}

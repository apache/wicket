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

import java.io.Serializable;
import java.util.List;

/**
 * The name and colour a player enters for the high-score list. It is the value of the
 * <code>player-entry</code> form island, so it arrives from the browser and is validated before
 * it is used.
 *
 * @param name
 *            the player's name
 * @param colour
 *            the player's colour, one of {@link #PALETTE}
 */
public record PlayerEntry(String name, String colour) implements Serializable
{
	/**
	 * The colours a player can choose from.
	 */
	public static final List<String> PALETTE = List.of("#e6194b", "#3cb44b", "#ffe119", "#4363d8",
		"#f58231", "#911eb4");
}

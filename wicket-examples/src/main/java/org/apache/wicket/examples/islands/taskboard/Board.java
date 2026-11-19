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

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A snapshot of a board, as the repository held it at one moment.
 *
 * @param id
 *            the id
 * @param version
 *            the version, which every change increments
 * @param members
 *            the members
 * @param columns
 *            the cards of each column, in order
 */
public record Board(String id, long version, List<Member> members, Map<Column, List<Card>> columns)
	implements Serializable
{
	public Optional<Card> findCard(String cardId)
	{
		return columns.values()
			.stream()
			.flatMap(List::stream)
			.filter(card -> card.id().equals(cardId))
			.findFirst();
	}

	public Optional<Member> findMember(String memberId)
	{
		return members.stream().filter(member -> member.id().equals(memberId)).findFirst();
	}
}

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

import java.util.List;
import java.util.Locale;

import org.apache.wicket.islands.IPagelessEndpoint;
import org.apache.wicket.islands.PagelessCall;

/**
 * The members of a board whose name starts with a prefix, for the filter's autocomplete. Runs
 * without the page lock; the board comes from the call's handle.
 */
public class MembersEndpoint implements IPagelessEndpoint<MembersEndpoint.MemberQuery, List<Member>>
{
	/**
	 * @param prefix
	 *            the start of the name, any case; empty for all members
	 */
	public record MemberQuery(String prefix)
	{
	}

	private final BoardRepository repository;

	public MembersEndpoint(BoardRepository repository)
	{
		this.repository = repository;
	}

	@Override
	public List<Member> handle(MemberQuery query, PagelessCall call)
	{
		String prefix = query.prefix() == null ? ""
			: query.prefix().strip().toLowerCase(Locale.ROOT);
		return repository.get(call.getHandle(BoardRef.class).boardId())
			.members()
			.stream()
			.filter(member -> member.name().toLowerCase(Locale.ROOT).startsWith(prefix))
			.toList();
	}
}

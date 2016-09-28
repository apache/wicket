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
package org.apache.wicket.protocol.ws.api.registry;

import org.apache.wicket.util.lang.Args;

/**
 * A key based on page's id
 */
public class PageIdKey implements IKey
{
	private final Integer pageId;

	public PageIdKey(Integer pageId)
	{
		this.pageId = Args.notNull(pageId, "pageId");
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		PageIdKey pageIdKey = (PageIdKey) o;

		if (!pageId.equals(pageIdKey.pageId)) return false;

		return true;
	}

	@Override
	public int hashCode()
	{
		return pageId.hashCode();
	}
}

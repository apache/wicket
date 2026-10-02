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
package org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.dynamic;

import java.util.HashMap;

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Session;

/**
 * Keeps the {@link ColumnState}s of tables in the session, so they survive reloading the page and
 * going back to it, but not the session. This is the store tables use by default.
 *
 * @since 11.0.0
 */
public class SessionColumnStateStore implements IColumnStateStore
{
	private static final long serialVersionUID = 1L;

	/**
	 * The store.
	 */
	public static final SessionColumnStateStore INSTANCE = new SessionColumnStateStore();

	private static final MetaDataKey<HashMap<String, ColumnState>> STATES = new MetaDataKey<>()
	{
		private static final long serialVersionUID = 1L;
	};

	@Override
	public ColumnState load(String key)
	{
		HashMap<String, ColumnState> states = Session.get().getMetaData(STATES);
		return states != null ? states.get(key) : null;
	}

	@Override
	public void save(String key, ColumnState state)
	{
		Session session = Session.get();
		HashMap<String, ColumnState> states = session.getMetaData(STATES);
		HashMap<String, ColumnState> changed = states != null ? new HashMap<>(states)
			: new HashMap<>();
		changed.put(key, state);
		session.bind();
		session.setMetaData(STATES, changed);
	}
}

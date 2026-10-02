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
package org.apache.wicket.examples.repeater;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.dynamic.ColumnState;
import org.junit.jupiter.api.Test;

/**
 * Saves and loads column states with {@link FileColumnStateStore}.
 */
class FileColumnStateStoreTest
{
	@Test
	void aSavedStateIsLoadedAgain()
	{
		String key = "test:" + UUID.randomUUID();
		ColumnState state = new ColumnState(List.of("0", "2", "1"), List.of("2"),
			Map.of("0", 12.5, "1", 40d));

		assertNull(FileColumnStateStore.INSTANCE.load(key));
		FileColumnStateStore.INSTANCE.save(key, state);

		assertEquals(state, new FileColumnStateStore().load(key));
	}
}

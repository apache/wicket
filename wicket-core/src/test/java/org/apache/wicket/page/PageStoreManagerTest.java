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
package org.apache.wicket.page;

import static org.junit.Assert.assertNull;

import java.io.Serializable;

import org.apache.wicket.MockPage;
import org.apache.wicket.pageStore.IPageStore;
import org.apache.wicket.pageStore.memory.DummyPageManagerContext;
import org.junit.Test;

public class PageStoreManagerTest {

	@Test
	public void test() {

		DummyPageManagerContext context = new DummyPageManagerContext();
		
		PageStoreManager manager = new PageStoreManager("test", new NoopPageStore(), context);

		manager.touchPage(new MockPage(1));
		manager.clear();
		
		assertNull("no page after clear", manager.getPage(1));
	}

	private final class NoopPageStore implements IPageStore {
		@Override
		public void unbind(String sessionId) {
		}

		@Override
		public void storePage(String sessionId, IManageablePage page) {
		}

		@Override
		public Object restoreAfterSerialization(Serializable serializable) {
			return null;
		}

		@Override
		public void removePage(String sessionId, int pageId) {
		}

		@Override
		public Serializable prepareForSerialization(String sessionId, Serializable page) {
			return null;
		}

		@Override
		public IManageablePage getPage(String sessionId, int pageId) {
			return null;
		}

		@Override
		public void destroy() {
		}

		@Override
		public IManageablePage convertToPage(Object page) {
			return null;
		}
	}
}
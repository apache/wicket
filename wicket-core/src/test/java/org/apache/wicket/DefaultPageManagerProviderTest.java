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
package org.apache.wicket;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apache.wicket.core.util.lang.PropertyResolver;
import org.apache.wicket.page.IPageManager;
import org.apache.wicket.pageStore.AsynchronousPageStore;
import org.apache.wicket.pageStore.CachingPageStore;
import org.apache.wicket.pageStore.DiskPageStore;
import org.apache.wicket.pageStore.InSessionPageStore;
import org.apache.wicket.pageStore.RequestPageStore;
import org.apache.wicket.pageStore.SerializingPageStore;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link DefaultPageManagerProvider}.
 */
class DefaultPageManagerProviderTest extends WicketTestCase
{

	/**
	 * Test default chain of stores.
	 */
	@Test
	void chain()
	{
		IPageManager manager = new DefaultPageManagerProvider(tester.getApplication()).get();

		RequestPageStore request = (RequestPageStore)manager.getPageStore();
		CachingPageStore caching = (CachingPageStore)request.getDelegate();
		InSessionPageStore session = (InSessionPageStore)caching.getCache();
		assertNull(PropertyResolver.getValue("serializer", session));
		SerializingPageStore serializing = (SerializingPageStore)caching.getDelegate();
		AsynchronousPageStore asynchronous = (AsynchronousPageStore)serializing.getDelegate();
		DiskPageStore disk = (DiskPageStore)asynchronous.getDelegate();
		
		assertNotNull(disk);
	}
}

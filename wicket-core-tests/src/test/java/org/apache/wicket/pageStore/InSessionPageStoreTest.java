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
package org.apache.wicket.pageStore;

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.pageStore.InSessionPageStore.SessionData;

/**
 * Test for {@link InSessionPageStore}. 
 */
public class InSessionPageStoreTest extends AbstractPageStoreTest
{
	
	private static final MetaDataKey<SessionData> KEY = new MetaDataKey<SessionData>()
	{
		private static final long serialVersionUID = 1L;
	};
	
	@Override
	protected IPageStore createPageStore(int maxEntries)
	{
		return new InSessionPageStore(maxEntries) {
			
			@Override
			protected MetaDataKey<SessionData> getKey()
			{
				return KEY;
			}
		};
	}

}

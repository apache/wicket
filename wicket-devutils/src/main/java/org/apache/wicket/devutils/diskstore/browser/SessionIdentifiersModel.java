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
package org.apache.wicket.devutils.diskstore.browser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.Session;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.pageStore.DefaultPageContext;
import org.apache.wicket.pageStore.DiskPageStore;
import org.apache.wicket.pageStore.IPageContext;
import org.apache.wicket.pageStore.IPersistentPageStore;

/**
 * A model that collects the keys from the {@link DiskPageStore} folder
 */
public class SessionIdentifiersModel extends LoadableDetachableModel<List<String>>
{
	private final IModel<IPersistentPageStore> store;
	
	public SessionIdentifiersModel(IModel<IPersistentPageStore> store)
	{
		this.store = store;
	}
	
	@Override
	protected List<String> load()
	{
		IPersistentPageStore store = this.store.getObject();
		if (store == null)
		{
			return Collections.emptyList();
		}

		ArrayList<String> identifiers = new ArrayList<>(store.getContextIdentifiers());

		IPageContext context = new DefaultPageContext(Session.get());
		String current = store.getContextIdentifier(context);
		if (identifiers.contains(current) == false)
		{
			// identifiers of the store seem no to match their sessions ids,
			// thus add the default identifier so the select works properly  
			identifiers.add(current);
		}

		return identifiers;
	}
	
	@Override
	public void detach()
	{
		super.detach();
		
		store.detach();
	}
}
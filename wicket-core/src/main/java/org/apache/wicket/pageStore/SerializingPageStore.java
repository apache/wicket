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

import org.apache.wicket.page.IManageablePage;
import org.apache.wicket.serialize.ISerializer;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Classes;

/**
 * A store that serializes all pages before delegating and vice versa.
 */
public class SerializingPageStore extends DelegatingPageStore
{

	private ISerializer serializer;

	/**
	 * @param delegate
	 *            store to delegate to
	 * @param serializer
	 *            serializer to use if session gets persisted
	 */
	public SerializingPageStore(IPageStore delegate, ISerializer serializer)
	{
		super(delegate);

		this.serializer = Args.notNull(serializer, "serializer");
	}

	/**
	 * Versioning is supported, since pages are always serialized.
	 */
	@Override
	public boolean supportsVersioning()
	{
		return true;
	}

	/**
	 * Supports asynchronous add if the delegate supports it.
	 */
	@Override
	public boolean canBeAsynchronous(IPageContext context)
	{
		return getDelegate().canBeAsynchronous(context);
	}
	
	@Override
	public IManageablePage getPage(IPageContext context, int id)
	{
		IManageablePage page = getDelegate().getPage(context, id);

		if (page instanceof SerializedPage)
		{
			page = (IManageablePage)serializer.deserialize(((SerializedPage)page).getData());
		}
		return page;
	}

	@Override
	public void addPage(IPageContext context, IManageablePage page)
	{
		if (page instanceof SerializedPage == false)
		{
			page = new SerializedPage(page.getPageId(), Classes.name(page.getClass()), serializer.serialize(page));
		}
		getDelegate().addPage(context, page);
	}
}
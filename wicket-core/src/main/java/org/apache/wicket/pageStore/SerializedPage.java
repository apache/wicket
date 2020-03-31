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
import org.apache.wicket.util.lang.Args;

/**
 * A wrapper around a serialized page.
 * <p>
 * An {@link IPageStore} might use this representation of a page internally,
 * accept it in {@link IPageStore#addPage(IPageContext, IManageablePage)} or delegate it
 * to another store.
 * <p>
 * Stores might pose restrictions on what type of pages they work with, see {@link CryptingPageStore}
 * for an example.
 * 
 * @see SerializingPageStore
 */
public final class SerializedPage implements IManageablePage
{
	private final int pageId;
	
	private final String pageType;

	private final byte[] data;

	/**
	 * Create a serialized page.
	 * 
	 * @param pageId id of page
	 * @param data
	 */
	public SerializedPage(int pageId, byte[] data)
	{
		this(pageId, null, data);
	}

	/**
	 * Create a serialized page.
	 * 
	 * @param pageId id of page
	 * @param pageType type of page, might be {@code null}
	 * @param data
	 */
	public SerializedPage(int pageId, String pageType, byte[] data)
	{
		this.pageId = pageId;
		this.pageType = pageType;
		this.data = Args.notNull(data, "data");
	}

	@Override
	public boolean isPageStateless()
	{
		return false;
	}

	@Override
	public int getPageId()
	{
		return pageId;
	}


	public String getPageType()
	{
		return pageType;
	}

	public byte[] getData()
	{
		return data;
	}

	@Override
	public void detach() {
	}

	@Override
	public boolean setFreezePageId(boolean freeze)
	{
		return false;
	}
	
	@Override
	public String toString()
	{
		return "[SerializedPage id = " + pageId + ", type=" + pageType + ", size=" + data.length + "]";
	}
}

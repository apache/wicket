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

import java.io.Serializable;

import org.apache.wicket.page.IManageablePage;
import org.apache.wicket.serialize.ISerializer;
import org.apache.wicket.util.lang.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public abstract class AbstractPageStore implements IPageStore
{
	private static final Logger LOG = LoggerFactory.getLogger(AbstractPageStore.class);

	protected final IDataStore dataStore;

	/**
	 * The {@link org.apache.wicket.serialize.ISerializer} that will be used to convert pages from/to byte arrays
	 */
	protected final ISerializer pageSerializer;

	protected AbstractPageStore(final ISerializer pageSerializer, final IDataStore dataStore)
	{
		Args.notNull(pageSerializer, "pageSerializer");
		Args.notNull(dataStore, "DataStore");

		this.pageSerializer = pageSerializer;
		this.dataStore = dataStore;
	}

	@Override
	public void destroy()
	{
		dataStore.destroy();
	}

	@Override
	public Serializable prepareForSerialization(final String sessionId, final Serializable page)
	{
		if (dataStore.isReplicated())
		{
			return null;
		}

		return page;
	}

	@Override
	public Object restoreAfterSerialization(final Serializable serializable)
	{
		return serializable;
	}

	/**
	 * @param sessionId
	 *          The id of the http session
	 * @param pageId
	 *          The id of page which serialized data should be got
	 * @return page data
	 * @see org.apache.wicket.pageStore.IDataStore#getData(String, int)
	 */
	protected byte[] getPageData(final String sessionId, final int pageId)
	{
		return dataStore.getData(sessionId, pageId);
	}

	/**
	 * @param sessionId
	 *          The id of the http session
	 * @param pageId
	 *          The id of page which serialized data should be removed
	 * @see org.apache.wicket.pageStore.IDataStore#removeData(String, int)
	 */
	protected void removePageData(final String sessionId, final int pageId)
	{
		dataStore.removeData(sessionId, pageId);
	}

	/**
	 * @param sessionId
	 *          The id of the http session for which all data should be removed
	 * @see org.apache.wicket.pageStore.IDataStore#removeData(String)
	 */
	protected void removePageData(final String sessionId)
	{
		dataStore.removeData(sessionId);
	}

	/**
	 * @param sessionId
	 *          The id of the http session
	 * @param pageId
	 *          The id of the page to store
	 * @param data
	 *          The serialized view of the page
	 * @see org.apache.wicket.pageStore.IDataStore#storeData(String, int, byte[])
	 */
	protected void storePageData(final String sessionId, final int pageId, final byte[] data)
	{
		dataStore.storeData(sessionId, pageId, data);
	}

	/**
	 * Serializes the passed page to byte[]
	 *
	 * @param page
	 *          The page to serialize
	 * @return the serialized view of the passed page
	 */
	protected byte[] serializePage(final IManageablePage page)
	{
		Args.notNull(page, "page");

		byte[] data = pageSerializer.serialize(page);

		if (data == null && LOG.isWarnEnabled())
		{
			LOG.warn("Page {} cannot be serialized. See previous logs for possible reasons.", page);
		}
		return data;
	}

	/**
	 *
	 * @param data
	 *          The serialized view of the page
	 * @return page data deserialized
	 */
	protected IManageablePage deserializePage(final byte[] data)
	{
		Args.notNull(data, "data");

		return (IManageablePage)pageSerializer.deserialize(data);
	}
}

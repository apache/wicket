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

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.page.IManageablePage;
import org.apache.wicket.pageStore.crypt.DefaultCrypter;
import org.apache.wicket.pageStore.crypt.ICrypter;
import org.apache.wicket.serialize.ISerializer;

/**
 * A store that encrypts all pages before delegating and vice versa.
 * <p>
 * All pages passing through this store are restricted to be {@link SerializedPage}s. You can
 * achieve this with
 * <ul>
 * <li>a {@link SerializingPageStore} delegating to this store and</li>
 * <li>delegating to a store that does not deserialize its pages, e.g. a {@link DiskPageStore}
 * without {@link ISerializer}</li>.
 * </ul>
 */
public class CryptingPageStore extends DelegatingPageStore
{
	private static final MetaDataKey<SessionData> KEY = new MetaDataKey<SessionData>()
	{
	};

	/**
	 * @param delegate
	 *            store to delegate to
	 * @param applicationName
	 *            name of application
	 */
	public CryptingPageStore(IPageStore delegate)
	{
		super(delegate);
	}

	/**
	 * Pages are always serialized, so versioning is supported.
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
		getSessionData(context);

		return getDelegate().canBeAsynchronous(context);
	}

	private SessionData getSessionData(IPageContext context)
	{
		SessionData data = context.getSessionData(KEY);
		if (data == null)
		{
			data = context.setSessionData(KEY, new SessionData(newCrypter(context)));
		}
		return data;
	}

	/**
	 * Create a new {@link ICrypter} for the given context.
	 */
	protected ICrypter newCrypter(IPageContext context) {
		return new DefaultCrypter();
	}

	@Override
	public IManageablePage getPage(IPageContext context, int id)
	{
		IManageablePage page = super.getPage(context, id);

		if (page != null)
		{
			if (page instanceof SerializedPage == false)
			{
				throw new WicketRuntimeException("CryptingPageStore expects serialized pages");
			}
			SerializedPage serializedPage = (SerializedPage)page;

			byte[] encrypted = serializedPage.getData();
			byte[] decrypted = getSessionData(context).decrypt(encrypted);

			page = new SerializedPage(page.getPageId(), serializedPage.getPageType(), decrypted);
		}

		return page;
	}

	@Override
	public void addPage(IPageContext context, IManageablePage page)
	{
		if (page instanceof SerializedPage == false)
		{
			throw new WicketRuntimeException("CryptingPageStore works with serialized pages only");
		}

		SerializedPage serializedPage = (SerializedPage)page;

		byte[] decrypted = serializedPage.getData();
		byte[] encrypted = getSessionData(context).encrypt(decrypted);

		page = new SerializedPage(page.getPageId(), serializedPage.getPageType(), encrypted);

		super.addPage(context, page);
	}

	private static class SessionData implements Serializable
	{

		private ICrypter cypter;

		public SessionData(ICrypter crypter)
		{
			this.cypter= crypter;
		}

		public byte[] encrypt(byte[] decrypted)
		{
			return cypter.encrypt(decrypted);
		}

		public byte[] decrypt(byte[] encrypted)
		{
			return cypter.decrypt(encrypted);
		}
	}
}
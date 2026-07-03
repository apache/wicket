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
import java.nio.ByteBuffer;
import java.security.SecureRandom;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.wicket.Application;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.core.util.crypt.ICrypt;
import org.apache.wicket.core.util.crypt.SchemeCrypt;
import org.apache.wicket.page.IManageablePage;
import org.apache.wicket.settings.SecuritySettings;
import org.apache.wicket.util.crypt.CipherUtils;
import org.apache.wicket.util.lang.Args;

/**
 * A store that encrypts all pages before delegating and vice versa.
 * <p>
 * All pages passing through this store are restricted to be {@link SerializedPage}s. You can
 * achieve this with
 * <ul>
 * <li>a {@link SerializingPageStore} delegating to this store and</li>
 * <li>delegating to a store that does not deserialize its pages, e.g. a {@link DiskPageStore}.</li>
 * </ul>
 * <p>
 * Each session gets its own random 256-bit AES key. Encryption uses the application's configured
 * {@link SecuritySettings#getCryptScheme() crypt scheme}; a page that can no longer be decrypted
 * (e.g. because the session key is gone or the data was tampered with) is treated as a cache miss.
 */
public class CryptingPageStore extends DelegatingPageStore
{
	private static final MetaDataKey<SessionData> KEY = new MetaDataKey<>()
	{
		private static final long serialVersionUID = 1L;
	};

	private final Application application;

	/**
	 * @param delegate
	 *            store to delegate to
	 * @param application
	 *            the application
	 */
	public CryptingPageStore(IPageStore delegate, Application application)
	{
		super(delegate);
		this.application = Args.notNull(application, "application");
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
		// session data must be added here *before* any asynchronous calls
		// when session is no longer available
		getSessionData(context);

		return getDelegate().canBeAsynchronous(context);
	}

	private SessionData getSessionData(IPageContext context)
	{
		return context.getSessionData(KEY, () -> new SessionData(generateKey()));
	}

	private SecretKey generateKey()
	{
		SecureRandom random = application.getSecuritySettings().getRandomSupplier().getRandom();
		return new SecretKeySpec(CipherUtils.generateKey("AES", 256, random).getEncoded(), "AES");
	}

	/**
	 * Builds an {@link ICrypt} bound to the current session's key and the application's crypto
	 * policy (encryption scheme + decryption whitelist).
	 */
	private ICrypt getCrypt(IPageContext context)
	{
		SecuritySettings settings = application.getSecuritySettings();
		return new SchemeCrypt(getSessionData(context).getKey(),
			settings.getRandomSupplier().getRandom(), settings.getCryptScheme(),
			settings.getWhitelistedCryptSchemes());
	}

	@Override
	public IManageablePage getPage(IPageContext context, int id)
	{
		IManageablePage page = getDelegate().getPage(context, id);

		if (page != null)
		{
			if (page instanceof SerializedPage == false)
			{
				throw new WicketRuntimeException("CryptingPageStore expects serialized pages");
			}
			SerializedPage serializedPage = (SerializedPage) page;

			byte[] encrypted = serializedPage.getData();
			// bind the page to the id we are looking up (the trusted method parameter), so a
			// blob that was stored for a different id fails authentication
			byte[] decrypted = getCrypt(context).decrypt(encrypted, aad(id));

			if (decrypted == null)
			{
				// the page could not be decrypted (e.g. a new session key or tampered data):
				// treat it as if it were no longer present
				return null;
			}

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

		SerializedPage serializedPage = (SerializedPage) page;

		byte[] decrypted = serializedPage.getData();
		byte[] encrypted = getCrypt(context).encrypt(decrypted, aad(serializedPage.getPageId()));

		page = new SerializedPage(page.getPageId(), serializedPage.getPageType(), encrypted);

		getDelegate().addPage(context, page);
	}

	/**
	 * The associated data binding an encrypted page to its id: the page id as 4 big-endian bytes.
	 */
	private static byte[] aad(int pageId)
	{
		return ByteBuffer.allocate(Integer.BYTES).putInt(pageId).array();
	}

	private static class SessionData implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private final SecretKey key;

		public SessionData(SecretKey key)
		{
			this.key = Args.notNull(key, "key");
		}

		public SecretKey getKey()
		{
			return key;
		}
	}
}

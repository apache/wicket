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
package org.apache.wicket.core.util.crypt;

import javax.crypto.SecretKey;

import org.apache.wicket.Application;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Session;

/**
 * The default {@link ICryptFactory}: it generates a fresh 256-bit AES key per user session and
 * stores it in the session's metadata. Using this factory forces the immediate creation of an
 * HTTP session.
 * <p>
 * Because the key is per session, data encrypted for one session cannot be decrypted from
 * another (or after the session has expired) &mdash; which is exactly what makes it safe to
 * encrypt session-scoped data such as URLs.
 */
public class KeyInSessionCryptFactory extends AbstractCryptFactory
{
	/** metadata-key used to store the crypto key in session metadata */
	private static final MetaDataKey<SecretKey> KEY = new MetaDataKey<>()
	{
		private static final long serialVersionUID = 1L;
	};

	@Override
	protected SecretKey getKey()
	{
		Session session = Session.get();
		session.bind();

		SecretKey key = session.getMetaData(KEY);
		if (key == null)
		{
			key = generateKey(
				Application.get().getSecuritySettings().getRandomSupplier().getRandom());
			session.setMetaData(KEY, key);
		}
		return key;
	}
}

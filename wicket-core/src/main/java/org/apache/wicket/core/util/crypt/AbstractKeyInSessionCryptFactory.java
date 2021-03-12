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

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Session;
import org.apache.wicket.util.crypt.ICrypt;
import org.apache.wicket.util.crypt.ICryptFactory;
import org.apache.wicket.util.io.IClusterable;


/**
 * Base class to implement crypt factories that store crypt into user session. Note that the use of
 * this crypt factory will result in an immediate creation of a http session.
 * 
 * @author andrea del bene
 *
 * @param <T>
 *            the type for the secret key.
 */
public abstract class AbstractKeyInSessionCryptFactory<T extends IClusterable>
	implements
		ICryptFactory
{
	/** metadata-key used to store crypto-key in session metadata */
	private final MetaDataKey<T> KEY = new MetaDataKey<T>()
	{
		private static final long serialVersionUID = 1L;
	};

	public AbstractKeyInSessionCryptFactory()
	{
		super();
	}

	/**
	 * Creates a new crypt for the current user session. If no user session is available, a new one
	 * is created.
	 * 
	 * @return
	 */
	@Override
	public ICrypt newCrypt()
	{
		Session session = Session.get();
		session.bind();

		// retrieve or generate encryption key from session
		T key = session.getMetaData(KEY);
		if (key == null)
		{
			// generate new key
			key = generateKey(session);
			session.setMetaData(KEY, key);
		}

		// build the crypt based on session key
		ICrypt crypt = createCrypt(key);
		return crypt;
	}

	/**
	 * Generates the secret key for a new crypt.
	 * 
	 * @param session
	 *            the current user session where crypt will be stored
	 * @return the secret key for a new crypt
	 */
	protected abstract T generateKey(Session session);

	/**
	 * @return the {@link org.apache.wicket.util.crypt.ICrypt} to use
	 */
	protected abstract ICrypt createCrypt(T key);
}
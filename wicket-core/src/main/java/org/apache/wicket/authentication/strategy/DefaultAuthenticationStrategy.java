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
package org.apache.wicket.authentication.strategy;

import org.apache.wicket.Application;
import org.apache.wicket.authentication.IAuthenticationStrategy;
import org.apache.wicket.util.cookies.CookieDefaults;
import org.apache.wicket.util.cookies.CookieUtils;
import org.apache.wicket.util.crypt.CachingSunJceCryptFactory;
import org.apache.wicket.util.crypt.ICrypt;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wicket's default implementation of an authentication strategy. It'll concatenate username and
 * password, encrypt it and put it into one Cookie.
 * 
 * @author Juergen Donnerstag
 */
public class DefaultAuthenticationStrategy implements IAuthenticationStrategy
{
	private static final Logger logger = LoggerFactory.getLogger(DefaultAuthenticationStrategy.class);

	/** The cookie name to store the username and password */
	protected final String cookieKey;

	/** The key to use for encrypting/decrypting the cookie value  */
	protected final String encryptionKey;

	/** The separator used to concatenate the username and password */
	protected final String VALUE_SEPARATOR = "-sep-";

	/** Cookie utils with default settings */
	private CookieUtils cookieUtils;

	/** Use to encrypt cookie values for username and password. */
	private ICrypt crypt;

	/**
	 * Constructor
	 * 
	 * @param cookieKey
	 *            The name of the cookie
	 */
	public DefaultAuthenticationStrategy(final String cookieKey)
	{
		this(cookieKey, defaultEncryptionKey(cookieKey));
	}

	private static String defaultEncryptionKey(String cookieKey)
	{
		if (Application.exists())
		{
			return Application.get().getName();
		}
		return cookieKey;
	}

	public DefaultAuthenticationStrategy(final String cookieKey, final String encryptionKey)
	{
		this.cookieKey = Args.notEmpty(cookieKey, "cookieKey");
		this.encryptionKey = Args.notEmpty(encryptionKey, "encryptionKey");
	}

	/**
	 * Make sure you always return a valid CookieUtils
	 * 
	 * @return CookieUtils
	 */
	protected CookieUtils getCookieUtils()
	{
		if (cookieUtils == null)
		{
			CookieDefaults settings = new CookieDefaults();
			cookieUtils = new CookieUtils(settings);
		}
		return cookieUtils;
	}

	/**
	 * @return The crypt engine to be used
	 */
	protected ICrypt getCrypt()
	{
		if (crypt == null)
		{
			CachingSunJceCryptFactory cryptFactory = new CachingSunJceCryptFactory(encryptionKey);
			crypt = cryptFactory.newCrypt();
		}
		return crypt;
	}

	@Override
	public String[] load()
	{
		String value = getCookieUtils().load(cookieKey);
		if (Strings.isEmpty(value) == false)
		{
			try
			{
				value = getCrypt().decryptUrlSafe(value);
			}
			catch (RuntimeException e)
			{
				logger.info(
					"Error decrypting login cookie: {}. The cookie will be deleted. Possible cause is that a session-relative encryption key was used to encrypt this cookie while this decryption attempt is happening in a different session, eg user coming back to the application after session expiration",
					cookieKey);
				getCookieUtils().remove(cookieKey);
				value = null;
			}
			return decode(value);
		}

		return null;
	}

	/**
	 * This method will decode decrypted cookie value based on application needs
	 *
	 * @param value decrypted cookie value
	 * @return decomposed values array, or null in case cookie value was empty.
	 */
	protected String[] decode(String value) {
		if (Strings.isEmpty(value) == false)
		{
			String username = null;
			String password = null;

			String[] values = value.split(VALUE_SEPARATOR);
			if ((values.length > 0) && (Strings.isEmpty(values[0]) == false))
			{
				username = values[0];
			}
			if ((values.length > 1) && (Strings.isEmpty(values[1]) == false))
			{
				password = values[1];
			}

			return new String[] { username, password };
		}
		return null;
	}

	@Override
	public void save(final String username, final String password)
	{
		String value = username + VALUE_SEPARATOR + password;

		String encryptedValue = getCrypt().encryptUrlSafe(value);

		getCookieUtils().save(cookieKey, encryptedValue);
	}

	@Override
	public void remove()
	{
		getCookieUtils().remove(cookieKey);
	}
}

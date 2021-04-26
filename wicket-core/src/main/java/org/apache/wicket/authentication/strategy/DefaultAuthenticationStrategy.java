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

import java.util.UUID;

import org.apache.wicket.authentication.IAuthenticationStrategy;
import org.apache.wicket.util.cookies.CookieDefaults;
import org.apache.wicket.util.cookies.CookieUtils;
import org.apache.wicket.util.crypt.ICrypt;
import org.apache.wicket.util.crypt.SunJceCrypt;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wicket's default implementation of an authentication strategy. It'll concatenate username and
 * password, encrypt it and put it into one Cookie.
 * <p>
 * Note: To support automatic authentication across application restarts you have to use
 * the constructor {@link DefaultAuthenticationStrategy#DefaultAuthenticationStrategy(String, ICrypt)}.
 * 
 * @author Juergen Donnerstag
 */
public class DefaultAuthenticationStrategy implements IAuthenticationStrategy
{
	private static final Logger logger = LoggerFactory.getLogger(DefaultAuthenticationStrategy.class);

	/** The cookie name to store the username and password */
	protected final String cookieKey;

	/**
	 * @deprecated no longer used TODO remove in Wicket 10
	 */
	@Deprecated(forRemoval = true)
	protected final String encryptionKey = null;

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
	 *            
	 * @deprecated supply a crypt instead TODO remove in Wicket 10
	 */
	@Deprecated(forRemoval = true)
	public DefaultAuthenticationStrategy(final String cookieKey)
	{
		this(cookieKey, defaultEncryptionKey());
	}

	private static String defaultEncryptionKey()
	{
		return UUID.randomUUID().toString();
	}

	/**
	 * @deprecated supply a crypt instead TODO remove in Wicket 10
	 */
	@Deprecated(forRemoval = true)
	public DefaultAuthenticationStrategy(final String cookieKey, final String encryptionKey)
	{
		this(cookieKey, defaultCrypt(encryptionKey));
	}

	private static ICrypt defaultCrypt(String encryptionKey)
	{
		byte[] salt = SunJceCrypt.randomSalt();

		SunJceCrypt crypt = new SunJceCrypt(salt, 1000);
		crypt.setKey(encryptionKey);
		return crypt;
	}

	/**
	 * This is the recommended constructor to be used, which allows automatic authentication across
	 * application restarts.  
	 * 
	 * @param cookieKey
	 *            The name of the cookie
	 * @param crypt
	 *            the crypt
	 */
	public DefaultAuthenticationStrategy(final String cookieKey, ICrypt crypt)
	{
		this.cookieKey = Args.notEmpty(cookieKey, "cookieKey");
		this.crypt = Args.notNull(crypt, "crypt");
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
			settings.setHttpOnly(true);
			cookieUtils = new CookieUtils(settings);
		}
		return cookieUtils;
	}

	/**
	 * @return The crypt engine to be used
	 */
	protected ICrypt getCrypt()
	{
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
	public void save(final String credential, final String... extraCredentials)
	{
		String encryptedValue = getCrypt().encryptUrlSafe(encode(credential, extraCredentials));

		getCookieUtils().save(cookieKey, encryptedValue);
	}

	/**
	 * This method can be overridden to provide different encoding mechanism
	 *
	 * @param credential
	 * @param extraCredentials
	 * @return String representation of the parameters given
	 */
	protected String encode(final String credential, final String... extraCredentials)
	{
		StringBuilder value = new StringBuilder(credential);
		if (extraCredentials != null)
		{
			for (String extraCredential : extraCredentials)
			{
				value.append(VALUE_SEPARATOR).append(extraCredential);
			}
		}
		return value.toString();
	}

	@Override
	public void remove()
	{
		getCookieUtils().remove(cookieKey);
	}
}

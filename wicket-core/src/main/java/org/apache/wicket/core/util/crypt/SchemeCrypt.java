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

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.apache.wicket.util.lang.Args;

/**
 * The default {@link ICrypt} implementation. It writes self-describing, authenticated ciphertext
 * of the form {@code marker(1) || scheme-payload} where the marker is the
 * {@link ICryptScheme#id() id} of the scheme that produced the payload.
 * <p>
 * <strong>Encryption</strong> always uses the configured {@code encryptionScheme} (the strongest
 * scheme). <strong>Decryption</strong> reads the marker, looks the scheme up in a whitelist and,
 * if allowed, hands the rest to that scheme; otherwise it returns {@code null}. The marker is
 * authenticated as associated data, so it cannot be altered to force a different scheme, and any
 * scheme not in the whitelist is refused &mdash; together these prevent downgrade attacks.
 * <p>
 * To migrate to a stronger scheme, temporarily whitelist the old scheme (so existing data still
 * decrypts) while setting the new scheme as {@code encryptionScheme} (so new data is upgraded),
 * then drop the old scheme from the whitelist once the data has been rewritten.
 */
public class SchemeCrypt implements ICrypt
{
	private final SecretKey key;

	private final SecureRandom random;

	private final ICryptScheme encryptionScheme;

	private final Map<Byte, ICryptScheme> allowedSchemes;

	/**
	 * @param key
	 *            the secret key
	 * @param random
	 *            source of randomness for nonces
	 * @param encryptionScheme
	 *            the scheme used for all encryption; always allowed for decryption too
	 * @param whitelistedSchemes
	 *            additional schemes allowed for decryption (may be empty)
	 */
	public SchemeCrypt(SecretKey key, SecureRandom random, ICryptScheme encryptionScheme,
		Collection<? extends ICryptScheme> whitelistedSchemes)
	{
		this.key = Args.notNull(key, "key");
		this.random = Args.notNull(random, "random");
		this.encryptionScheme = Args.notNull(encryptionScheme, "encryptionScheme");
		Args.notNull(whitelistedSchemes, "whitelistedSchemes");

		Map<Byte, ICryptScheme> map = new HashMap<>();
		// the encryption scheme is always allowed for decryption
		map.put(encryptionScheme.id(), encryptionScheme);
		for (ICryptScheme scheme : whitelistedSchemes)
		{
			map.put(scheme.id(), scheme);
		}
		this.allowedSchemes = map;
	}

	@Override
	public byte[] encrypt(byte[] plainBytes)
	{
		Args.notNull(plainBytes, "plainBytes");

		byte id = encryptionScheme.id();
		byte[] aad = { id };
		byte[] payload = encryptionScheme.encrypt(plainBytes, key, aad, random);

		byte[] result = new byte[payload.length + 1];
		result[0] = id;
		System.arraycopy(payload, 0, result, 1, payload.length);
		return result;
	}

	@Override
	public byte[] decrypt(byte[] encryptedBytes)
	{
		if (encryptedBytes == null || encryptedBytes.length < 1)
		{
			return null;
		}

		byte id = encryptedBytes[0];
		ICryptScheme scheme = allowedSchemes.get(id);
		if (scheme == null)
		{
			// unknown or non-whitelisted scheme: refuse (downgrade protection)
			return null;
		}

		byte[] aad = { id };
		byte[] payload = Arrays.copyOfRange(encryptedBytes, 1, encryptedBytes.length);
		return scheme.decrypt(payload, key, aad);
	}
}

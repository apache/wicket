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

import javax.crypto.SecretKey;

/**
 * A single, self-identifying authenticated encryption scheme.
 * <p>
 * Every scheme has a stable {@link #id()} that {@link SchemeCrypt} writes as a one-byte marker
 * in front of the ciphertext, so the correct scheme can be selected on decryption and matched
 * against a whitelist (downgrade protection). The marker is passed to {@link #encrypt} /
 * {@link #decrypt} as <em>additional authenticated data</em> ({@code aad}) so it is covered by
 * the authentication tag and cannot be altered to force a different scheme.
 * <p>
 * Schemes must use authenticated (AEAD) encryption: {@link #decrypt} returns {@code null} when
 * authentication fails. A scheme also produces the {@link SecretKey}s it consumes (see
 * {@link #generateKey(SecureRandom)}) &mdash; the key material is a property of the scheme, whereas
 * a {@link ICryptFactory factory} only decides <em>where</em> the key lives (per session, global,
 * externally supplied). All schemes sharing one {@link SchemeCrypt} must produce mutually
 * compatible keys, since existing ciphertext is decrypted with the current key during migration.
 * <p>
 * A scheme offers both a randomized ({@link #encrypt}) and a deterministic
 * ({@link #encryptDeterministic}) encryption path, the latter for callers that need a stable
 * result &mdash; see {@link ICrypt#encryptDeterministic(byte[], byte[])}. Both must produce the
 * same ciphertext format, so that a single {@link #decrypt} handles either.
 * <p>
 * Implementations must be thread-safe and pick a unique, stable {@link #id()}. Wicket reserves
 * ids {@code 1..31}; custom schemes should use ids {@code >= 32}.
 * <p>
 * A custom scheme that does not honour the AEAD contract - returning unverified plaintext instead
 * of {@code null} on authentication failure - silently removes the tamper detection that callers
 * rely on. See {@code SECURITY.md} for the trust assumptions Wicket makes about encrypted data,
 * and {@link org.apache.wicket.pageStore.CryptingPageStore} for the page-store use of a scheme.
 */
public interface ICryptScheme
{
	/**
	 * @return the stable, unique one-byte marker identifying this scheme
	 */
	byte id();

	/**
	 * Generate a new secret key suitable for this scheme.
	 *
	 * @param random
	 *            source of randomness
	 * @return a new, serializable {@link SecretKey}
	 */
	SecretKey generateKey(SecureRandom random);

	/**
	 * Encrypt the given plaintext.
	 *
	 * @param plaintext
	 *            the bytes to encrypt
	 * @param key
	 *            the secret key
	 * @param aad
	 *            additional authenticated data (the scheme marker); authenticated but not encrypted
	 * @param random
	 *            source of randomness for the nonce
	 * @return the ciphertext (including nonce and authentication tag)
	 */
	/**
	 * Encrypts, leaving {@code prefixLength} bytes untouched at the start of the result for the
	 * caller to fill in. {@link SchemeCrypt} puts its scheme marker there; without the reservation
	 * it would have to copy the whole ciphertext to make room for a single byte.
	 *
	 * @param plaintext
	 *            what to encrypt
	 * @param key
	 *            the key to encrypt with
	 * @param aad
	 *            additional authenticated data, may be {@code null}
	 * @param random
	 *            source of randomness for the nonce
	 * @param prefixLength
	 *            how many bytes to leave free at the start of the result
	 * @return a freshly allocated array holding the ciphertext, preceded by {@code prefixLength}
	 *         bytes the caller is free to write into
	 */
	byte[] encrypt(byte[] plaintext, SecretKey key, byte[] aad, SecureRandom random,
		int prefixLength);

	/**
	 * @see #encrypt(byte[], SecretKey, byte[], SecureRandom, int)
	 */
	default byte[] encrypt(byte[] plaintext, SecretKey key, byte[] aad, SecureRandom random)
	{
		return encrypt(plaintext, key, aad, random, 0);
	}

	/**
	 * Encrypt the given plaintext deterministically: the same {@code plaintext}, {@code key} and
	 * {@code aad} must always produce the same ciphertext. Note the deliberate absence of a
	 * {@link SecureRandom} parameter &mdash; a scheme derives whatever it would otherwise draw at
	 * random from its inputs, for instance by deriving the nonce from the plaintext with a
	 * pseudo-random function.
	 * <p>
	 * The result must be decryptable by {@link #decrypt} exactly like the output of
	 * {@link #encrypt}. An implementation that returns randomized ciphertext here breaks the
	 * callers that rely on stability, notably
	 * {@link org.apache.wicket.core.request.mapper.CryptoMapper}, whose URLs would then change on
	 * every request.
	 *
	 * @param plaintext
	 *            the bytes to encrypt
	 * @param key
	 *            the secret key
	 * @param aad
	 *            additional authenticated data (the scheme marker); authenticated but not encrypted
	 * @return the ciphertext (including nonce and authentication tag)
	 */
	/**
	 * As {@link #encrypt(byte[], SecretKey, byte[], SecureRandom, int)}, but deriving the nonce
	 * from the input so that the same input yields the same ciphertext.
	 *
	 * @param plaintext
	 *            what to encrypt
	 * @param key
	 *            the key to encrypt with
	 * @param aad
	 *            additional authenticated data, may be {@code null}
	 * @param prefixLength
	 *            how many bytes to leave free at the start of the result
	 * @return a freshly allocated array holding the ciphertext, preceded by {@code prefixLength}
	 *         bytes the caller is free to write into
	 */
	byte[] encryptDeterministic(byte[] plaintext, SecretKey key, byte[] aad, int prefixLength);

	/**
	 * @see #encryptDeterministic(byte[], SecretKey, byte[], int)
	 */
	default byte[] encryptDeterministic(byte[] plaintext, SecretKey key, byte[] aad)
	{
		return encryptDeterministic(plaintext, key, aad, 0);
	}

	/**
	 * Decrypt the given ciphertext.
	 *
	 * @param ciphertext
	 *            the bytes produced by {@link #encrypt} or {@link #encryptDeterministic} (nonce +
	 *            ciphertext + tag)
	 * @param key
	 *            the secret key
	 * @param aad
	 *            the additional authenticated data that was supplied on encryption (the marker)
	 * @return the decrypted plaintext, or {@code null} if authentication fails or the input is
	 *         malformed
	 */
	/**
	 * Decrypts {@code length} bytes of {@code ciphertext} starting at {@code offset}, so that a
	 * caller which prefixed the ciphertext can skip its own header without copying the rest.
	 *
	 * @param ciphertext
	 *            the buffer holding the ciphertext
	 * @param offset
	 *            where the ciphertext starts
	 * @param length
	 *            how many bytes of ciphertext there are
	 * @param key
	 *            the key to decrypt with
	 * @param aad
	 *            additional authenticated data, may be {@code null}
	 * @return the plaintext, or {@code null} if the input is not authentic
	 */
	byte[] decrypt(byte[] ciphertext, int offset, int length, SecretKey key, byte[] aad);

	/**
	 * @see #decrypt(byte[], int, int, SecretKey, byte[])
	 */
	default byte[] decrypt(byte[] ciphertext, SecretKey key, byte[] aad)
	{
		return decrypt(ciphertext, 0, ciphertext.length, key, aad);
	}
}

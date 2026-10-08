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

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * The unified encryption/decryption abstraction used throughout Wicket (page store, URL
 * encryption and authentication cookies).
 * <p>
 * Implementations operate on raw {@code byte[]} and are expected to produce
 * <em>self-describing</em>, authenticated ciphertext (see {@link ICryptScheme} and
 * {@link SchemeCrypt}). The {@link #decrypt(byte[])} contract is to return {@code null} on
 * <em>any</em> failure (unknown/non-whitelisted scheme, authentication failure, malformed
 * input) rather than throwing, so callers can uniformly treat undecryptable data as
 * absent/expired.
 * <p>
 * The default {@link #encryptUrlSafe(String)} / {@link #decryptUrlSafe(String)} methods add a
 * URL-safe Base64 {@code String} layer on top of the {@code byte[]} core.
 * <p>
 * Encryption comes in two flavours: {@link #encrypt(byte[], byte[])} is randomized (a fresh nonce
 * per call, so the same input yields different ciphertext every time) while
 * {@link #encryptDeterministic(byte[], byte[])} is stable (the same input yields the same
 * ciphertext for as long as the key lives). Both produce the same ciphertext format and are
 * decrypted by the same {@link #decrypt(byte[], byte[])}.
 */
public interface ICrypt
{
	/**
	 * Encrypt the given bytes.
	 *
	 * @param plainBytes
	 *            the bytes to encrypt, must not be {@code null}
	 * @param associatedData
	 *            optional additional data that is authenticated but not encrypted; the identical
	 *            value must be supplied to {@link #decrypt(byte[], byte[])} or decryption fails.
	 *            Use it to bind the ciphertext to its context (e.g. a page id). May be
	 *            {@code null}.
	 * @return the encrypted bytes
	 */
	byte[] encrypt(byte[] plainBytes, byte[] associatedData);

	/**
	 * Decrypt the given bytes.
	 *
	 * @param encryptedBytes
	 *            the bytes to decrypt
	 * @param associatedData
	 *            the same additional data that was supplied to {@link #encrypt(byte[], byte[])},
	 *            or {@code null} if none was used
	 * @return the decrypted bytes, or {@code null} if the input could not be decrypted for any
	 *         reason (unknown or non-whitelisted scheme, failed authentication including an
	 *         associated-data mismatch, malformed input)
	 */
	byte[] decrypt(byte[] encryptedBytes, byte[] associatedData);

	/**
	 * Encrypt the given bytes <em>deterministically</em>: the same {@code plainBytes} and
	 * {@code associatedData} always produce the same ciphertext for as long as the key lives. The
	 * result is decrypted by {@link #decrypt(byte[], byte[])} just like randomized ciphertext.
	 * <p>
	 * Use this only where a stable result is required &mdash; encrypted URLs, so that a regenerated
	 * URL matches the one the client requested and stays cacheable. Prefer
	 * {@link #encrypt(byte[], byte[])} everywhere else: determinism reveals that two ciphertexts
	 * encrypt equal plaintexts, and lets anyone holding the key confirm a guessed plaintext by
	 * re-encrypting it.
	 *
	 * @param plainBytes
	 *            the bytes to encrypt, must not be {@code null}
	 * @param associatedData
	 *            optional additional data that is authenticated but not encrypted, as for
	 *            {@link #encrypt(byte[], byte[])}. May be {@code null}.
	 * @return the encrypted bytes
	 */
	byte[] encryptDeterministic(byte[] plainBytes, byte[] associatedData);

	/**
	 * Encrypt the given bytes without additional associated data.
	 *
	 * @param plainBytes
	 *            the bytes to encrypt, must not be {@code null}
	 * @return the encrypted bytes
	 */
	default byte[] encrypt(byte[] plainBytes)
	{
		return encrypt(plainBytes, null);
	}

	/**
	 * Deterministically encrypt the given bytes without additional associated data.
	 *
	 * @param plainBytes
	 *            the bytes to encrypt, must not be {@code null}
	 * @return the encrypted bytes
	 * @see #encryptDeterministic(byte[], byte[])
	 */
	default byte[] encryptDeterministic(byte[] plainBytes)
	{
		return encryptDeterministic(plainBytes, null);
	}

	/**
	 * Decrypt the given bytes that were encrypted without additional associated data.
	 *
	 * @param encryptedBytes
	 *            the bytes to decrypt
	 * @return the decrypted bytes, or {@code null} if the input could not be decrypted
	 */
	default byte[] decrypt(byte[] encryptedBytes)
	{
		return decrypt(encryptedBytes, null);
	}

	/**
	 * Encrypt the given text into a URL-safe (Base64, no padding) {@code String}.
	 *
	 * @param plainText
	 *            the text to encrypt
	 * @return the encrypted, URL-safe text
	 */
	default String encryptUrlSafe(String plainText)
	{
		byte[] encrypted = encrypt(plainText.getBytes(StandardCharsets.UTF_8));
		return Base64.getUrlEncoder().withoutPadding().encodeToString(encrypted);
	}

	/**
	 * Deterministically encrypt the given text into a URL-safe (Base64, no padding)
	 * {@code String}: the same {@code plainText} always yields the same result for as long as the
	 * key lives. This is what {@link org.apache.wicket.core.request.mapper.CryptoMapper} uses, so
	 * that encrypted URLs are stable.
	 *
	 * @param plainText
	 *            the text to encrypt
	 * @return the encrypted, URL-safe text
	 * @see #encryptDeterministic(byte[], byte[])
	 */
	default String encryptUrlSafeDeterministic(String plainText)
	{
		byte[] encrypted = encryptDeterministic(plainText.getBytes(StandardCharsets.UTF_8));
		return Base64.getUrlEncoder().withoutPadding().encodeToString(encrypted);
	}

	/**
	 * Decrypt a URL-safe (Base64) {@code String} produced by {@link #encryptUrlSafe(String)} or
	 * {@link #encryptUrlSafeDeterministic(String)}.
	 *
	 * @param encryptedText
	 *            the encrypted, URL-safe text
	 * @return the decrypted text, or {@code null} if it could not be decrypted
	 */
	default String decryptUrlSafe(String encryptedText)
	{
		try
		{
			byte[] decoded = Base64.getUrlDecoder().decode(encryptedText);
			byte[] decrypted = decrypt(decoded);
			if (decrypted == null)
			{
				return null;
			}
			return new String(decrypted, StandardCharsets.UTF_8);
		}
		catch (RuntimeException ex)
		{
			// e.g. invalid Base64 input
			return null;
		}
	}
}

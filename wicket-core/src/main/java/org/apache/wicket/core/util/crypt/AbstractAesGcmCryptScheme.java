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

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.util.crypt.CipherUtils;

/**
 * Base class for the AES-256 GCM-family {@link ICryptScheme}s ({@link AesGcmCryptScheme} and
 * {@link AesGcmSivCryptScheme}). It owns everything the two variants share:
 * <ul>
 * <li>the 256-bit AES key they consume (see {@link #generateKey(SecureRandom)}) &mdash; the key is
 * a property of the scheme, not of the factory that decides where the key lives;</li>
 * <li>the ciphertext layout {@code nonce(12) || ciphertext || tag(16)} and the encrypt/decrypt
 * flow (96-bit nonce, 128-bit authentication tag, marker authenticated as associated
 * data).</li>
 * </ul>
 * {@link #encrypt} draws the nonce at random; {@link #encryptDeterministic} derives it from the
 * plaintext with HMAC-SHA-256 under a key derived from {@code key} (an SIV-like construction), so
 * that identical input reproduces identical ciphertext. Both write the same layout, so a single
 * {@link #decrypt} reads either.
 * <p>
 * A derived nonce means the same nonce is intentionally reused for identical plaintexts, which only
 * reveals that the plaintexts are equal. Distinct plaintexts colliding on a nonce is the case
 * AES-GCM does not survive, and for a 96-bit nonce derived by a pseudo-random function that has
 * probability of roughly <i>n</i><sup>2</sup>/2<sup>97</sup> over <i>n</i> distinct plaintexts per
 * key &mdash; negligible for the URLs this is used for. {@link AesGcmSivCryptScheme} is nonce-misuse
 * resistant by construction, so the consideration does not arise there at all.
 * <p>
 * Subclasses only supply the concrete {@link Cipher} and its {@link AlgorithmParameterSpec}, plus a
 * stable {@link #id()}.
 */
public abstract class AbstractAesGcmCryptScheme implements ICryptScheme
{
	/** AES key size in bits. */
	protected static final int KEY_LENGTH_BITS = 256;

	/** Nonce (IV) length in bytes; 96 bits is the GCM-family recommended size. */
	protected static final int NONCE_LENGTH = 12;

	/** Authentication tag length in bits. */
	protected static final int TAG_LENGTH_BITS = 128;

	/** MAC used to derive the nonce for {@link #encryptDeterministic}. */
	private static final String NONCE_MAC_ALGORITHM = "HmacSHA256";

	/** Domain separation for the key that {@link #deriveNonce} derives the nonce with. */
	private static final byte[] NONCE_SUBKEY_INFO = "wicket-deterministic-nonce"
		.getBytes(StandardCharsets.US_ASCII);

	@Override
	public SecretKey generateKey(SecureRandom random)
	{
		// wrap in a SecretKeySpec so the key is guaranteed serializable (a per-session key is
		// stored in the session's metadata)
		byte[] encoded = CipherUtils.generateKey("AES", KEY_LENGTH_BITS, random).getEncoded();
		return new SecretKeySpec(encoded, "AES");
	}

	@Override
	public byte[] encrypt(byte[] plaintext, SecretKey key, byte[] aad, SecureRandom random)
	{
		byte[] nonce = new byte[NONCE_LENGTH];
		random.nextBytes(nonce);

		return encrypt(plaintext, key, aad, nonce);
	}

	@Override
	public byte[] encryptDeterministic(byte[] plaintext, SecretKey key, byte[] aad)
	{
		return encrypt(plaintext, key, aad, deriveNonce(plaintext, key, aad));
	}

	/**
	 * Encrypts with the given nonce, which the caller either drew at random or derived from the
	 * plaintext.
	 *
	 * @param plaintext
	 *            the bytes to encrypt
	 * @param key
	 *            the secret key
	 * @param aad
	 *            additional authenticated data, may be {@code null}
	 * @param nonce
	 *            the nonce to use, {@link #NONCE_LENGTH} bytes
	 * @return {@code nonce || ciphertext || tag}
	 */
	private byte[] encrypt(byte[] plaintext, SecretKey key, byte[] aad, byte[] nonce)
	{
		try
		{
			Cipher cipher = getCipher();
			cipher.init(Cipher.ENCRYPT_MODE, key, newParameterSpec(nonce));
			if (aad != null)
			{
				cipher.updateAAD(aad);
			}

			byte[] ciphertext = cipher.doFinal(plaintext);

			byte[] result = Arrays.copyOf(nonce, nonce.length + ciphertext.length);
			System.arraycopy(ciphertext, 0, result, nonce.length, ciphertext.length);
			return result;
		}
		catch (GeneralSecurityException ex)
		{
			throw new WicketRuntimeException(ex);
		}
	}

	/**
	 * Derives the nonce for {@link #encryptDeterministic} as
	 * {@code HMAC(HMAC(key, "wicket-deterministic-nonce"), len(aad) || aad || plaintext)},
	 * truncated to {@link #NONCE_LENGTH}. The inner HMAC separates nonce derivation from the
	 * encryption use of the same key; the length prefix keeps the {@code aad}/plaintext boundary
	 * unambiguous, so that moving bytes between the two cannot yield the same nonce.
	 *
	 * @param plaintext
	 *            the bytes to encrypt
	 * @param key
	 *            the secret key
	 * @param aad
	 *            additional authenticated data, may be {@code null}
	 * @return a nonce of {@link #NONCE_LENGTH} bytes
	 */
	private static byte[] deriveNonce(byte[] plaintext, SecretKey key, byte[] aad)
	{
		try
		{
			Mac mac = Mac.getInstance(NONCE_MAC_ALGORITHM);
			mac.init(new SecretKeySpec(key.getEncoded(), NONCE_MAC_ALGORITHM));
			byte[] subKey = mac.doFinal(NONCE_SUBKEY_INFO);

			mac.init(new SecretKeySpec(subKey, NONCE_MAC_ALGORITHM));
			mac.update(
				ByteBuffer.allocate(Integer.BYTES).putInt(aad == null ? 0 : aad.length).array());
			if (aad != null)
			{
				mac.update(aad);
			}
			return Arrays.copyOf(mac.doFinal(plaintext), NONCE_LENGTH);
		}
		catch (GeneralSecurityException ex)
		{
			throw new WicketRuntimeException(ex);
		}
	}

	@Override
	public byte[] decrypt(byte[] ciphertext, SecretKey key, byte[] aad)
	{
		try
		{
			if (ciphertext.length < NONCE_LENGTH)
			{
				return null;
			}

			byte[] nonce = Arrays.copyOfRange(ciphertext, 0, NONCE_LENGTH);

			Cipher cipher = getCipher();
			cipher.init(Cipher.DECRYPT_MODE, key, newParameterSpec(nonce));
			if (aad != null)
			{
				cipher.updateAAD(aad);
			}

			return cipher.doFinal(ciphertext, NONCE_LENGTH, ciphertext.length - NONCE_LENGTH);
		}
		catch (GeneralSecurityException ex)
		{
			// authentication failure or malformed input
			return null;
		}
	}

	/**
	 * @return the {@link Cipher} to use
	 * @throws GeneralSecurityException
	 *             if the cipher is unavailable
	 */
	protected abstract Cipher getCipher() throws GeneralSecurityException;

	/**
	 * @param nonce
	 *            the freshly generated nonce
	 * @return the {@link AlgorithmParameterSpec} binding the nonce and tag length for this cipher
	 */
	protected abstract AlgorithmParameterSpec newParameterSpec(byte[] nonce);
}

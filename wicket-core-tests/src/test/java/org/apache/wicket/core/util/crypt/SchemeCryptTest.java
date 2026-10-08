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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Arrays;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests for {@link SchemeCrypt} and the {@link ICryptScheme} implementations, covering
 * round-trip, tamper detection, downgrade protection (marker whitelist + authenticated marker)
 * and scheme migration.
 */
public class SchemeCryptTest
{
	private static final SecureRandom RANDOM = new SecureRandom();

	@BeforeAll
	static void init()
	{
		Security.addProvider(new BouncyCastleProvider());
	}

	static List<ICryptScheme> schemes()
	{
		return List.of(new AesGcmCryptScheme(), new AesGcmSivCryptScheme());
	}

	private static SecretKey newKey()
	{
		byte[] bytes = new byte[32];
		RANDOM.nextBytes(bytes);
		return new SecretKeySpec(bytes, "AES");
	}

	private static SchemeCrypt crypt(SecretKey key, ICryptScheme scheme)
	{
		return new SchemeCrypt(key, RANDOM, scheme, List.of(scheme));
	}

	@ParameterizedTest
	@MethodSource("schemes")
	void roundTrip(ICryptScheme scheme)
	{
		SchemeCrypt crypt = crypt(newKey(), scheme);
		byte[] plain = "the quick brown fox 花鳥風月".getBytes(StandardCharsets.UTF_8);
		assertArrayEquals(plain, crypt.decrypt(crypt.encrypt(plain)));
	}

	@ParameterizedTest
	@MethodSource("schemes")
	void generatedKeyIsUsableAes256(ICryptScheme scheme)
	{
		SecretKey key = scheme.generateKey(RANDOM);
		assertEquals("AES", key.getAlgorithm());
		assertEquals(32, key.getEncoded().length);

		// a crypt built on the scheme-generated key round-trips
		SchemeCrypt crypt = crypt(key, scheme);
		byte[] plain = "generated-key".getBytes(StandardCharsets.UTF_8);
		assertArrayEquals(plain, crypt.decrypt(crypt.encrypt(plain)));
	}

	@ParameterizedTest
	@MethodSource("schemes")
	void ciphertextStartsWithSchemeMarker(ICryptScheme scheme)
	{
		byte[] enc = crypt(newKey(), scheme).encrypt("x".getBytes(StandardCharsets.UTF_8));
		assertEquals(scheme.id(), enc[0]);
	}

	@ParameterizedTest
	@MethodSource("schemes")
	void encryptionIsNotDeterministic(ICryptScheme scheme)
	{
		SchemeCrypt crypt = crypt(newKey(), scheme);
		byte[] plain = "same input".getBytes(StandardCharsets.UTF_8);
		assertFalse(Arrays.equals(crypt.encrypt(plain), crypt.encrypt(plain)));
	}

	@ParameterizedTest
	@MethodSource("schemes")
	void deterministicEncryptionIsStable(ICryptScheme scheme)
	{
		SchemeCrypt crypt = crypt(newKey(), scheme);
		byte[] plain = "same input".getBytes(StandardCharsets.UTF_8);

		byte[] deterministic = crypt.encryptDeterministic(plain);

		// the same input always yields the same ciphertext...
		assertArrayEquals(deterministic, crypt.encryptDeterministic(plain));
		// ...which is not what the randomized path produces...
		assertFalse(Arrays.equals(deterministic, crypt.encrypt(plain)));
		// ...and it is read back by the ordinary decrypt, in the same format
		assertEquals(scheme.id(), deterministic[0]);
		assertArrayEquals(plain, crypt.decrypt(deterministic));
	}

	@ParameterizedTest
	@MethodSource("schemes")
	void deterministicEncryptionDiffersPerKey(ICryptScheme scheme)
	{
		byte[] plain = "same input".getBytes(StandardCharsets.UTF_8);

		assertFalse(Arrays.equals(crypt(newKey(), scheme).encryptDeterministic(plain),
			crypt(newKey(), scheme).encryptDeterministic(plain)));
	}

	@ParameterizedTest
	@MethodSource("schemes")
	void deterministicEncryptionDiffersPerAssociatedData(ICryptScheme scheme)
	{
		SchemeCrypt crypt = crypt(newKey(), scheme);
		byte[] plain = "same input".getBytes(StandardCharsets.UTF_8);
		byte[] aad = { 1, 2, 3, 4 };

		byte[] withAad = crypt.encryptDeterministic(plain, aad);

		assertFalse(Arrays.equals(withAad, crypt.encryptDeterministic(plain)));
		// each still round-trips with its own associated data only
		assertArrayEquals(plain, crypt.decrypt(withAad, aad));
		assertNull(crypt.decrypt(withAad));
	}

	@ParameterizedTest
	@MethodSource("schemes")
	void deterministicNonceDiffersPerPlaintext(ICryptScheme scheme)
	{
		// a nonce shared by two *different* plaintexts is what AES-GCM does not survive, so the
		// derived nonce must vary with the plaintext. Layout is marker(1) || nonce(12) || ...
		final int nonceEnd = 1 + 12;

		SchemeCrypt crypt = crypt(newKey(), scheme);
		byte[] first = crypt.encryptDeterministic("plaintext one".getBytes(StandardCharsets.UTF_8));
		byte[] second = crypt.encryptDeterministic("plaintext two".getBytes(StandardCharsets.UTF_8));

		assertFalse(Arrays.equals(Arrays.copyOfRange(first, 1, nonceEnd),
			Arrays.copyOfRange(second, 1, nonceEnd)));
	}

	@ParameterizedTest
	@MethodSource("schemes")
	void tamperingAnywhereIsDetected(ICryptScheme scheme)
	{
		SchemeCrypt crypt = crypt(newKey(), scheme);
		byte[] enc = crypt.encrypt("secret payload".getBytes(StandardCharsets.UTF_8));

		// flipping any bit of the scheme payload must be detected (return null)
		for (int i = 1; i < enc.length; i++)
		{
			byte[] tampered = enc.clone();
			tampered[i] ^= 0x01;
			assertNull(crypt.decrypt(tampered), "tampering at index " + i + " went undetected");
		}
	}

	@ParameterizedTest
	@MethodSource("schemes")
	void wrongKeyCannotDecrypt(ICryptScheme scheme)
	{
		byte[] enc = crypt(newKey(), scheme).encrypt("data".getBytes(StandardCharsets.UTF_8));
		assertNull(crypt(newKey(), scheme).decrypt(enc));
	}

	@ParameterizedTest
	@MethodSource("schemes")
	void associatedDataMustMatch(ICryptScheme scheme)
	{
		SchemeCrypt crypt = crypt(newKey(), scheme);
		byte[] plain = "data".getBytes(StandardCharsets.UTF_8);
		byte[] aad = { 1, 2, 3, 4 };

		byte[] enc = crypt.encrypt(plain, aad);

		// only the identical associated data decrypts
		assertArrayEquals(plain, crypt.decrypt(enc, aad));
		// a different, absent, or no-arg associated data fails authentication
		assertNull(crypt.decrypt(enc, new byte[] { 9, 9, 9, 9 }));
		assertNull(crypt.decrypt(enc, null));
		assertNull(crypt.decrypt(enc));

		// data encrypted without associated data cannot be read as if it had some, and vice versa
		byte[] encNoAad = crypt.encrypt(plain);
		assertArrayEquals(plain, crypt.decrypt(encNoAad));
		assertNull(crypt.decrypt(encNoAad, aad));
	}

	@Test
	void emptyOrShortInputReturnsNull()
	{
		SchemeCrypt crypt = crypt(newKey(), new AesGcmCryptScheme());
		assertNull(crypt.decrypt(new byte[0]));
		assertNull(crypt.decrypt(new byte[] { AesGcmCryptScheme.ID }));
		assertNull(crypt.decrypt(new byte[] { AesGcmCryptScheme.ID, 1, 2, 3 }));
	}

	@Test
	void nonWhitelistedSchemeIsRefused()
	{
		SecretKey key = newKey();
		byte[] enc = crypt(key, new AesGcmSivCryptScheme())
			.encrypt("data".getBytes(StandardCharsets.UTF_8));

		AesGcmCryptScheme gcm = new AesGcmCryptScheme();
		SchemeCrypt gcmOnly = new SchemeCrypt(key, RANDOM, gcm, List.of(gcm));
		// the SIV marker is not whitelisted -> refuse (downgrade protection)
		assertNull(gcmOnly.decrypt(enc));
	}

	@Test
	void relabelingSchemeMarkerIsRefused()
	{
		// both schemes whitelisted; the marker is authenticated, so relabeling ciphertext
		// from one scheme to another must fail to decrypt
		SecretKey key = newKey();
		AesGcmCryptScheme gcm = new AesGcmCryptScheme();
		AesGcmSivCryptScheme siv = new AesGcmSivCryptScheme();
		SchemeCrypt crypt = new SchemeCrypt(key, RANDOM, gcm, List.of(gcm, siv));

		byte[] enc = crypt.encrypt("data".getBytes(StandardCharsets.UTF_8));
		assertEquals(gcm.id(), enc[0]);

		enc[0] = siv.id();
		assertNull(crypt.decrypt(enc));
	}

	@Test
	void migrationBetweenSchemes()
	{
		SecretKey key = newKey();
		AesGcmCryptScheme oldScheme = new AesGcmCryptScheme();
		AesGcmSivCryptScheme newScheme = new AesGcmSivCryptScheme();

		byte[] legacy = new SchemeCrypt(key, RANDOM, oldScheme, List.of(oldScheme))
			.encrypt("legacy".getBytes(StandardCharsets.UTF_8));

		// during migration: encrypt with the new scheme, still accept the old one for decryption
		SchemeCrypt migrating = new SchemeCrypt(key, RANDOM, newScheme,
			List.of(oldScheme, newScheme));
		assertArrayEquals("legacy".getBytes(StandardCharsets.UTF_8), migrating.decrypt(legacy));

		byte[] upgraded = migrating.encrypt("fresh".getBytes(StandardCharsets.UTF_8));
		assertEquals(newScheme.id(), upgraded[0]);

		// after migration: only the new scheme is whitelisted -> old data is refused
		SchemeCrypt afterMigration = new SchemeCrypt(key, RANDOM, newScheme, List.of(newScheme));
		assertNull(afterMigration.decrypt(legacy));
		assertArrayEquals("fresh".getBytes(StandardCharsets.UTF_8),
			afterMigration.decrypt(upgraded));
	}

	@Test
	void urlSafeRoundTrip()
	{
		SchemeCrypt crypt = crypt(newKey(), new AesGcmCryptScheme());
		String value = "a value with / and + and a space";
		assertEquals(value, crypt.decryptUrlSafe(crypt.encryptUrlSafe(value)));
		assertNull(crypt.decryptUrlSafe("not-valid-ciphertext"));
	}

	@Test
	void urlSafeDeterministicRoundTripIsStable()
	{
		SchemeCrypt crypt = crypt(newKey(), new AesGcmCryptScheme());
		String value = "wicket/bookmarkable/com.example.MyPage";

		String encrypted = crypt.encryptUrlSafeDeterministic(value);
		assertEquals(encrypted, crypt.encryptUrlSafeDeterministic(value));
		assertEquals(value, crypt.decryptUrlSafe(encrypted));
	}
}

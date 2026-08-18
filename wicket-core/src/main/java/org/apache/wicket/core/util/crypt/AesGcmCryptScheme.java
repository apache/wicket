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

import java.security.GeneralSecurityException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;

/**
 * Authenticated encryption using JDK-native AES-256-GCM ({@code AES/GCM/NoPadding}).
 * <p>
 * This is the default scheme: it needs no extra dependencies and provides confidentiality and
 * integrity. The ciphertext layout is {@code nonce(12) || ciphertext || tag(16)}; the scheme
 * marker supplied by {@link SchemeCrypt} is authenticated as associated data.
 * <p>
 * A fresh random 96-bit nonce is generated per message. Because each key is used by a single
 * session (or a single application), the number of encryptions under one key stays far below the
 * NIST SP&nbsp;800-38D safe-usage bound for random nonces. Deployments that need nonce-misuse
 * resistance beyond that bound can switch to {@link AesGcmSivCryptScheme}.
 */
public class AesGcmCryptScheme extends AbstractAesGcmCryptScheme
{
	/** Stable marker id for this scheme. */
	public static final byte ID = 1;

	@Override
	public byte id()
	{
		return ID;
	}

	@Override
	protected Cipher getCipher() throws GeneralSecurityException
	{
		return Cipher.getInstance("AES/GCM/NoPadding");
	}

	@Override
	protected AlgorithmParameterSpec newParameterSpec(byte[] nonce)
	{
		return new GCMParameterSpec(TAG_LENGTH_BITS, nonce);
	}
}

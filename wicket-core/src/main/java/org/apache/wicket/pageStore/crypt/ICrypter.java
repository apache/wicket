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
package org.apache.wicket.pageStore.crypt;

import java.io.Serializable;
import java.security.SecureRandom;

import javax.crypto.SecretKey;

/**
 * An encrypter and decrypter of pages.
 * <p>
 * Implementations are not required to provide <em>authenticated</em> encryption, and callers must
 * not assume that they do. An unauthenticated implementation gives confidentiality only: it hides
 * the contents of a serialized page, but it does not detect modification of the stored bytes, so
 * tampered ciphertext may still be handed to the deserializer. Of the implementations shipped with
 * Wicket, {@link GCMSIVCrypter} is authenticated and {@link DefaultCrypter} - the default - is not.
 * <p>
 * If you implement this interface and want tamper detection, use an AEAD cipher mode (such as
 * GCM, GCM-SIV or CCM) rather than adding encryption on top of an unauthenticated mode. See
 * {@code SECURITY.md} for the trust assumptions Wicket makes about the page store.
 *
 * @see org.apache.wicket.pageStore.CryptingPageStore
 * @see org.apache.wicket.settings.StoreSettings#setCrypter(java.util.function.Supplier)
 */
public interface ICrypter {
	SecretKey generateKey(SecureRandom random);
	
	byte[] encrypt(byte[] bytes, SecretKey key, SecureRandom random);
	
	byte[] decrypt(byte[] bytes, SecretKey key);
}
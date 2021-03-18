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
package org.apache.wicket.util.crypt;

/**
 * Encryption and decryption implementations are accessed through this interface. It provide some
 * simple means to encrypt and decrypt strings, like passwords etc.. It depends on the
 * implementation itself which algorithms are used to en-/decrypt the data.
 * <p>
 * If you value the privacy of your websites users, then please consider using a one-way encryption
 * algorithm instead of the Wicket provided ICrypt implementations. The intention of these
 * encryption facilities is to keep passwords private when stored in cookies or in the session.The
 * implementation of the encryption algorithm may change between releases. As such, this interface
 * and its implementations are not intended and should not be used as an encryption facility for
 * persistent values.
 * <p>
 * As of Wicket 1.2 the methods encrypt and decrypt are deprecated. Consider changing your
 * persistent encryption strategy to be based on a one-way encryption such as a SHA1 hash, not
 * depending on Wicket classes.
 * 
 * @author Juergen Donnerstag
 */
public interface ICrypt
{
	/**
	 * Decrypts a string using URL and filename safe Base64 decoding.
	 * 
	 * @param text
	 *            the text to decrypt
	 * @return the decrypted string.
	 * @since 1.2
	 */
	String decryptUrlSafe(final String text);

	/**
	 * Encrypts a string using URL and filename safe Base64 encoding.
	 * 
	 * @param plainText
	 * @return encrypted string
	 * @since 1.2
	 */
	String encryptUrlSafe(final String plainText);

	/**
	 * Sets private encryption key. It depends on the implementation if a default key is applied or
	 * an exception is thrown, if no private key has been provided.
	 * 
	 * @param key
	 *            private key
	 * 
	 *
     * @deprecated TODO remove in Wicket 10
	 */
	@Deprecated(forRemoval = true)
	void setKey(final String key);
}
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
package org.apache.wicket.authentication;

import org.apache.wicket.Application;

/**
 * The interface of an authentication strategy which is accessible via
 * {@link Application#getSecuritySettings()}. Implementations determine how logon data (username and
 * password) are persisted (e.g. Cookie), retrieved and removed.
 * <p>
 * <strong>This interface is deprecated for security reasons and cannot be made safe.</strong>
 * Whatever it persists is what signs the user in: {@link #load()} hands its result straight to
 * {@code AuthenticatedWebSession#signIn(String, String)}, so what is stored on the client is the
 * password, and it is replayed on every visit for as long as the cookie lives. Wicket's own
 * implementation joins the username and the password with a fixed separator and writes them to a
 * cookie that has a thirty day lifetime and no {@code Secure} attribute, encrypted with
 * {@code PBEWithMD5AndDES} &mdash; DES in an unauthenticated mode &mdash; under a key that is
 * regenerated on every restart. Anyone who obtains that cookie can sign in as the user, and the
 * fixed separator gives them a crib for recovering the password itself.
 * </p>
 * <p>
 * None of that is a defect in the implementation. {@code AuthenticatedWebSession} already says that
 * a cookie based login "may not rely on putting username and password into the cookie but something
 * else that safely identifies the user", and this contract cannot express that: {@link #load()}
 * returns credentials for {@code authenticate(String, String)} to check, so a token can only be
 * carried here by making the application accept that token as a password. There is therefore no
 * replacement and no configuration that makes this safe. An application that needs a persistent
 * login has to implement one itself, with a random, revocable, per-device token, and can sign the
 * session in with {@code AuthenticatedWebSession#signIn(boolean)} once it has verified that token
 * for itself. See {@code SECURITY.md} for the scope this places the interface in.
 * </p>
 * 
 * @author Juergen Donnerstag
 * @deprecated no replacement; see above. Persisting credentials on the client so that a later visit
 *             can replay them cannot be made safe, so this is removed in Wicket 11.
 */
@Deprecated
public interface IAuthenticationStrategy
{
	/**
	 * If "rememberMe" is enabled, then load the saved credentials (e.g. username and password) from the persistence storage
	 * (e.g. Cookie) for automatic sign in. This is useful for applications which users typically
	 * have open the whole day but where the server invalidates the session after a timeout and you
	 * want to force the user to sign in again and again during the day.
	 * 
	 * @return The {@link #save(String, String...) saved} credentials
	 */
	String[] load();

	/**
	 * If "rememberMe" is enabled and login was successful, then store the given credentials in the
	 * persistence store (e.g. Cookie).
	 *
	 * <p>The implementation of this method should be symmetrical with the implementation of
	 * {@link #load()}.</p>
	 *
	 * @param credential
	 *          The credential to store. For example: a security token or username.
	 * @param extraCredentials
	 *          Optional extra credentials. For example: a password
	 */
	void save(final String credential, final String... extraCredentials);

	/**
	 * When the user logs out (session invalidation), than remove username and password from the
	 * persistence store
	 */
	void remove();
}

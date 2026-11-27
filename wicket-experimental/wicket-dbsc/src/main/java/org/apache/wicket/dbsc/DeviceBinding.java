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
package org.apache.wicket.dbsc;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * What a Wicket session knows about the device it is bound to: the public key, the bound cookie
 * values it accepts and the challenges it has handed out.
 */
final class DeviceBinding implements Serializable
{
	private static final long serialVersionUID = 1L;

	private static final int MAX_CHALLENGES = 8;

	private final String sessionIdentifier;

	private final PublicKey publicKey;

	private final String algorithm;

	private final Instant armedAt;

	private boolean armed;

	private Expiring current;

	private Expiring previous;

	private final List<Expiring> challenges = new ArrayList<>();

	/**
	 * @param sessionIdentifier
	 *            the DBSC session identifier, sent by the browser in {@code Sec-Secure-Session-Id}
	 * @param publicKey
	 *            the key proofs have to be signed with
	 * @param algorithm
	 *            the algorithm proofs have to be signed with
	 * @param armedAt
	 *            when a request without a valid bound cookie is rejected even if the browser never
	 *            presented one
	 */
	DeviceBinding(String sessionIdentifier, PublicKey publicKey, String algorithm, Instant armedAt)
	{
		this.sessionIdentifier = sessionIdentifier;
		this.publicKey = publicKey;
		this.algorithm = algorithm;
		this.armedAt = armedAt;
	}

	String getSessionIdentifier()
	{
		return sessionIdentifier;
	}

	PublicKey getPublicKey()
	{
		return publicKey;
	}

	String getAlgorithm()
	{
		return algorithm;
	}

	/**
	 * Issues a new bound cookie value. The one it replaces stays valid until it expires, for the
	 * requests still in flight with it.
	 *
	 * @param value
	 *            the new value
	 * @param validUntil
	 *            until when the value is accepted
	 */
	synchronized void issueCredential(String value, Instant validUntil)
	{
		previous = current;
		current = new Expiring(value, validUntil);
	}

	/**
	 * Checks a bound cookie value. A valid one arms the binding.
	 *
	 * @param value
	 *            the value the request carries, may be {@code null}
	 * @param now
	 *            the current time
	 * @return whether the value is one of the unexpired values issued last
	 */
	synchronized boolean acceptsCredential(String value, Instant now)
	{
		if (matches(current, value, now) || matches(previous, value, now))
		{
			armed = true;
			return true;
		}
		return false;
	}

	/**
	 * @param now
	 *            the current time
	 * @return whether a request without a valid bound cookie has to be rejected
	 */
	synchronized boolean isArmed(Instant now)
	{
		return armed || !now.isBefore(armedAt);
	}

	/**
	 * Remembers a challenge for a later proof.
	 *
	 * @param value
	 *            the challenge
	 * @param validUntil
	 *            until when a proof may answer it
	 */
	synchronized void addChallenge(String value, Instant validUntil)
	{
		if (challenges.size() == MAX_CHALLENGES)
		{
			challenges.remove(0);
		}
		challenges.add(new Expiring(value, validUntil));
	}

	/**
	 * Uses up a challenge. Proofs for older challenges are accepted too, because a browser may
	 * still answer one after a newer one was issued.
	 *
	 * @param value
	 *            the challenge a proof answers
	 * @param now
	 *            the current time
	 * @return whether the challenge was issued, unused and unexpired
	 */
	synchronized boolean consumeChallenge(String value, Instant now)
	{
		challenges.removeIf(challenge -> !now.isBefore(challenge.validUntil()));
		for (Expiring challenge : challenges)
		{
			if (challenge.matches(value, now))
			{
				challenges.remove(challenge);
				return true;
			}
		}
		return false;
	}

	private static boolean matches(Expiring expected, String value, Instant now)
	{
		return expected != null && expected.matches(value, now);
	}

	/**
	 * A value that is accepted until some time.
	 *
	 * @param value
	 *            the value
	 * @param validUntil
	 *            until when it is accepted
	 */
	record Expiring(String value, Instant validUntil) implements Serializable
	{
		/**
		 * @param candidate
		 *            the value to check, may be {@code null}
		 * @param now
		 *            the current time
		 * @return whether {@code candidate} is this value and it has not expired yet
		 */
		boolean matches(String candidate, Instant now)
		{
			return candidate != null && now.isBefore(validUntil) &&
				MessageDigest.isEqual(value.getBytes(StandardCharsets.US_ASCII),
					candidate.getBytes(StandardCharsets.US_ASCII));
		}
	}
}

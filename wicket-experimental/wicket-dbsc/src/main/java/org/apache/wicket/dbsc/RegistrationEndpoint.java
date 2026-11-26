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

import java.time.Instant;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.wicket.Session;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registers the key a browser has created for a session, in answer to the
 * {@code Secure-Session-Registration} header {@link DeviceBoundSessions#start()} sent.
 * <p>
 * The proof's challenge is checked before its key is built and its signature verified, so a
 * client the session did not ask to register cannot make the server do that work.
 */
final class RegistrationEndpoint extends Endpoint
{
	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger(RegistrationEndpoint.class);

	@Override
	void respond(DeviceBoundSessions deviceBoundSessions, WebRequest request, WebResponse response)
	{
		if (!Session.exists())
		{
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return;
		}
		String jwt = StructuredFields
			.parseString(request.getHeader(DeviceBoundSessions.RESPONSE_HEADER));
		if (jwt == null)
		{
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		Instant now = deviceBoundSessions.now();
		Session session = Session.get();
		DbscProof proof;
		try
		{
			proof = DbscProof.parse(jwt);
		}
		catch (DbscProof.InvalidProofException e)
		{
			log.debug("Unusable registration proof", e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		String challenge = proof.challenge();
		if (!isAwaited(session, challenge, now))
		{
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		DeviceBinding binding;
		try
		{
			binding = deviceBoundSessions.newBinding(proof, now);
		}
		catch (DbscProof.InvalidProofException e)
		{
			log.debug("Unusable key in a registration proof", e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		if (!proof.isSignedBy(binding.getPublicKey()))
		{
			log.debug("Registration proof with a signature that does not match its key");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		synchronized (session)
		{
			if (!isAwaited(session, challenge, now))
			{
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				return;
			}
			session.setMetaData(DeviceBoundSessions.PENDING_REGISTRATION, null);
			deviceBoundSessions.issueCredential(binding, response, now);
			session.setMetaData(DeviceBoundSessions.BINDING, binding);
		}
		writeJson(response, deviceBoundSessions.instructions(binding));
	}

	/**
	 * @return whether the session is unbound and waits for a registration that answers the
	 *         challenge
	 */
	private static boolean isAwaited(Session session, String challenge, Instant now)
	{
		synchronized (session)
		{
			DeviceBinding.Expiring pending = session
				.getMetaData(DeviceBoundSessions.PENDING_REGISTRATION);
			return pending != null && pending.matches(challenge, now) &&
				session.getMetaData(DeviceBoundSessions.BINDING) == null;
		}
	}
}

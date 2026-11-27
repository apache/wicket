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
 * Issues a new bound cookie to a browser that proves it holds the session's key.
 * <p>
 * A request without a proof, or with one for an unknown or expired challenge, gets 403 and a new
 * challenge, which the browser signs and sends again. A malformed proof or a wrong signature gets
 * 400, which makes the browser give up the session. A session that is gone or bound to another key
 * gets {@code {"continue": false}}.
 */
final class RefreshEndpoint extends Endpoint
{
	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger(RefreshEndpoint.class);

	@Override
	void respond(DeviceBoundSessions deviceBoundSessions, WebRequest request, WebResponse response)
	{
		String sessionIdentifier = StructuredFields
			.parseString(request.getHeader(DeviceBoundSessions.SESSION_ID_HEADER));
		if (sessionIdentifier == null)
		{
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		Session session = Session.exists() ? Session.get() : null;
		DeviceBinding binding = session == null ? null
			: session.getMetaData(DeviceBoundSessions.BINDING);
		if (binding == null || !binding.getSessionIdentifier().equals(sessionIdentifier))
		{
			terminate(response);
			return;
		}

		Instant now = deviceBoundSessions.now();
		String jwt = StructuredFields
			.parseString(request.getHeader(DeviceBoundSessions.RESPONSE_HEADER));
		if (jwt == null)
		{
			challenge(deviceBoundSessions, session, binding, response, now);
			return;
		}
		DbscProof proof;
		try
		{
			proof = DbscProof.parse(jwt);
		}
		catch (DbscProof.InvalidProofException e)
		{
			log.debug("Unusable refresh proof", e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		if (!proof.algorithm().equals(binding.getAlgorithm()) ||
			!proof.isSignedBy(binding.getPublicKey()))
		{
			log.debug("Refresh proof not signed with the session's key");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		String challenge = proof.challenge();
		if (challenge == null || !binding.consumeChallenge(challenge, now))
		{
			challenge(deviceBoundSessions, session, binding, response, now);
			return;
		}
		deviceBoundSessions.issueCredential(binding, response, now);
		session.dirty();
	}

	private static void challenge(DeviceBoundSessions deviceBoundSessions, Session session,
		DeviceBinding binding, WebResponse response, Instant now)
	{
		deviceBoundSessions.sendChallenge(binding, response, now);
		session.dirty();
		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
	}
}

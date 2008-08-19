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

import java.security.SecureRandom;

import javax.servlet.http.HttpSession;

import org.apache.wicket.RequestCycle;
import org.apache.wicket.protocol.http.WebRequestCycle;

/**
 * Crypt factory that produces {@link SunJceCrypt} instances based on http session-specific
 * encryption key. This allows each user to have their own encryption key, hardening against CSRF
 * attacks.
 * 
 * Note that the use of this crypt factory will result in an immediate creation of a http session
 * 
 * @author igor.vaynberg
 */
public class KeyInSessionSunJceCryptFactory implements ICryptFactory
{
	private static SecureRandom numberGenerator;

	public ICrypt newCrypt()
	{
		WebRequestCycle rc = (WebRequestCycle)RequestCycle.get();

		// get http session, create if necessary
		HttpSession session = rc.getWebRequest().getHttpServletRequest().getSession(true);

		// retrieve or generate encryption key from session
		final String keyAttr = rc.getApplication().getApplicationKey() + "." + getClass().getName();
		String key = (String)session.getAttribute(keyAttr);
		if (key == null)
		{
			// generate new key
			key = session.getId() + "." + randomUUIDString();
			session.setAttribute(keyAttr, key);
		}

		// build the crypt based on session key
		ICrypt crypt = new SunJceCrypt();
		crypt.setKey(key);
		return crypt;
	}

	private static String randomUUIDString()
	{
		SecureRandom ng = numberGenerator;
		if (ng == null)
		{
			numberGenerator = ng = new SecureRandom();
		}

		byte[] randomBytes = new byte[16];
		ng.nextBytes(randomBytes);
		randomBytes[6] &= 0x0f; /* clear version */
		randomBytes[6] |= 0x40; /* set to version 4 */
		randomBytes[8] &= 0x3f; /* clear variant */
		randomBytes[8] |= 0x80; /* set to IETF variant */

		long mostSigBits = 0;
		long leastSigBits = 0;
		for (int i = 0; i < 8; i++)
			mostSigBits = (mostSigBits << 8) | (randomBytes[i] & 0xff);
		for (int i = 8; i < 16; i++)
			leastSigBits = (leastSigBits << 8) | (randomBytes[i] & 0xff);


		return (digits(mostSigBits >> 32, 8) + "-" + digits(mostSigBits >> 16, 4) + "-" +
			digits(mostSigBits, 4) + "-" + digits(leastSigBits >> 48, 4) + "-" + digits(
			leastSigBits, 12));
	}

	private static String digits(long val, int digits)
	{
		long hi = 1L << (digits * 4);
		return Long.toHexString(hi | (val & (hi - 1))).substring(1);
	}
}

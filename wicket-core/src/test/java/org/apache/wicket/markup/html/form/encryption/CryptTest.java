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
package org.apache.wicket.markup.html.form.encryption;

import org.apache.wicket.WicketTestCase;
import org.apache.wicket.util.crypt.ICrypt;
import org.apache.wicket.util.crypt.NoCrypt;
import org.apache.wicket.util.crypt.SunJceCrypt;
import org.junit.Test;


/**
 * @author Juergen Donnerstag
 */
public class CryptTest extends WicketTestCase
{
	@Test
	public void crypt()
	{
		final ICrypt crypt = new SunJceCrypt();
		crypt.setKey("someStableKey");

		try
		{
			if (crypt.encryptUrlSafe("test") != null)
			{
				final String text = "abcdefghijkABC: A test which creates a '/' and/or a '+'";
				final String expectedUrlSafeEncrypted = "xXMS3UMELV--qVINGVFaYaiqUPOtryc_E4x0MyMFgYl-TgTGKxczTzPvwJrE-4YEVMpl-F3eDAg";

				final String encrypted = crypt.encryptUrlSafe(text);
				assertEquals(expectedUrlSafeEncrypted, encrypted);
				assertEquals(text, crypt.decryptUrlSafe(expectedUrlSafeEncrypted));
				assertNull(crypt.decryptUrlSafe("style.css"));
			}
		}
		catch (Exception ex)
		{
			// fails on JVMs without security provider (e.g. seems to be on
			// MAC in US)
		}
	}

	/**
	 * 
	 */
	@Test
	public void noCrypt()
	{
		// The NoCrypt implementation does not modify the string at all
		final ICrypt crypt = new NoCrypt();

		assertEquals("test", crypt.encryptUrlSafe("test"));
		assertEquals("test", crypt.decryptUrlSafe("test"));
	}
}

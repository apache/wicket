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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.wicket.core.util.crypt.ICrypt;
import org.apache.wicket.core.util.crypt.NoCrypt;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 * @author Juergen Donnerstag
 */
class CryptTest extends WicketTestCase
{
	/**
	 * The {@link NoCrypt} implementation does not modify the string at all.
	 */
	@Test
	void noCrypt()
	{
		final ICrypt crypt = new NoCrypt();

		assertEquals("test", crypt.encryptUrlSafe("test"));
		assertEquals("test", crypt.decryptUrlSafe("test"));
	}
}

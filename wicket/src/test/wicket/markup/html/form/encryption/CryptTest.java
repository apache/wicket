/*
 * $Id$
 * $Revision$ $Date$
 * 
 * ======================================================================== 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may 
 * not use this file except in compliance with the License. You may obtain 
 * a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.markup.html.form.encryption;

import junit.framework.TestCase;

/**
 * @author Juergen Donnerstag
 */
public class CryptTest extends TestCase
{
	/**
	 * 
	 */
	public void testNoCrypt()
	{
		// The NoCrypt implementation does not modify the string at all
		final ICrypt crypt = new NoCrypt();

		assertEquals("test", crypt.encryptString("test"));
		assertEquals("test", crypt.decryptString("test"));
	}

	/**
	 * 
	 */
	public void testCrypt()
	{
		final ICrypt crypt = new SunJceCrypt();

		try
		{
			if (crypt.encryptString("test") != null)
			{
				assertEquals("KxMxhk6i4Us=", crypt.encryptString("test"));
				assertEquals("test", crypt.decryptString("KxMxhk6i4Us="));
			}
		}
		catch (Exception ex)
		{
		    // fails on JVMs without security provider (e.g. seems to be on
			// MAC in US)
		}
	}
}

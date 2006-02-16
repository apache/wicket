/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.util.crypt;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.util.Random;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import junit.framework.TestCase;

/**
 * 
 * @author Juergen Donnerstag
 */
public class Base64Test extends TestCase
{
	/**
	 * Construct.
	 * @param name
	 */
	public Base64Test(String name)
	{
		super(name);
	}
	
	/**
	 * @throws IOException
	 */
	public void test_1() throws IOException
	{
	    byte bytes1[] = new byte[200];
	    new Random().nextBytes(bytes1);

	    String s = new BASE64Encoder().encode(bytes1);

	    byte bytes2[] = new BASE64Decoder().decodeBuffer(s);
	    boolean isEqual = ByteBuffer.wrap(bytes1).equals(ByteBuffer.wrap(bytes2) );
	    assertEquals(true, isEqual);
	}
	
	/**
	 * @throws IOException
	 */
	public void test_2() throws IOException
	{
		String input = "This is a text which is longer than 76 character and which contains some none-ascii chars like these: הצüßי?´`=";
	    String s = new NoCrypt().encrypt(input);

	    String output = new NoCrypt().decrypt(s);
	    assertEquals(input, output);
	}
	
	/**
	 * 
	 * @author Juergen Donnerstag
	 */
	public static class NoCrypt extends AbstractCrypt
	{
		/**
		 * Construct.
		 */
		public NoCrypt()
		{
		}
		
		protected byte[] crypt(byte[] input, int mode) throws GeneralSecurityException
		{
			return input;
		}
	}
}

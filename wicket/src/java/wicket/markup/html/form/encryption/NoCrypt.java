/*
 * $Id$ $Revision$
 * $Date$
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
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

import javax.crypto.SecretKey;

/**
 * Due to legal reasons in some countries the JRE is shipped without a security
 * provider. As a convinience solution, we provide a default implementation
 * which does not encrypt/decrypt the data. It does not modify the data at all.
 * Thus we strongly recommend not to use it for production sites.
 * 
 * @author Juergen Donnerstag
 */
public class NoCrypt implements ICrypt
{
	/**
	 * Constructor
	 */
	public NoCrypt()
	{
	}

	/**
	 * Decrypts a string into a string.
	 * 
	 * @param text
	 *            text to decript
	 * @return the decrypted text
	 */
	public final String decryptString(final String text)
	{
		return text;
	}

	/**
	 * Encrypt a string into a string
	 * 
	 * @param plainText
	 *            text to encrypt
	 * @return encrypted string
	 */
	public final String encryptString(final String plainText)
	{
		return plainText;
	}

	/**
	 * Set encryption private key
	 * 
	 * @param key
	 *            private key to make de-/encryption unique
	 */
	public void setKey(final String key)
	{
	}

	/**
	 * Crypts the given byte array
	 * 
	 * @param input
	 *            byte array to be crypted
	 * @param mode
	 *            crypt mode
	 * @return the input crypted. Null in case of an error
	 */
	private final byte[] crypt(final byte[] input, final int mode)
	{
		return input;
	}

	/**
	 * Decrypts a String into a byte array.
	 * 
	 * @param encrypted
	 *            text to decrypt
	 * @return the decrypted text
	 */
	private final byte[] decryptStringToByteArray(final String encrypted)
	{
		return encrypted.getBytes();
	}

	/**
	 * Encrypts the given text into a byte array.
	 * 
	 * @param plainText
	 *            text to encrypt
	 * @return the string encrypted
	 */
	private final byte[] encryptStringToByteArray(final String plainText)
	{
		return plainText.getBytes();
	}

	/**
	 * Generate the de-/encryption key.
	 * <p>
	 * Note: if you don't provide your own encryption key, the implementation
	 * will use a default. Be aware that this is potential security risk. Thus
	 * make sure you always provide your own one.
	 * 
	 * @return secretKey the security key generated
	 */
	private final SecretKey generateKey()
	{
		return null;
	}
}


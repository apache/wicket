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
package wicket.markup.html.form;

import java.io.IOException;
import java.security.*;
import java.security.spec.*;
import javax.crypto.spec.*;
import javax.crypto.*;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * Provide some simple means to encrypt and decrypt strings (e.g. passwords)
 * 
 * @author Juergen Donnerstag
 */
public class Crypt implements ICrypt
{
    /** Name of encryption method */
    private static final String CRYPT_METHOD = "PBEWithMD5AndDES";
    
    /** Salt */
    private final static byte[] salt = { (byte) 0x15, (byte) 0x8c, (byte) 0xa3,
            (byte) 0x4a, (byte) 0x66, (byte) 0x51, (byte) 0x2a, (byte) 0xbc };

    private final static int count = 17;

    // Initialize the provider
    static
    {
        Security.addProvider(new com.sun.crypto.provider.SunJCE());
    }

    /** Key used to de-/encrypt the data */
    private String encryptionKey;
    
    /**
     * Constructor
     */
    public Crypt() 
    {
    }

    /**
     * Set encryption private key
     * 
     * @param key private key to make de-/encryption unique
     */
    public void setKey(final String key) 
    {
        this.encryptionKey = key;
    }
    
    /**
     * Generate the de-/encryption key
     * 
     * @return secretKey
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    private final SecretKey generateKey() throws NoSuchAlgorithmException,
            InvalidKeySpecException
    {
        if (this.encryptionKey == null)
        {
            this.encryptionKey = "WiCkEt-CrYpT";
        }
        
        final PBEKeySpec spec = new PBEKeySpec(this.encryptionKey.toCharArray());
        return SecretKeyFactory.getInstance(CRYPT_METHOD).generateSecret(spec);
    }

    /**
     * Crypts the given byte array
     * @param input byte array to be crypted
     * @param mode crypt mode
     * @return the input crypted
     */
    private final byte[] crypt(final byte[] input, final int mode)
    {
        byte[] result = null;
        try
        {
            SecretKey key = generateKey();
            PBEParameterSpec spec = new PBEParameterSpec(salt, count);
            Cipher ciph = Cipher.getInstance(CRYPT_METHOD);
            ciph.init(mode, key, spec);
            result = ciph.doFinal(input);
        } 
        catch (Exception e)
        {
            return null;
        }
        
        return result;
    }

    /**
     * Encrypts the given text.
     * @param plainText text to encrypt
     * @return the string encrypted
     */
    private final byte[] encryptString(final String plainText)
    {
        return crypt(plainText.getBytes(), Cipher.ENCRYPT_MODE);
    }

    /**
     * Encrypt a string
     * 
     * @param plainText
     * @return encrypted string
     */
    public final String encryptStringToString(final String plainText)
    {
        byte[] cipherText = encryptString(plainText);
        return new BASE64Encoder().encode(cipherText);
    }

    /**
     * Decrypts a String.
     * @param plainText text to decript
     * @return the decrypted text
     */
    private final byte[] decryptString(final String plainText)
    {
        final byte[] plainBytes;
        try
        {
            plainBytes = new BASE64Decoder().decodeBuffer(plainText);
        }
        catch (IOException ex) 
        {
            throw new RuntimeException(ex.getMessage());
        }
        return crypt(plainBytes, Cipher.DECRYPT_MODE);
    }

    /**
     * Decrypts a string.
     * @param text text to decript
     * @return the dycripted text
     */
    public final String decryptStringToString(final String text)
    {
        return new String(decryptString(text));
    }
}
/*
 * $Id: CachingSunJceCryptFactory.java 3623 2006-01-04 10:19:18 +0000 (Wed, 04
 * Jan 2006) jdonnerstag $ $Revision$ $Date: 2006-01-04 10:19:18 +0000
 * (Wed, 04 Jan 2006) $
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.util.crypt;

/**
 * Default crypt factory. this factory will instantiate the object via the
 * provided Class and cache the result so that the object is only instantiated
 * once.
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class CachingSunJceCryptFactory extends CryptFactoryCachingDecorator
{
	/**
	 * Construct.
	 * 
	 * @param encryptionKey
	 *            encryption key
	 */
	public CachingSunJceCryptFactory(String encryptionKey)
	{
		super(new ClassCryptFactory(SunJceCrypt.class, encryptionKey));
	}
}

/*
 * $Id: CryptFactoryCachingDecorator.java,v 1.2 2006/01/04 09:42:04 ivaynberg
 * Exp $ $Revision$ $Date$
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
 * {@link ICryptFactory} decorator that caches the call to
 * {@link ICryptFactory#newCrypt()}
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class CryptFactoryCachingDecorator implements ICryptFactory
{
	private ICryptFactory delegate;
	private ICrypt cache;

	/**
	 * Construct.
	 * 
	 * @param delegate
	 *            the crypt factory whose {@link ICryptFactory#newCrypt()} call
	 *            will be cached
	 */
	public CryptFactoryCachingDecorator(ICryptFactory delegate)
	{
		if (delegate == null)
		{
			throw new IllegalArgumentException("delegate cannot be null");
		}
		this.delegate = delegate;
	}

	/**
	 * @see wicket.util.crypt.ICryptFactory#newCrypt()
	 */
	public final ICrypt newCrypt()
	{
		if (cache == null)
		{
			cache = delegate.newCrypt();
		}
		return cache;
	}
}

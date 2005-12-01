/*
 * $Id$
 * $Revision$
 * $Date$
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
package wicket.request;

import wicket.PageParameters;
import wicket.Request;

/**
 * Encoder responsible for encoding and decoding <code>PageParameters</code>
 * object to and from a url fragment
 * 
 * Url fragment is usually the part of url after the request path.
 * 
 * @see PageParameters
 * @see Request#getPath()
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public interface IPageParamsEncoder
{
	/**
	 * Encodes PageParameters into a url fragment
	 * 
	 * @param parameters
	 *            PageParameters object to be encoded
	 * @return url fragment that represents the parameters
	 */
	String encode(PageParameters parameters);

	/**
	 * Decodes PageParameters object from the provided url fragment
	 * 
	 * @param urlFragment
	 * @return PageParameters object created from the url fragment
	 */
	PageParameters decode(String urlFragment);
}

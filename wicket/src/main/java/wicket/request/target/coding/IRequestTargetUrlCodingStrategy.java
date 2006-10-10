/*
 * $Id: IRequestTargetUrlCodingStrategy.java 5241 2006-04-02 21:09:16 +0000
 * (Sun, 02 Apr 2006) joco01 $ $Revision$ $Date: 2006-04-02 21:09:16
 * +0000 (Sun, 02 Apr 2006) $
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
package wicket.request.target.coding;

import wicket.IRequestTarget;
import wicket.request.RequestParameters;

/**
 * Implementations of this interface know how to encode and decode request
 * targets to/from a URL.
 * 
 * @author Eelco Hillenius
 */
public interface IRequestTargetUrlCodingStrategy
{
	
	/**
	 * Returns the path of the url where this request target is mounted on.
	 * 
	 * @return String The path of the url
	 */
	String getMountPath();
	
	/**
	 * Gets the encoded url for the provided request target. Typically, the
	 * result will be prepended with a protocol specific prefix. In a servlet
	 * environment, the prefix typically is the context-path + servlet path, eg
	 * mywebapp/myservletname.
	 * 
	 * @param requestTarget
	 *            the request target to encode
	 * 
	 * @return the encoded url
	 */
	CharSequence encode(IRequestTarget requestTarget);

	/**
	 * Gets the decoded request target.
	 * 
	 * @param requestParameters
	 *            the request parameters
	 * @return the decoded request target
	 */
	IRequestTarget decode(RequestParameters requestParameters);

	/**
	 * Gets whether this mounter is applicable for the provided request target.
	 * 
	 * @param requestTarget
	 *            the request target
	 * @return whether this mounter is applicable for the provided request
	 *         target
	 */
	boolean matches(IRequestTarget requestTarget);
}
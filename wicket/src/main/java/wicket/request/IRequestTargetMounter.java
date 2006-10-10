/*
 * $Id: IRequestTargetMounter.java 5241 2006-04-02 21:09:16 +0000 (Sun, 02 Apr
 * 2006) joco01 $ $Revision$ $Date: 2006-04-02 21:09:16 +0000 (Sun, 02
 * Apr 2006) $
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

import wicket.IRequestTarget;
import wicket.request.target.coding.IRequestTargetUrlCodingStrategy;

/**
 * Defines the contract for mounting request targets to paths.
 * 
 * @author Eelco Hillenius
 */
public interface IRequestTargetMounter
{
	/**
	 * Mounts a request target with the given path.
	 * 
	 * @param path
	 *            the path to mount the request target with
	 * @param urlCodingStrategy
	 *            The strategy to use for encoding and decoding urls
	 */
	void mount(IRequestTargetUrlCodingStrategy urlCodingStrategy);

	/**
	 * Gets the url that the provided request target conforms to.
	 * 
	 * @param requestTarget
	 *            the request target
	 * @return The url that the provided request target conforms to
	 */
	CharSequence pathForTarget(IRequestTarget requestTarget);

	/**
	 * Gets the request target that conforms to the given request parameters.
	 * 
	 * @param requestParameters
	 *            the request parameters
	 * @return the request target or null if nothing was mounted with the given
	 *         request parameters
	 */
	IRequestTarget targetForRequest(RequestParameters requestParameters);

	/**
	 * Unmounts a request target.
	 * 
	 * @param path
	 *            the path to unmount
	 */
	void unmount(String path);

	/**
	 * Gets the encoder that was mounted on the provided path if any.
	 * 
	 * @param path
	 *            the path
	 * @return The encoder/decoder that was mounted on the provided path, if any
	 */
	IRequestTargetUrlCodingStrategy urlCodingStrategyForPath(String path);
}

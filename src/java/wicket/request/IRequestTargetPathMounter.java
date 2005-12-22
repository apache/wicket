/*
 * $Id$
 * $Revision$ $Date$
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
import wicket.request.target.mixin.IMountEncoder;

/**
 * Defines the contract for mounting request targets to paths.
 * 
 * @author Eelco Hillenius
 */
public interface IRequestTargetPathMounter
{
	/**
	 * Mounts a request target with the given path.
	 * 
	 * @param path
	 *            the path to mount the request target with
	 * @param encoder
	 *            the mount encoder to use for encoding and decoding targets and
	 *            mount paths
	 */
	void mount(String path, IMountEncoder encoder);

	/**
	 * Unmounts a request target.
	 * 
	 * @param path
	 *            the path to unmount
	 */
	void unmount(String path);

	/**
	 * Gets the request target that conforms to the given path.
	 * 
	 * @param path
	 *            the path
	 * @return the request target or null if nothing was mounted with the given
	 *         path
	 */
	IRequestTarget targetForPath(String path);

	/**
	 * Gets the encoder that was mounted on the provided path if any.
	 * 
	 * @param path
	 *            the path
	 * @return the encoder that was mounted on the provided path if any
	 */
	IMountEncoder encoderForPath(String path);

	/**
	 * Gets the path that the provided request target conforms to.
	 * 
	 * @param requestTarget
	 *            the request target
	 * @return the path that the provided request target conforms ti
	 */
	String pathForTarget(IRequestTarget requestTarget);
}

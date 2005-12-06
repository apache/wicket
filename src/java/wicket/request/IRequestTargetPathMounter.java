/*
 * $Id$ $Revision:
 * 1.1 $ $Date$
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
	 * @param requestTarget
	 *            the request target
	 */
	void mountPath(String path, IRequestTarget requestTarget);

	/**
	 * Unmounts a request target.
	 * 
	 * @param path
	 *            the path to unmount
	 */
	void unmountPath(String path);

	/**
	 * Gets the request target that was registered with the given path.
	 * 
	 * @param path
	 *            the path
	 * @return the request target or null if nothing was mounted with the given
	 *         path
	 */
	IRequestTarget targetForPath(String path);

	/**
	 * Gets the path that the provided request target was registered with.
	 * 
	 * @param requestTarget
	 *            the request target
	 * @return the path that the provided request target was registered with
	 */
	String pathForTarget(IRequestTarget requestTarget);
}

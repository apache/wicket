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
import wicket.RequestCycle;
import wicket.Resource;
import wicket.SharedResources;
import wicket.WicketRuntimeException;

/**
 * Target that denotes a shared {@link wicket.Resource}.
 * 
 * @author Eelco Hillenius
 */
public class SharedResourceRequestTarget implements IRequestTarget
{
	/** the key of the resource. */
	private final String resourceKey;

	/** the resource. */
	private final Resource resource;

	/**
	 * Construct.
	 * 
	 * @param resourceKey
	 *            the key of the resource
	 * @param resource
	 *            the resource
	 */
	public SharedResourceRequestTarget(String resourceKey, Resource resource)
	{
		this.resourceKey = resourceKey;
		this.resource = resource;
	}

	/**
	 * Respond by looking up the shared resource and delegating the actual
	 * response to that resource.
	 * 
	 * @see wicket.IRequestTarget#respond(wicket.RequestCycle)
	 */
	public void respond(RequestCycle requestCycle)
	{
		SharedResources sharedResources = requestCycle.getApplication().getSharedResources();
		Resource resource = sharedResources.get(resourceKey);
		if (resource == null)
		{
			throw new WicketRuntimeException("shared resource " + resourceKey + " not found");
		}
		sharedResources.onResourceRequested(resourceKey);
		resource.onResourceRequested();
	}

	/**
	 * Returns null because we don't need to do anything thread safe.
	 * 
	 * @see wicket.IRequestTarget#getSynchronizationLock()
	 */
	public Object getSynchronizationLock()
	{
		return null;
	}

	/**
	 * @see wicket.IRequestTarget#cleanUp(wicket.RequestCycle)
	 */
	public void cleanUp(RequestCycle requestCycle)
	{
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return resourceKey;
	}
}

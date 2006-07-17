/*
 * $Id: MarkupCache.java 4639 2006-02-26 01:44:07 -0800 (Sun, 26 Feb 2006)
 * jdonnerstag $ $Revision$ $Date: 2006-02-26 01:44:07 -0800 (Sun, 26 Feb
 * 2006) $
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
package wicket.markup;

import wicket.util.resource.IResourceStream;

/**
 * Wicket users may may modify a MarkupContainer's markup loading strategy by
 * subclassing {@link wicket.MarkupContainer#newMarkupResourceStream(Class)}.
 * By means of this lookup result class you may influence how the markup stream
 * just loaded is cached. Independently from the Application mode (development
 * or deployment) the markup stream may not be cached at all allowing users to
 * implement there own caching strategy (on a per Component basis; not for the
 * whole Application). Or users may provide they own cache key.
 * 
 * @TODO 2.0 Markup Loading is currently under reconstruction and should cover
 *       the missing pieces mentioned above.
 * 
 * @author Juergen Donnerstag
 */
public class MarkupResourceStreamLookupResult
{
	private IResourceStream resourceStream;

	private CharSequence cacheKey;

	private boolean disableCaching;

	/**
	 * Create a default lookup result which will be cached and reload if
	 * lastModifyTime has changed and which will use the default caching key.
	 * 
	 * @param resourceStream
	 */
	public MarkupResourceStreamLookupResult(final IResourceStream resourceStream)
	{
		this.resourceStream = resourceStream;
		this.cacheKey = null;
		this.disableCaching = false;
	}

	/**
	 * Gets cacheKey.
	 * 
	 * @return cacheKey
	 */
	public final CharSequence getCacheKey()
	{
		return cacheKey;
	}

	/**
	 * Sets cacheKey.
	 * 
	 * @param cacheKey
	 *            cacheKey
	 */
	public final void setCacheKey(final CharSequence cacheKey)
	{
		this.cacheKey = cacheKey;
	}

	/**
	 * Gets disableCaching.
	 * 
	 * @return disableCaching
	 */
	public final boolean isDisableCaching()
	{
		return disableCaching;
	}

	/**
	 * Sets disableCaching.
	 * 
	 * @param disableCaching
	 *            disableCaching
	 */
	public final void setDisableCaching(final boolean disableCaching)
	{
		this.disableCaching = disableCaching;
	}

	/**
	 * Gets resourceStream.
	 * 
	 * @return resourceStream
	 */
	public final IResourceStream getResourceStream()
	{
		return resourceStream;
	}

	/**
	 * Gets markup resource stream.
	 * 
	 * @return resourceStream
	 */
	public final MarkupResourceStream getMarkupResourceStream()
	{
		return (MarkupResourceStream)resourceStream;
	}

	/**
	 * Sets resourceStream.
	 * 
	 * @param resourceStream
	 *            resourceStream
	 */
	public final void setResourceStream(final IResourceStream resourceStream)
	{
		this.resourceStream = resourceStream;
	}
}
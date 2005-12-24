/*
 * $Id: BookmarkablePageRequestTargetEncoderDecoder.java,v 1.1 2005/12/10 21:28:56 eelco12
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
package wicket.request.target.mixin;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import wicket.PageParameters;

/**
 * Abstract class for mount encoders that uses paths and forward slashes.
 * 
 * @author Eelco Hillenius
 */
public abstract class AbstractRequestTargetEncoderDecoder implements IRequestTargetEncoderDecoder
{
	/** mounted path. */
	private final String mountPath;

	/**
	 * Construct.
	 * 
	 * @param mountPath
	 *            the mount path
	 */
	public AbstractRequestTargetEncoderDecoder(final String mountPath)
	{
		if (mountPath == null)
		{
			throw new NullPointerException("argument mountPath must be not null");
		}

		this.mountPath = mountPath;
	}

	/**
	 * Gets path.
	 * 
	 * @return path
	 */
	protected final String getMountPath()
	{
		return mountPath;
	}

	/**
	 * Decodes PageParameters object from the provided url fragment
	 * 
	 * @param urlFragment
	 * @return PageParameters object created from the url fragment
	 */
	protected PageParameters decodePageParameters(String urlFragment)
	{
		PageParameters params = new PageParameters();

		if (urlFragment.startsWith("/"))
		{
			urlFragment = urlFragment.substring(1);
		}

		String[] pairs = urlFragment.split("/");
		// TODO check pairs.length%2==0
		for (int i = 0; i < pairs.length - 1; i += 2)
		{
			params.put(pairs[i], pairs[i + 1]);
		}
		return params;
	}

	/**
	 * Encodes PageParameters into a url fragment and append that to the
	 * provided url buffer.
	 * 
	 * @param url
	 *            url so far
	 * 
	 * @param parameters
	 *            PageParameters object to be encoded
	 */
	protected void appendPageParameters(StringBuffer url, PageParameters parameters)
	{
		if (parameters != null)
		{
			Iterator entries = parameters.entrySet().iterator();
			while (entries.hasNext())
			{
				Map.Entry entry = (Entry)entries.next();
				url.append("/").append(entry.getKey()).append("/").append(entry.getValue());
			}
		}
	}
}

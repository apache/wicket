/*
 * $Id: BookmarkablePageRequestTargetUrlCodingStrategy.java,v 1.1 2005/12/10
 * 21:28:56 eelco12 Exp $ $Revision$ $Date$
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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Application;
import wicket.PageParameters;

/**
 * Abstract class for mount encoders that uses paths and forward slashes.
 * 
 * @author Eelco Hillenius
 * @author Igor Vaynberg (ivaynberg)
 */
public abstract class AbstractRequestTargetUrlCodingStrategy
		implements
			IRequestTargetUrlCodingStrategy
{
	/** log. */
	private static Log log = LogFactory.getLog(AbstractRequestTargetUrlCodingStrategy.class);

	
	/** mounted path. */
	private final String mountPath;

	/**
	 * Construct.
	 * 
	 * @param mountPath
	 *            the mount path
	 */
	public AbstractRequestTargetUrlCodingStrategy(final String mountPath)
	{
		if (mountPath == null)
		{
			throw new IllegalArgumentException("Argument mountPath must be not null");
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
		// Hack off any leading slash
		if (urlFragment.startsWith("/"))
		{
			urlFragment = urlFragment.substring(1);
		}

		if (urlFragment.length()==0) {
			return new PageParameters();
		}
		
		// Split into pairs
		final String[] pairs = urlFragment.split("/");

		// If we don't have an even number of pairs
		if (pairs.length % 2 != 0)
		{
			// give up
			throw new IllegalStateException("URL fragment has unmatched key/value pair: "
					+ urlFragment);
		}

		// Loop through pairs
		PageParameters parameters = new PageParameters();
		for (int i = 0; i < pairs.length; i += 2)
		{
			parameters.put(pairs[i], pairs[i + 1]);
		}
		return parameters;
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
				String escapedValue=(String)entry.getValue();
				try
				{
					escapedValue=URLEncoder.encode(escapedValue, Application.get()
							.getRequestCycleSettings().getResponseRequestEncoding());
				}
				catch (UnsupportedEncodingException e)
				{
					log.error(e.getMessage(), e);
				}
				url.append("/").append(entry.getKey()).append("/").append(escapedValue);
			}
		}
	}
}

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
package wicket.request.target.mixin;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import wicket.IRequestTarget;
import wicket.PageParameters;
import wicket.request.IBookmarkablePageRequestTarget;
import wicket.request.target.BookmarkablePageRequestTarget;

/**
 * Encodes and decodes mounts for a single bookmarkable page class.
 * 
 * @author Eelco Hillenius
 */
public class BookmarkablePagePathMountEncoder implements IMountEncoder
{
	/** mounted path. */
	private final String mountPath;

	/** bookmarkable page class. */
	private final Class bookmarkablePageClass;

	/** page map name. */
	private final String pageMapName;

	/**
	 * Construct.
	 * 
	 * @param mountPath
	 *            the mount path
	 * @param bookmarkablePageClass
	 *            the class of the bookmarkable page
	 * @param pageMapName
	 *            the page map name if any
	 */
	public BookmarkablePagePathMountEncoder(final String mountPath,
			final Class bookmarkablePageClass, String pageMapName)
	{
		if (mountPath == null)
		{
			throw new NullPointerException("argument mountPath must be not null");
		}
		if (bookmarkablePageClass == null)
		{
			throw new NullPointerException("argument bookmarkablePageClass must be not null");
		}

		this.mountPath = mountPath;
		this.bookmarkablePageClass = bookmarkablePageClass;
		this.pageMapName = pageMapName;
	}

	/**
	 * @see wicket.request.target.mixin.IMountEncoder#encode(wicket.IRequestTarget)
	 */
	public final String encode(IRequestTarget requestTarget)
	{
		if (!(requestTarget instanceof IBookmarkablePageRequestTarget))
		{
			throw new IllegalArgumentException("this encoder can only be used with instances of "
					+ IBookmarkablePageRequestTarget.class.getName());
		}
		StringBuffer url = new StringBuffer();
		url.append(getMountPath());
		IBookmarkablePageRequestTarget target = (IBookmarkablePageRequestTarget)requestTarget;
		// TODO what to do with the page map?
		appendPageParameters(url, target.getPageParameters());
		return url.toString();
	}

	/**
	 * @see wicket.request.target.mixin.IMountEncoder#decode(java.lang.String)
	 */
	public IRequestTarget decode(String urlFragment)
	{
		String parametersFragment = urlFragment.substring(mountPath.length());
		PageParameters parameters = decodePageParameters(parametersFragment);
		BookmarkablePageRequestTarget target = new BookmarkablePageRequestTarget(
				bookmarkablePageClass, parameters);
		return target;
	}

	/**
	 * @see wicket.request.target.mixin.IMountEncoder#matches(wicket.IRequestTarget)
	 */
	public boolean matches(IRequestTarget requestTarget)
	{
		if (requestTarget instanceof IBookmarkablePageRequestTarget)
		{
			IBookmarkablePageRequestTarget target = (IBookmarkablePageRequestTarget)requestTarget;
			if (bookmarkablePageClass.equals(target.getPageClass()))
			{
				if (this.pageMapName == null)
				{
					return target.getPageMapName() == null;
				}
				else
				{
					return this.pageMapName.equals(target.getPageMapName());
				}
			}
		}
		return false;
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

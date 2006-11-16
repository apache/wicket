/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.protocol.http.request;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import wicket.IRequestTarget;
import wicket.RequestCycle;
import wicket.WicketRuntimeException;
import wicket.protocol.http.WebApplication;
import wicket.request.IRequestTargetMountsInfo;
import wicket.request.target.coding.IRequestTargetUrlCodingStrategy;
import wicket.request.target.component.IBookmarkablePageRequestTarget;
import wicket.request.target.component.IPageRequestTarget;
import wicket.request.target.component.listener.IListenerInterfaceRequestTarget;
import wicket.request.target.resource.ISharedResourceRequestTarget;
import wicket.util.string.AppendingStringBuffer;
import wicket.util.string.Strings;

/**
 * Request parameters factory implementation that uses http request parameters
 * and path info to construct the request parameters object.
 * 
 * @author Eelco Hillenius
 * @author Jonathan Locke
 */
public class WebRequestCodingStrategy extends AbstractWebRequestCodingStrategy
		implements
			IRequestTargetMountsInfo
{
	/**
	 * map of path mounts for mount encoders on paths.
	 * <p>
	 * mountsOnPath is sorted by longest paths first to improve resolution of
	 * possible path conflicts. <br />
	 * For example: <br/> we mount Page1 on /page and Page2 on /page/test <br />
	 * Page1 uses a parameters encoder that only encodes parameter values <br />
	 * now suppose we want to access Page1 with a single paramter param="test".
	 * we have a url collision since both pages can be access with /page/test
	 * <br />
	 * the sorting by longest path first guarantees that the iterator will
	 * return the mount /page/test before it returns mount /page therefore
	 * giving deterministic behavior to path resolution by always trying to
	 * match the longest possible path first.
	 * </p>
	 */
	private final MountsMap mountsOnPath;

	/** cached url prefix. */
	private CharSequence urlPrefix;

	/** settings for the coding strategy */
	private final Settings settings;

	/**
	 * Various settings used to configure this strategy
	 * 
	 * @author ivaynberg
	 */
	public static class Settings
	{
		/** whether or not mount paths are case sensitive */
		private boolean mountsCaseSensitive = true;

		/**
		 * Sets mountsCaseSensitive.
		 * 
		 * @param mountsCaseSensitive
		 *            mountsCaseSensitive
		 */
		public void setMountsCaseSensitive(boolean mountsCaseSensitive)
		{
			this.mountsCaseSensitive = mountsCaseSensitive;
		}

		/**
		 * Gets caseSensitive.
		 * 
		 * @return caseSensitive
		 */
		public boolean areMountsCaseSensitive()
		{
			return mountsCaseSensitive;
		}
	}


	/**
	 * Construct.
	 */
	public WebRequestCodingStrategy()
	{
		this(new Settings());
	}

	/**
	 * Construct.
	 * 
	 * @param settings
	 *            settings to use for the coding strategy
	 */
	public WebRequestCodingStrategy(Settings settings)
	{
		this.settings = settings;
		mountsOnPath = new MountsMap(settings.areMountsCaseSensitive());
	}

	/**
	 * Encode the given request target. If a mount is found, that mounted url
	 * will be returned. Otherwise, one of the delegation methods will be
	 * called. In case you are using custom targets that are not part of the
	 * default target hierarchy, you need to override
	 * {@link #doEncode(RequestCycle, IRequestTarget)}, which will be called
	 * after the defaults have been tried. When that doesn't provide a url
	 * either, and exception will be thrown saying that encoding could not be
	 * done.
	 * 
	 * @see wicket.request.IRequestCodingStrategy#encode(wicket.RequestCycle,
	 *      wicket.IRequestTarget)
	 */
	public final CharSequence encode(final RequestCycle requestCycle,
			final IRequestTarget requestTarget)
	{
		// first check whether the target was mounted
		CharSequence path = pathForTarget(requestTarget);
		if (path != null)
		{
			CharSequence prefix = urlPrefix(requestCycle);
			// TODO has this to be done in 2.0?? special check if the prefix
			// ends on '/' because a mount always starts with '/'
			// because rootPath see WicketServlet will always contain a "/" and
			// context path and filterpath are both ""
			if (prefix.charAt(prefix.length() - 1) == '/')
				prefix = prefix.subSequence(0, prefix.length() - 1);
			final AppendingStringBuffer buffer = new AppendingStringBuffer(prefix.length()
					+ path.length());
			buffer.append(prefix);
			buffer.append(path);
			return requestCycle.getOriginalResponse().encodeURL(buffer);
		}

		// no mount found; go on with default processing
		if (requestTarget instanceof IBookmarkablePageRequestTarget)
		{
			return encode(requestCycle, (IBookmarkablePageRequestTarget)requestTarget);
		}
		else if (requestTarget instanceof ISharedResourceRequestTarget)
		{
			return encode(requestCycle, (ISharedResourceRequestTarget)requestTarget);
		}
		else if (requestTarget instanceof IListenerInterfaceRequestTarget)
		{
			return encode(requestCycle, (IListenerInterfaceRequestTarget)requestTarget);
		}
		else if (requestTarget instanceof IPageRequestTarget)
		{
			return encode(requestCycle, (IPageRequestTarget)requestTarget);
		}

		// fallthough for non-default request targets
		String url = doEncode(requestCycle, requestTarget);
		if (url != null)
		{
			return url;
		}

		// Just return null intead of throwing an exception. So that it can be
		// handled better
		return null;
	}

	/**
	 * @see wicket.request.IRequestTargetMountsInfo#listMounts()
	 */
	public IRequestTargetUrlCodingStrategy[] listMounts()
	{
		return (IRequestTargetUrlCodingStrategy[])mountsOnPath.strategies().toArray(
				new IRequestTargetUrlCodingStrategy[mountsOnPath.size()]);
	}

	/**
	 * @see wicket.request.IRequestTargetMounter#mount(
	 *      wicket.request.target.coding.IRequestTargetUrlCodingStrategy)
	 */
	public final void mount(IRequestTargetUrlCodingStrategy encoder)
	{
		if (encoder == null)
		{
			throw new IllegalArgumentException("Argument encoder must be not-null");
		}

		String path = encoder.getMountPath();
		if (Strings.isEmpty(path))
		{
			throw new IllegalArgumentException("Argument path must be not be empty");
		}
		if (path.equals("/"))
		{
			throw new IllegalArgumentException(
					"The mount path '/' is reserved for the application home page");
		}

		// sanity check
		if (!path.startsWith("/"))
		{
			path = "/" + path;
		}

		if (mountsOnPath.strategyForMount(path) != null)
		{
			throw new WicketRuntimeException(path + " is already mounted for "
					+ mountsOnPath.strategyForMount(path));
		}
		mountsOnPath.mount(path, encoder);
	}

	/**
	 * @see wicket.request.IRequestCodingStrategy#pathForTarget(wicket.IRequestTarget)
	 */
	public final CharSequence pathForTarget(IRequestTarget requestTarget)
	{
		// first check whether the target was mounted
		IRequestTargetUrlCodingStrategy encoder = getMountEncoder(requestTarget);
		if (encoder != null)
		{
			return encoder.encode(requestTarget);
		}
		return null;
	}

	/**
	 * @see wicket.request.IRequestCodingStrategy#unmount(java.lang.String)
	 */
	public final void unmount(String path)
	{
		if (path == null)
		{
			throw new IllegalArgumentException("Argument path must be not-null");
		}

		// sanity check
		if (!path.startsWith("/"))
		{
			path = "/" + path;
		}

		mountsOnPath.unmount(path);
	}

	/**
	 * @see wicket.request.IRequestTargetMounter#urlCodingStrategyForPath(java.lang.String)
	 */
	public final IRequestTargetUrlCodingStrategy urlCodingStrategyForPath(String path)
	{
		if (path == null)
		{
			return mountsOnPath.strategyForMount(null);
		}
		else if (!path.equals("/")) // ignore root paths.. is this the right
		// path?
		{
			IRequestTargetUrlCodingStrategy strat = mountsOnPath.strategyForPath(path);
			if (strat != null)
			{
				return strat;
			}
		}
		return null;
	}

	/**
	 * Gets the mount encoder for the given request target if any.
	 * 
	 * @param requestTarget
	 *            the request target to match
	 * @return the mount encoder if any
	 */
	protected IRequestTargetUrlCodingStrategy getMountEncoder(IRequestTarget requestTarget)
	{
		// TODO Post 1.2: Performance: Optimize algorithm if possible and/ or
		// cache lookup results
		for (IRequestTargetUrlCodingStrategy encoder : mountsOnPath.strategies())
		{
			if (encoder.matches(requestTarget))
			{
				return encoder;
			}
		}

		return null;
	}

	/**
	 * Gets prefix.
	 * 
	 * @param requestCycle
	 *            the request cycle
	 * 
	 * @return prefix
	 */
	@Override
	protected final CharSequence urlPrefix(final RequestCycle requestCycle)
	{
		if (urlPrefix == null)
		{
			urlPrefix = WebApplication.get().getRootPath();
		}
		return urlPrefix;
	}

	/**
	 * Map used to store mount paths and their corresponding url coding
	 * strategies.
	 * 
	 * @author ivaynberg
	 */
	private static class MountsMap
	{
		private static final long serialVersionUID = 1L;

		/** case sensitive flag */
		private final boolean caseSensitiveMounts;

		/** backing map */
		private final TreeMap<String, IRequestTargetUrlCodingStrategy> map;

		/**
		 * Constructor
		 * 
		 * @param caseSensitiveMounts
		 *            whether or not keys of this map are case-sensitive
		 */
		public MountsMap(boolean caseSensitiveMounts)
		{
			map = new TreeMap<String, IRequestTargetUrlCodingStrategy>(LENGTH_COMPARATOR);
			this.caseSensitiveMounts = caseSensitiveMounts;
		}

		/**
		 * Checks if the specified path matches any mount, and if so returns the
		 * coding strategy for that mount. Returns null if the path doesnt match
		 * any mounts.
		 * 
		 * NOTE: path here is not the mount - it is the full url path
		 * 
		 * @param path
		 *            non-null url path
		 * @return coding strategy or null
		 */
		public IRequestTargetUrlCodingStrategy strategyForPath(String path)
		{
			if (path == null)
			{
				throw new IllegalArgumentException("Argument [[path]] cannot be null");
			}
			if (caseSensitiveMounts == false)
			{
				path = path.toLowerCase();
			}
			for (final Iterator it = map.entrySet().iterator(); it.hasNext();)
			{
				final Map.Entry entry = (Entry)it.next();
				final String key = (String)entry.getKey();
				if (path.startsWith(key))
				{
					return (IRequestTargetUrlCodingStrategy)entry.getValue();
				}
			}
			return null;
		}


		/**
		 * @return number of mounts in the map
		 */
		public int size()
		{
			return map.size();
		}

		/**
		 * @return collection of coding strategies associated with every mount
		 */
		public Collection<IRequestTargetUrlCodingStrategy> strategies()
		{
			return map.values();
		}


		/**
		 * Removes mount from the map
		 * 
		 * @param mount
		 */
		public void unmount(String mount)
		{
			if (caseSensitiveMounts == false && mount != null)
			{
				mount = mount.toLowerCase();
			}

			map.remove(mount);
		}


		/**
		 * Gets the coding strategy for the specified mount path
		 * 
		 * @param mount
		 *            mount paht
		 * @return associated coding strategy or null if none
		 */
		public IRequestTargetUrlCodingStrategy strategyForMount(String mount)
		{
			if (caseSensitiveMounts == false && mount != null)
			{
				mount = mount.toLowerCase();
			}

			return (IRequestTargetUrlCodingStrategy)map.get(mount);
		}

		/**
		 * Associates a mount with a coding strategy
		 * 
		 * @param mount
		 * @param encoder
		 * @return previous coding strategy associated with the mount, or null
		 *         if none
		 */
		public IRequestTargetUrlCodingStrategy mount(String mount,
				IRequestTargetUrlCodingStrategy encoder)
		{
			if (caseSensitiveMounts == false && mount != null)
			{
				mount = mount.toLowerCase();
			}
			return (IRequestTargetUrlCodingStrategy)map.put(mount, encoder);
		}


		/** Comparator implementation that sorts longest strings first */
		private static final Comparator<String> LENGTH_COMPARATOR = new Comparator<String>()
		{
			public int compare(String o1, String o2)
			{
				if (o1 == o2)
				{
					return 0;
				}
				else if (o1 == null)
				{
					return 1;
				}
				else if (o2 == null)
				{
					return -1;
				}
				else
				{
					return o2.compareTo(o1);
				}
			}
		};

	}
}

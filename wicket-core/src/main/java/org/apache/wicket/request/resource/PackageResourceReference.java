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
package org.apache.wicket.request.resource;

import static org.apache.wicket.util.resource.ResourceUtils.MIN_POSTFIX_DEFAULT_AS_EXTENSION;

import java.util.Locale;
import java.util.concurrent.ConcurrentMap;

import org.apache.wicket.Application;
import org.apache.wicket.Session;
import org.apache.wicket.core.util.resource.locator.IResourceStreamLocator;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.resource.ResourceUtil;
import org.apache.wicket.util.lang.Generics;
import org.apache.wicket.util.lang.Packages;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a ResourceReference that knows how to find and serve resources located in the Java
 * package (i.e. next to the class files).
 * 
 * @author Tobias Soloschenko
 */
public class PackageResourceReference extends ResourceReference
{
	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger(PackageResourceReference.class);

	private static final String CSS_EXTENSION = "css";
	private static final String JAVASCRIPT_EXTENSION = "js";

	private transient ConcurrentMap<UrlAttributes, UrlAttributes> urlAttributesCacheMap;

	/**
	 * Reads the resource buffered - the content is copied into memory
	 */
	private boolean readBuffered = true;

	/**
	 * Construct.
	 * 
	 * @param key
	 */
	public PackageResourceReference(final ResourceReference.Key key)
	{
		super(key);
	}

	/**
	 * Construct.
	 * 
	 * @param scope
	 * @param name
	 * @param locale
	 * @param style
	 * @param variation
	 */
	public PackageResourceReference(final Class<?> scope, final String name, final Locale locale,
		final String style, String variation)
	{
		super(scope, name, locale, style, variation);
	}

	/**
	 * Construct.
	 * 
	 * @param scope
	 * @param name
	 */
	public PackageResourceReference(final Class<?> scope, final String name)
	{
		super(scope, name);
	}

	/**
	 * Construct.
	 * 
	 * @param name
	 */
	public PackageResourceReference(final String name)
	{
		super(name);
	}

	/**
	 * @see org.apache.wicket.request.resource.ResourceReference#getResource()
	 */
	@Override
	public PackageResource getResource()
	{
		final String extension = getExtension();

		final PackageResource resource;
		
		final Url url = RequestCycle.get().getRequest().getUrl();
		//resource attributes (locale, style, variation) might be encoded in the URL
		final UrlAttributes urlAttributes = ResourceUtil.decodeResourceReferenceAttributes(url);
		final String currentVariation = getCurrentVariation(urlAttributes);
		final String currentStyle = getCurrentStyle(urlAttributes);
		final Locale currentLocale = getCurrentLocale(urlAttributes);
		final Class<?> scope = getScope();
		final String name = getName();

		if (CSS_EXTENSION.equals(extension))
		{
			resource = new CssPackageResource(scope, name, currentLocale,
					currentStyle, currentVariation);
		}
		else if (JAVASCRIPT_EXTENSION.equals(extension))
		{
			resource = new JavaScriptPackageResource(scope, name, currentLocale,
					currentStyle, currentVariation);
		}
		else
		{
			resource = new PackageResource(scope, name, currentLocale,
					currentStyle, currentVariation);
		}
		resource.readBuffered(readBuffered);

		removeCompressFlagIfUnnecessary(resource);

		return resource;
	}

	/**
	 * Method allowing to remove the compress flag if the resource has been detected as a minified
	 * one (i.e. ending with .min.EXT) This method is to be called by subclasses overriding
	 * <code>getResource</code> if they want to rely on default minification detection handling
	 *
	 * see WICKET-5250 for further explanation
	 * 
	 * @param resource
	 *            resource to check
	 */
	protected final void removeCompressFlagIfUnnecessary(final PackageResource resource)
	{
		String minifiedName = getName();
		if (minifiedName != null && minifiedName.contains(MIN_POSTFIX_DEFAULT_AS_EXTENSION))
		{
			resource.setCompress(false);
		}
	}

	private ResourceReference.UrlAttributes getUrlAttributes(Locale locale, String style,
		String variation)
	{
		IResourceStreamLocator locator = Application.get()
			.getResourceSettings()
			.getResourceStreamLocator();

		String absolutePath = Packages.absolutePath(getScope(), getName());

		IResourceStream stream = locator.locate(getScope(), absolutePath, style, variation, locale,
			null, false);

		if (stream == null)
			return new ResourceReference.UrlAttributes(null, null, null);

		return new ResourceReference.UrlAttributes(stream.getLocale(), stream.getStyle(),
			stream.getVariation());
	}

	private Locale getCurrentLocale(UrlAttributes attributes)
	{
		Locale currentLocale = getCurrentLocale();

		return currentLocale != null ? currentLocale : attributes.getLocale();
	}

	private Locale getCurrentLocale()
	{
		final Locale locale = getLocale();

		if (locale != null)
		{
			return locale;
		}

		if (Session.exists())
		{
			return Session.get().getLocale();
		}

		return locale;
	}

	private String getCurrentStyle(UrlAttributes attributes)
	{
		String currentStyle = getCurrentStyle();
		
		return currentStyle != null ? currentStyle : attributes.getStyle();
	}
	
	private String getCurrentStyle()
	{
		final String style = getStyle();

		if (style != null)
		{
			return style;
		}

		if (Session.exists())
		{
			return Session.get().getStyle();
		}

		return style;
	}
	
	private String getCurrentVariation(UrlAttributes attributes)
	{
		final String variation = getVariation();

		return variation != null ? variation : attributes.getVariation();
	}

	/**
	 * @return How the minified file should be named.
	 */
	protected String getMinifiedName()
	{
		String name = super.getName();
		return ResourceUtils.getMinifiedName(name, ResourceUtils.MIN_POSTFIX_DEFAULT);
	}

	@Override
	public ResourceReference.UrlAttributes getUrlAttributes()
	{
		Locale locale = getCurrentLocale();
		String style = getCurrentStyle();
		String variation = getVariation();

		ResourceReference.UrlAttributes key = new ResourceReference.UrlAttributes(locale, style,
			variation);

		if (urlAttributesCacheMap == null)
		{
			urlAttributesCacheMap = Generics.newConcurrentHashMap();
		}
		ResourceReference.UrlAttributes value = urlAttributesCacheMap.get(key);
		if (value == null)
		{
			value = getUrlAttributes(locale, style, variation);
			UrlAttributes tmpValue = urlAttributesCacheMap.putIfAbsent(key, value);
			if (tmpValue != null)
			{
				value = tmpValue;
			}
		}

		return value;
	}

	/**
	 * If the package resource should be read buffered.<br>
	 * <br>
	 * WARNING - if the stream is not read buffered compressors will not work, because they require the
	 * whole content to be read into memory.<br>
	 * ({@link org.apache.wicket.javascript.IJavaScriptCompressor}, <br>
	 * {@link org.apache.wicket.css.ICssCompressor}, <br>
	 * {@link org.apache.wicket.resource.IScopeAwareTextResourceProcessor})
	 * 
	 * @param readBuffered
	 *            if the package resource should be read buffered
	 * @return the current package resource
	 */
	public PackageResourceReference readBuffered(boolean readBuffered)
	{
		this.readBuffered = readBuffered;
		return this;
	}
}

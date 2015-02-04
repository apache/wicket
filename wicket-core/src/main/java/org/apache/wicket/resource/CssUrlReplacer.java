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
package org.apache.wicket.resource;

import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.application.IComponentInitializationListener;
import org.apache.wicket.core.util.lang.WicketObjects;
import org.apache.wicket.css.ICssCompressor;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.PackageResourceReference;

/**
 * This compressor is used to replace url within css files with resources that belongs to their
 * corresponding component classes. The compress method is not compressing any content, but replacing the
 * URLs with Wicket representatives.<br>
 * <br>
 * Usage:
 * 
 * <pre>
 * this.getResourceSettings().setCssCompressor(new CssUrlReplacementCompressor(this));
 * </pre>
 * 
 * @since 6.20.0
 * @author Tobias Soloschenko
 * 
 */
public class CssUrlReplacer implements ICssCompressor
{

	// Holds the names of components
	private final Map<String, String> componentNames = Collections.synchronizedMap(new LinkedHashMap<String, String>());

	// The pattern to find URLs in CSS resources
	private static final Pattern urlPattern = Pattern.compile("url\\(['|\"]*(.*)['|\"]*\\)");

	public CssUrlReplacer(Application application)
	{

		// Create an instantiation listener to detect components
		application.getComponentInitializationListeners().add(
			new IComponentInitializationListener()
			{

				@Override
				public void onInitialize(Component component)
				{
					CssUrlReplacer.this.componentNames.put(component.getClass().getName(),
						component.getClass().getSimpleName());
				}
			});
	}

	/**
	 * Replaces the URLs of CSS resources with Wicket representatives.
	 */
	@Override
	public String compress(String original)
	{
		Matcher matcher = CssUrlReplacer.urlPattern.matcher(original);
		// Search for urls
		while (matcher.find())
		{
			Collection<String> componentNames = this.componentNames.keySet();
			for (String componentName : componentNames)
			{
				try
				{
					Class<?> componentClass = WicketObjects.resolveClass(componentName);
					String url = matcher.group(1);
					if (!url.contains("/"))
					{
						URL urlResource = componentClass.getResource(url);
						// If the resource is not found skip it
						if (urlResource != null)
						{
							PackageResourceReference packageResourceReference = new PackageResourceReference(
								componentClass, url);
							String replacedUrl = RequestCycle.get()
								.urlFor(packageResourceReference, null)
								.toString();
							StringBuilder urlBuilder = new StringBuilder();
							urlBuilder.append("url('");
							urlBuilder.append(replacedUrl);
							urlBuilder.append("')");
							original = matcher.replaceFirst(urlBuilder.toString());
						}
					}
				}
				catch (Exception e)
				{
					throw new WicketRuntimeException(
						"A problem occurred during CSS url replacement.", e);
				}
			}

		}
		return original;
	}
}

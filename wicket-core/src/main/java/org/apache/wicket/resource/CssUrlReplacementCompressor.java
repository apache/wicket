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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.application.IComponentInitializationListener;
import org.apache.wicket.css.ICssCompressor;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.PackageResourceReference;

/**
 * This compressor is used to replace url within css files with resources that belongs to their
 * corresponding page classes. The compress method is not compressing any content, but replacing the
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
public class CssUrlReplacementCompressor implements ICssCompressor
{

	// Holds the names of pages
	private Map<String, String> pageNames = Collections.synchronizedMap(new LinkedHashMap<String, String>());

	// The pattern to find URLs in CSS resources
	private Pattern urlPattern = Pattern.compile("url\\(['|\"](.*)['|\"]\\)");

	public CssUrlReplacementCompressor(Application application)
	{

		// Create an instantiation listener which filters only pages.
		application.getComponentInitializationListeners().add(
			new IComponentInitializationListener()
			{

				@Override
				public void onInitialize(Component component)
				{
					if (Page.class.isAssignableFrom(component.getClass()))
					{
						CssUrlReplacementCompressor.this.pageNames.put(component.getClass()
							.getName(), component.getClass().getSimpleName());
					}
				}
			});
	}

	/**
	 * Replaces the URLs of CSS resources with Wicket representatives.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public String compress(String original)
	{
		Matcher matcher = this.urlPattern.matcher(original);
		// Search for urls
		while (matcher.find())
		{
			Collection<String> pageNames = this.pageNames.keySet();
			for (String pageName : pageNames)
			{
				try
				{
					Class<Page> pageClass = (Class<Page>)Class.forName(pageName);
					String url = matcher.group(1);
					if (!url.contains("/"))
					{
						URL urlResource = pageClass.getResource(url);
						// If the resource is not found for a page skip it
						if (urlResource != null)
						{
							PackageResourceReference packageResourceReference = new PackageResourceReference(
								pageClass, url);
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
					StringWriter stringWriter = this.printStack(e);
					throw new WicketRuntimeException(stringWriter.toString());
				}
			}

		}
		return original;
	}

	/**
	 * Prints the stack trace to a print writer
	 * 
	 * @param exception
	 *            the exception
	 * @return the string writer containing the stack trace
	 */
	private StringWriter printStack(Exception exception)
	{
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		exception.printStackTrace(printWriter);
		return stringWriter;
	}
}

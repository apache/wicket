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
package org.apache.wicket.examples.repeater;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.regex.Pattern;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.core.util.resource.UrlResourceStream;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.request.resource.ResourceStreamResource;

/**
 * Serves the style sheet and the fonts of the Font Awesome web jar, so the page can show
 * {@link org.apache.wicket.extensions.markup.html.icon.FontAwesomeIcon}s. The application mounts it
 * at {@value #PATH}; the style sheet finds the fonts next to it.
 */
class FontAwesomeResourceReference extends ResourceReference
{
	private static final long serialVersionUID = 1L;

	static final String PATH = "fontawesome";

	static final FontAwesomeResourceReference INSTANCE = new FontAwesomeResourceReference();

	private static final String WEBJAR = "META-INF/resources/webjars/fortawesome__fontawesome-free/";

	private static final Pattern FILES = Pattern
		.compile("css/all\\.min\\.css|webfonts/[a-z0-9-]+\\.woff2");

	private static final String VERSION = version();

	private FontAwesomeResourceReference()
	{
		super(FontAwesomeResourceReference.class, PATH);
	}

	/**
	 * @return the header item of the Font Awesome style sheet
	 */
	static CssHeaderItem styleSheet()
	{
		return CssHeaderItem.forReference(INSTANCE,
			new PageParameters().set(0, "css").set(1, "all.min.css"), null);
	}

	@Override
	public IResource getResource()
	{
		return attributes -> {
			PageParameters parameters = attributes.getParameters();
			String file = parameters.getIndexedCount() == 2
				? parameters.get(0) + "/" + parameters.get(1) : "";
			URL url = FILES.matcher(file).matches() ? FontAwesomeResourceReference.class
				.getClassLoader().getResource(WEBJAR + VERSION + "/" + file) : null;
			if (url == null)
			{
				((WebResponse)attributes.getResponse()).sendError(
					HttpServletResponse.SC_NOT_FOUND, null);
				return;
			}
			new ResourceStreamResource(new UrlResourceStream(url)).respond(attributes);
		};
	}

	private static String version()
	{
		try (InputStream in = FontAwesomeResourceReference.class.getClassLoader()
			.getResourceAsStream(
				"META-INF/maven/org.webjars.npm/fortawesome__fontawesome-free/pom.properties"))
		{
			Properties properties = new Properties();
			properties.load(in);
			return properties.getProperty("version");
		}
		catch (IOException | RuntimeException e)
		{
			throw new WicketRuntimeException("The Font Awesome web jar is missing", e);
		}
	}
}

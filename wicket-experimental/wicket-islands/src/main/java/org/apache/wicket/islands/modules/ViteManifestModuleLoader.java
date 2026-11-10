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
package org.apache.wicket.islands.modules;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Instant;
import java.util.Objects;

import org.apache.wicket.Application;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.JavaScriptReferenceType;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.io.Connections;
import org.apache.wicket.util.lang.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads island modules from a Vite production build on the classpath.
 * <p>
 * The build's output directory is expected under a base path on the classpath, with Vite's
 * manifest in it. Each island type is an input of the build whose name is the island type; its
 * entry module and style sheets are rendered from the manifest, and served by an
 * {@link IslandAssetResource} mounted under the given reference.
 *
 * @since 12.0.0
 */
public class ViteManifestModuleLoader implements IIslandModuleLoader
{
	private static final Logger LOG = LoggerFactory.getLogger(ViteManifestModuleLoader.class);

	private final String basePath;

	private final String manifestFile;

	private final ResourceReference assets;

	private volatile ViteManifest manifest;

	private volatile Instant manifestModified;

	private volatile boolean manifestMissing;

	/**
	 * @param basePath
	 *            the classpath location of the build's output directory, ending in a slash
	 * @param manifestFile
	 *            the manifest, relative to the base path
	 * @param assets
	 *            the mounted reference of the {@link IslandAssetResource} serving the files; the
	 *            path of a file is passed as its indexed parameters
	 */
	public ViteManifestModuleLoader(String basePath, String manifestFile, ResourceReference assets)
	{
		this.basePath = Args.notEmpty(basePath, "basePath");
		this.manifestFile = Args.notEmpty(manifestFile, "manifestFile");
		this.assets = Args.notNull(assets, "assets");
	}

	/**
	 * @return the classpath location of the build's output directory
	 */
	public String getBasePath()
	{
		return basePath;
	}

	/**
	 * Reads the manifest once in deployment mode. In development mode it is read again whenever it
	 * changes, so a build made while the application runs is picked up.
	 *
	 * @return the manifest, {@code null} if there is no build on the classpath
	 */
	public ViteManifest getManifest()
	{
		boolean development = Application.get().usesDevelopmentConfig();
		if (!development && (manifest != null || manifestMissing))
		{
			return manifest;
		}

		URL url = getClassLoader().getResource(basePath + manifestFile);
		if (url == null)
		{
			if (!manifestMissing)
			{
				manifestMissing = true;
				LOG.warn("No Vite manifest at '{}{}' on the classpath: islands are rendered without "
					+ "their JavaScript modules.", basePath, manifestFile);
			}
			manifest = null;
			return null;
		}
		manifestMissing = false;
		try
		{
			Instant modified = development ? Connections.getLastModified(url) : null;
			if (manifest == null || !Objects.equals(modified, manifestModified))
			{
				try (InputStream in = url.openStream())
				{
					manifest = ViteManifest.read(in);
				}
				manifestModified = modified;
			}
		}
		catch (IOException e)
		{
			throw new WicketRuntimeException("Cannot read the Vite manifest at " + url, e);
		}
		return manifest;
	}

	/**
	 * @return the class loader to read the build from
	 */
	protected ClassLoader getClassLoader()
	{
		return Application.get().getApplicationSettings().getClassResolver().getClassLoader();
	}

	@Override
	public void renderHead(String islandType, IHeaderResponse response)
	{
		ViteManifest current = getManifest();
		if (current == null)
		{
			return;
		}

		String key = current.findEntry(islandType);
		if (key == null)
		{
			throw new IllegalStateException("The Vite manifest at '" + basePath + manifestFile +
				"' has no entry for island type '" + islandType + "'");
		}

		for (String css : current.collectCss(key))
		{
			response.render(CssHeaderItem.forReference(assets, toParameters(css), null));
		}
		response.render(JavaScriptHeaderItem
			.forReference(assets, toParameters(current.getChunk(key).file()), null)
			.setType(JavaScriptReferenceType.MODULE));
	}

	private static PageParameters toParameters(String file)
	{
		PageParameters parameters = new PageParameters();
		String[] segments = file.split("/");
		for (int i = 0; i < segments.length; i++)
		{
			parameters.set(i, segments[i]);
		}
		return parameters;
	}
}

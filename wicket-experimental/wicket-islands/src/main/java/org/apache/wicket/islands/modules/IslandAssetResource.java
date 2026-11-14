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
import java.util.function.Supplier;

import org.apache.wicket.Application;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.util.io.Streams;
import org.apache.wicket.util.lang.Args;

/**
 * Serves the files of a Vite production build from the classpath.
 * <p>
 * Only files listed in the build's manifest are served; any other path is answered with
 * <code>404</code>, so the resource cannot be used to read other classpath entries. Vite gives
 * built files content-hashed names, so they are cached for as long as possible.
 *
 * @since 12.0.0
 */
public class IslandAssetResource extends AbstractResource
{
	private static final long serialVersionUID = 1L;

	private final Supplier<ViteManifestModuleLoader> loader;

	/**
	 * @param loader
	 *            supplies the loader whose build is served
	 */
	public IslandAssetResource(Supplier<ViteManifestModuleLoader> loader)
	{
		this.loader = Args.notNull(loader, "loader");
	}

	@Override
	protected ResourceResponse newResourceResponse(Attributes attributes)
	{
		ResourceResponse response = new ResourceResponse();

		String path = toPath(attributes.getParameters());
		ViteManifestModuleLoader current = loader.get();
		ViteManifest manifest = current.getManifest();
		URL url = null;
		if (manifest != null && manifest.getFiles().contains(path))
		{
			url = current.getClassLoader().getResource(current.getBasePath() + path);
		}
		if (url == null)
		{
			response.setError(404);
			return response;
		}

		response.setContentType(contentType(path));
		response.setCacheDurationToMaximum();
		response.setCacheScope(WebResponse.CacheScope.PUBLIC);
		final URL file = url;
		response.setWriteCallback(new WriteCallback()
		{
			@Override
			public void writeData(Attributes attributes) throws IOException
			{
				try (InputStream in = file.openStream())
				{
					Streams.copy(in, attributes.getResponse().getOutputStream());
				}
			}
		});
		return response;
	}

	private static String toPath(PageParameters parameters)
	{
		StringBuilder path = new StringBuilder();
		for (int i = 0; i < parameters.getIndexedCount(); i++)
		{
			if (i > 0)
			{
				path.append('/');
			}
			path.append(parameters.get(i).toString(""));
		}
		return path.toString();
	}

	private static String contentType(String path)
	{
		if (path.endsWith(".js") || path.endsWith(".mjs"))
		{
			return "text/javascript";
		}
		if (path.endsWith(".css"))
		{
			return "text/css";
		}
		String mimeType = Application.get().getMimeType(path);
		return mimeType != null ? mimeType : "application/octet-stream";
	}
}

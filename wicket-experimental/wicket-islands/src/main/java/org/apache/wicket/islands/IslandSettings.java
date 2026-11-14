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
package org.apache.wicket.islands;

import org.apache.wicket.Application;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.islands.modules.IIslandModuleLoader;
import org.apache.wicket.islands.modules.IslandAssetResource;
import org.apache.wicket.islands.modules.ViteDevServerModuleLoader;
import org.apache.wicket.islands.modules.ViteManifestModuleLoader;
import org.apache.wicket.islands.serializer.IIslandSerializer;
import org.apache.wicket.islands.serializer.JacksonIslandSerializer;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.lang.Args;

/**
 * Application-wide settings for islands. Install them once, in
 * {@link WebApplication#init()}:
 *
 * <pre>
 * IslandSettings.install(this).setDevServerUrl("http://localhost:5173");
 * </pre>
 *
 * @since 12.0.0
 */
public class IslandSettings
{
	/**
	 * The default classpath location of the Vite build.
	 */
	public static final String DEFAULT_BASE_PATH = "META-INF/wicket-islands/";

	/**
	 * The default location of the Vite manifest, relative to the base path.
	 */
	public static final String DEFAULT_MANIFEST_FILE = ".vite/manifest.json";

	/**
	 * The path the built files are served under.
	 */
	public static final String MOUNT_PATH = "wicket-islands";

	/**
	 * The path page-less endpoints are served under.
	 */
	public static final String ENDPOINT_PATH = "wicket-islands-call";

	private static final MetaDataKey<IslandSettings> KEY = new MetaDataKey<>()
	{
		private static final long serialVersionUID = 1L;
	};

	private IIslandSerializer serializer = new JacksonIslandSerializer();

	private String basePath = DEFAULT_BASE_PATH;

	private String manifestFile = DEFAULT_MANIFEST_FILE;

	private String devServerUrl;

	private IIslandModuleLoader moduleLoader;

	private volatile ViteManifestModuleLoader manifestModuleLoader;

	private ResourceReference assets;

	private ResourceReference endpoints;

	private final PagelessEndpoints pagelessEndpoints = new PagelessEndpoints();

	private IIslandHandleStore handleStore = new SessionHandleStore();

	/**
	 * Installs the settings in an application, mounts the resource serving the built files,
	 * registers the listener that answers island calls failing before they reach their island, and
	 * makes Ajax behaviors on a {@link FormIsland} send the hidden input holding its value. When the
	 * settings are installed already, returns them unchanged.
	 *
	 * @param application
	 *            the application
	 * @return the settings, for further configuration
	 */
	public static IslandSettings install(WebApplication application)
	{
		Args.notNull(application, "application");

		IslandSettings installed = application.getMetaData(KEY);
		if (installed != null)
		{
			return installed;
		}
		IslandSettings settings = new IslandSettings();
		settings.assets = new ResourceReference(IslandSettings.class, "assets")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public IResource getResource()
			{
				return new IslandAssetResource(settings::getManifestModuleLoader);
			}
		};
		settings.endpoints = new ResourceReference(IslandSettings.class, "endpoints")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public IResource getResource()
			{
				return new PagelessEndpointResource();
			}
		};
		application.setMetaData(KEY, settings);
		application.mountResource(MOUNT_PATH, settings.assets);
		application.mountResource(ENDPOINT_PATH, settings.endpoints);
		application.getRequestCycleListeners().add(new IslandCallFailureListener());
		application.getAjaxRequestTargetListeners().add(new AjaxRequestTarget.IListener()
		{
			@Override
			public void updateAjaxAttributes(AbstractDefaultAjaxBehavior behavior,
				AjaxRequestAttributes attributes)
			{
				if (behavior.getComponent() instanceof FormIsland)
				{
					attributes.setSerializeRecursively(true);
				}
			}
		});
		return settings;
	}

	/**
	 * @return the settings of the current application
	 * @throws IllegalStateException
	 *             if they have not been installed
	 */
	public static IslandSettings get()
	{
		IslandSettings settings = Application.get().getMetaData(KEY);
		if (settings == null)
		{
			throw new IllegalStateException(
				"Islands are not installed: call IslandSettings.install(this) in Application#init()");
		}
		return settings;
	}

	/**
	 * @return the page-less endpoints of the application
	 */
	public PagelessEndpoints getPagelessEndpoints()
	{
		return pagelessEndpoints;
	}

	/**
	 * @return the store of the handles islands issue for page-less calls, by default a
	 *         {@link SessionHandleStore}
	 */
	public IIslandHandleStore getHandleStore()
	{
		return handleStore;
	}

	/**
	 * @param handleStore
	 *            the store of the handles islands issue for page-less calls
	 * @return {@code this}
	 */
	public IslandSettings setHandleStore(IIslandHandleStore handleStore)
	{
		this.handleStore = Args.notNull(handleStore, "handleStore");
		return this;
	}

	ResourceReference getEndpointReference()
	{
		return endpoints;
	}

	/**
	 * @return the serializer for props, call payloads and results
	 */
	public IIslandSerializer getSerializer()
	{
		return serializer;
	}

	/**
	 * @param serializer
	 *            the serializer for props, call payloads and results
	 * @return {@code this}
	 */
	public IslandSettings setSerializer(IIslandSerializer serializer)
	{
		this.serializer = Args.notNull(serializer, "serializer");
		return this;
	}

	/**
	 * @param basePath
	 *            the classpath location of the Vite build, ending in a slash
	 * @return {@code this}
	 */
	public IslandSettings setBasePath(String basePath)
	{
		this.basePath = Args.notEmpty(basePath, "basePath");
		manifestModuleLoader = null;
		return this;
	}

	/**
	 * @param manifestFile
	 *            the location of the Vite manifest, relative to the base path
	 * @return {@code this}
	 */
	public IslandSettings setManifestFile(String manifestFile)
	{
		this.manifestFile = Args.notEmpty(manifestFile, "manifestFile");
		manifestModuleLoader = null;
		return this;
	}

	/**
	 * Sets the URL of a Vite dev server to load island modules from. It is only used when the
	 * application runs in development mode.
	 *
	 * @param devServerUrl
	 *            the dev server's URL, {@code null} to always use the build on the classpath
	 * @return {@code this}
	 */
	public IslandSettings setDevServerUrl(String devServerUrl)
	{
		this.devServerUrl = devServerUrl;
		return this;
	}

	/**
	 * @param moduleLoader
	 *            a loader to use instead of the default, {@code null} for the default
	 * @return {@code this}
	 */
	public IslandSettings setModuleLoader(IIslandModuleLoader moduleLoader)
	{
		this.moduleLoader = moduleLoader;
		return this;
	}

	/**
	 * @return the configured loader; otherwise the dev server loader when a dev server is set and
	 *         the application runs in development mode; otherwise the loader for the build on the
	 *         classpath
	 */
	public IIslandModuleLoader getModuleLoader()
	{
		if (moduleLoader != null)
		{
			return moduleLoader;
		}
		if (devServerUrl != null && Application.get().usesDevelopmentConfig())
		{
			return new ViteDevServerModuleLoader(devServerUrl);
		}
		return getManifestModuleLoader();
	}

	/**
	 * @return the loader for the build on the classpath
	 */
	public ViteManifestModuleLoader getManifestModuleLoader()
	{
		ViteManifestModuleLoader loader = manifestModuleLoader;
		if (loader == null)
		{
			loader = new ViteManifestModuleLoader(basePath, manifestFile, assets);
			manifestModuleLoader = loader;
		}
		return loader;
	}
}

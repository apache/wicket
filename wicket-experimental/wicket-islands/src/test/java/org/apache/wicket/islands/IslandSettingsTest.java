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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;

import org.apache.wicket.core.random.ISecureRandomSupplier;
import org.apache.wicket.islands.modules.ViteManifestModuleLoader;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Installing islands, and what is checked when an island is set up.
 */
class IslandSettingsTest extends IslandTestCase
{
	@Test
	void installingTwiceReturnsTheInstalledSettings()
	{
		WebApplication application = tester.getApplication();

		assertSame(IslandSettings.get(), IslandSettings.install(application));
	}

	@Test
	void keyComesFromTheSecureRandomSupplier()
	{
		tester.getApplication().getSecuritySettings().setRandomSupplier(new ISecureRandomSupplier()
		{
			@Override
			public SecureRandom getRandom()
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public String getRandomBase64(int length)
			{
				return "random-" + length;
			}
		});

		assertEquals("random-12", new Island<>("island", "test-island", Model.of("")).getKey());
	}

	@Test
	void unknownPagelessEndpointFailsWhenTheIslandIsSetUp()
	{
		Island<String> island = new Island<>("island", "test-island", Model.of(""));

		assertThrows(IllegalArgumentException.class, () -> island.usePageless("no-such-endpoint"));
	}

	@Test
	void rejectionDropsMissingMessages()
	{
		assertEquals(List.of("negative"),
			new IslandCallRejectedException("negative", null).getMessages());
		assertEquals(List.of(), new IslandCallRejectedException((String)null).getMessages());
	}

	@Test
	void manifestIsReadAgainInDevelopmentWhenItChanges(@TempDir Path build) throws IOException
	{
		Path manifest = build.resolve("build/manifest.json");
		Files.createDirectories(manifest.getParent());
		writeManifest(manifest, "first", Instant.parse("2026-11-01T10:00:00Z"));
		ViteManifestModuleLoader loader = loaderFor(build);
		assertEquals("first.js", loader.getManifest().findEntry("first"));

		writeManifest(manifest, "second", Instant.parse("2026-11-01T10:05:00Z"));

		assertNull(loader.getManifest().findEntry("first"));
		assertEquals("second.js", loader.getManifest().findEntry("second"));
	}

	private static void writeManifest(Path manifest, String name, Instant modified)
		throws IOException
	{
		Files.writeString(manifest, "{\"" + name + ".js\":{\"file\":\"assets/" + name +
			".js\",\"name\":\"" + name + "\",\"isEntry\":true}}");
		Files.setLastModifiedTime(manifest, FileTime.from(modified));
	}

	private static ViteManifestModuleLoader loaderFor(Path root) throws MalformedURLException
	{
		ClassLoader classLoader = new URLClassLoader(new URL[] { root.toUri().toURL() }, null);
		ResourceReference assets = new ResourceReference("assets")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public IResource getResource()
			{
				throw new UnsupportedOperationException();
			}
		};
		return new ViteManifestModuleLoader("build/", "manifest.json", assets)
		{
			@Override
			protected ClassLoader getClassLoader()
			{
				return classLoader;
			}
		};
	}
}

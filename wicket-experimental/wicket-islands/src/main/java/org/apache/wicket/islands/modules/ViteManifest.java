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
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.util.lang.Args;

import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * The manifest Vite writes for a production build: a map from source module to the files it was
 * built into.
 *
 * @see <a href="https://vite.dev/guide/backend-integration">Vite backend integration</a>
 * @since 12.0.0
 */
public final class ViteManifest
{
	/**
	 * One chunk of the build.
	 *
	 * @param file
	 *            the built file, relative to the build's output directory
	 * @param name
	 *            the chunk name; for an entry the key of the build's input
	 * @param isEntry
	 *            whether the chunk is an entry
	 * @param imports
	 *            the manifest keys of the chunks this chunk imports statically
	 * @param css
	 *            the style sheets of this chunk
	 * @param assets
	 *            other files this chunk references
	 */
	public record Chunk(String file, String name, Boolean isEntry, List<String> imports,
		List<String> css, List<String> assets)
	{
	}

	private static final JsonMapper MAPPER = JsonMapper.builder()
		.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
		.build();

	private final Map<String, Chunk> chunks;

	private final Set<String> files;

	private ViteManifest(Map<String, Chunk> chunks)
	{
		this.chunks = Map.copyOf(chunks);
		files = Collections.unmodifiableSet(collectFiles(chunks));
	}

	/**
	 * Reads a manifest.
	 *
	 * @param in
	 *            the manifest's JSON
	 * @return the manifest
	 */
	public static ViteManifest read(InputStream in)
	{
		Args.notNull(in, "in");
		try (in)
		{
			return new ViteManifest(MAPPER.readValue(in, new TypeReference<Map<String, Chunk>>()
			{
			}));
		}
		catch (JacksonException | IOException e)
		{
			throw new WicketRuntimeException("Cannot read the Vite manifest", e);
		}
	}

	/**
	 * Finds the entry chunk built for an input name.
	 *
	 * @param name
	 *            the input name
	 * @return the manifest key of the entry, {@code null} if there is none
	 */
	public String findEntry(String name)
	{
		for (Map.Entry<String, Chunk> entry : chunks.entrySet())
		{
			Chunk chunk = entry.getValue();
			if (Boolean.TRUE.equals(chunk.isEntry()) && name.equals(chunk.name()))
			{
				return entry.getKey();
			}
		}
		return null;
	}

	/**
	 * @param key
	 *            a manifest key
	 * @return the chunk, {@code null} if there is none
	 */
	public Chunk getChunk(String key)
	{
		return chunks.get(key);
	}

	/**
	 * Collects the style sheets of a chunk and of every chunk it imports, transitively, in
	 * dependency order.
	 *
	 * @param key
	 *            the manifest key of the chunk
	 * @return the style sheet files
	 */
	public Set<String> collectCss(String key)
	{
		Set<String> css = new LinkedHashSet<>();
		collectCss(key, new HashSet<>(), css);
		return css;
	}

	private void collectCss(String key, Set<String> visited, Set<String> css)
	{
		Chunk chunk = chunks.get(key);
		if (chunk == null || !visited.add(key))
		{
			return;
		}
		if (chunk.imports() != null)
		{
			for (String imported : chunk.imports())
			{
				collectCss(imported, visited, css);
			}
		}
		if (chunk.css() != null)
		{
			css.addAll(chunk.css());
		}
	}

	/**
	 * @return every file the build produced, relative to its output directory
	 */
	public Set<String> getFiles()
	{
		return files;
	}

	private static Set<String> collectFiles(Map<String, Chunk> chunks)
	{
		Set<String> files = new HashSet<>();
		for (Chunk chunk : chunks.values())
		{
			if (chunk.file() != null)
			{
				files.add(chunk.file());
			}
			if (chunk.css() != null)
			{
				files.addAll(chunk.css());
			}
			if (chunk.assets() != null)
			{
				files.addAll(chunk.assets());
			}
		}
		return files;
	}
}

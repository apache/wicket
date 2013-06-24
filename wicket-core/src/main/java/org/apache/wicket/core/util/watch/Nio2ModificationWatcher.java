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
package org.apache.wicket.core.util.watch;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.ThreadContext;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.util.io.IOUtils;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.thread.ICode;
import org.apache.wicket.util.thread.Task;
import org.apache.wicket.util.time.Duration;
import org.apache.wicket.util.watch.ModificationWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An extension of ModificationWatcher that removes the NotFound entries from
 * the MarkupCache for newly created files.
 *
 * By default MarkupCache registers Markup.NO_MARKUP value for each requested but
 * not found markup file. Later when the user creates the markup file the MarkupCache
 * should be notified.
 *
 * @since 7.0.0
 */
public class Nio2ModificationWatcher extends ModificationWatcher
{
	private static final Logger LOG = LoggerFactory.getLogger(Nio2ModificationWatcher.class);

	private final WatchService watchService;
	private final Application application;

	/** the <code>Task</code> to run */
	private Task task;

	public Nio2ModificationWatcher(final Application application)
	{
		try
		{
			this.application = application;

			this.watchService = FileSystems.getDefault().newWatchService();
			registerWatchables(watchService);

			start(Duration.seconds(2));

		} catch (IOException iox)
		{
			throw new WicketRuntimeException("Cannot get the watch service", iox);
		}
	}

	@Override
	public void start(final Duration pollFrequency)
	{
		// Construct task with the given polling frequency
		task = new Task("Wicket-ModificationWatcher-NIO2");

		task.run(pollFrequency, new ICode() {
			@Override
			public void run(final Logger log)
			{
				checkCreated();
				checkModified();
			}
		});
	}

	/**
	 * Checks for newly created files and folders.
	 * New folders are registered to be watched.
	 * New files are removed from the MarkupCache because there could be
	 * {@link org.apache.wicket.markup.Markup#NO_MARKUP} (Not Found) entries for them already.
	 */
	protected void checkCreated()
	{
		WatchKey watchKey = watchService.poll();
		if (watchKey != null)
		{
			List<WatchEvent<?>> events = watchKey.pollEvents();
			for (WatchEvent<?> event : events)
			{
				WatchEvent.Kind<?> eventKind = event.kind();
				Path eventPath = (Path) event.context();

				if (eventKind == ENTRY_CREATE)
				{
					if (Files.isDirectory(eventPath))
					{
						try
						{
							// a directory is created. register it for notifications
							register(eventPath, watchService);
						} catch (IOException iox)
						{
							LOG.warn("Cannot register folder '" + eventPath + "' to be watched.", iox);
						}
					}
					else
					{
						// a new file appeared. we need to clear the NOT_FOUND entry that may be added earlier.
						// MarkupCache keys are fully qualified URIs
						String absolutePath = eventPath.toAbsolutePath().toFile().toURI().toString();

						try
						{
							ThreadContext.setApplication(application);
							application.getMarkupSettings()
									.getMarkupFactory().getMarkupCache().removeMarkup(absolutePath);
						} finally {
							ThreadContext.setApplication(null);
						}
					}
				}
			}

			watchKey.reset();
		}
	}

	@Override
	public void destroy()
	{
		if (task != null)
		{
			task.interrupt();
		}
		IOUtils.closeQuietly(watchService);
	}

	/**
	 * Registers all classpath folder entries and their subfolders in the {@code #watchService}.
	 * 
	 * @param watchService
	 *      the watch service that will send the notifications
	 * @throws IOException
	 */
	private void registerWatchables(final WatchService watchService) throws IOException
	{
		String classpath = System.getProperty("java.class.path");

		String[] classPathEntries = Strings.split(classpath, File.pathSeparatorChar);
		for (String classPathEntry : classPathEntries)
		{
			if (classPathEntry.endsWith(".jar") == false)
			{
				Path folder = Paths.get(classPathEntry);
				if (Files.isDirectory(folder))
				{
					register(folder, watchService);

					Files.walkFileTree(folder, new SimpleFileVisitor<Path>() {
						@Override
						public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException
						{
							register(dir, watchService);
							return FileVisitResult.CONTINUE;
						}
					});
				}
			}
		}
	}
	
	private void register(final Path folder, final WatchService watchService) throws IOException
	{
		LOG.debug("Registering folder '{}'", folder);
		folder.register(watchService, ENTRY_CREATE);
	}
}

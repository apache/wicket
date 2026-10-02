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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.apache.wicket.Application;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.dynamic.UpdateRowMessageType;
import org.apache.wicket.protocol.ws.api.registry.IKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Advances the progress of random contacts among those a page shows, on a timer, and pushes the
 * changed rows to the page over its web socket connection, at most once per {@link #FLUSH_MILLIS}
 * however often they change. Rows the page does not show are neither advanced nor pushed: the
 * page reports which rows it shows with {@link #show(String, int, Collection)}, whenever the
 * table sends rows to the browser.
 * <p>
 * One simulator runs per page, from the opening of the page's web socket connection to its
 * closing.
 */
final class ProgressSimulator
{
	private static final Logger LOG = LoggerFactory.getLogger(ProgressSimulator.class);

	private static final long TICK_MILLIS = 100;

	private static final long FLUSH_MILLIS = 500;

	private static final int CONTACTS_PER_TICK = 3;

	private static final ScheduledExecutorService TIMER = Executors.newScheduledThreadPool(1,
		runnable -> {
			Thread thread = new Thread(runnable, "dynamic-table-progress");
			thread.setDaemon(true);
			return thread;
		});

	private static final ConcurrentMap<String, ProgressSimulator> RUNNING =
		new ConcurrentHashMap<>();

	private static final ConcurrentMap<String, List<Long>> SHOWN = new ConcurrentHashMap<>();

	private final Application application;

	private final String sessionId;

	private final int pageId;

	private final IKey key;

	private final String tableId;

	private final ContactsDatabase database;

	private final Set<Long> changed = ConcurrentHashMap.newKeySet();

	private final List<ScheduledFuture<?>> tasks = new ArrayList<>();

	private ProgressSimulator(Application application, String sessionId, int pageId, IKey key,
		String tableId, ContactsDatabase database)
	{
		this.application = application;
		this.sessionId = sessionId;
		this.pageId = pageId;
		this.key = key;
		this.tableId = tableId;
		this.database = database;
	}

	/**
	 * Starts simulating for a page, replacing a simulator already running for it.
	 */
	static void start(Application application, String sessionId, int pageId, IKey key,
		String tableId, ContactsDatabase database)
	{
		ProgressSimulator simulator = new ProgressSimulator(application, sessionId, pageId, key,
			tableId, database);
		ProgressSimulator previous = RUNNING.put(id(sessionId, pageId), simulator);
		if (previous != null)
		{
			previous.cancel();
		}
		simulator.schedule();
	}

	/**
	 * Stops simulating for a page.
	 */
	static void stop(String sessionId, int pageId)
	{
		ProgressSimulator simulator = RUNNING.remove(id(sessionId, pageId));
		SHOWN.remove(id(sessionId, pageId));
		if (simulator != null)
		{
			simulator.cancel();
		}
	}

	/**
	 * Records which contacts a page shows; may be called before the simulator of the page starts.
	 */
	static void show(String sessionId, int pageId, Collection<Long> ids)
	{
		SHOWN.put(id(sessionId, pageId), List.copyOf(ids));
	}

	private static String id(String sessionId, int pageId)
	{
		return sessionId + '/' + pageId;
	}

	private List<Long> shown()
	{
		return SHOWN.getOrDefault(id(sessionId, pageId), List.of());
	}

	private synchronized void schedule()
	{
		tasks.add(TIMER.scheduleAtFixedRate(this::advance, TICK_MILLIS, TICK_MILLIS,
			TimeUnit.MILLISECONDS));
		tasks.add(TIMER.scheduleAtFixedRate(this::flush, FLUSH_MILLIS, FLUSH_MILLIS,
			TimeUnit.MILLISECONDS));
	}

	private synchronized void cancel()
	{
		tasks.forEach(task -> task.cancel(false));
		tasks.clear();
	}

	private void advance()
	{
		List<Long> shown = shown();
		for (int i = 0; i < CONTACTS_PER_TICK && shown.isEmpty() == false; i++)
		{
			Long id = shown.get(ThreadLocalRandom.current().nextInt(shown.size()));
			try
			{
				Contact contact = database.get(id);
				int progress = contact.getProgress() + ThreadLocalRandom.current().nextInt(1, 8);
				contact.setProgress(progress > 100 ? 0 : progress);
				changed.add(id);
			}
			catch (RuntimeException deleted)
			{
				continue;
			}
		}
	}

	private void flush()
	{
		List<Long> ids = new ArrayList<>(changed);
		changed.removeAll(ids);
		ids.retainAll(shown());
		for (Long id : ids)
		{
			Contact contact;
			try
			{
				contact = database.get(id);
			}
			catch (RuntimeException deleted)
			{
				continue;
			}
			try
			{
				if (UpdateRowMessageType.newMessage(tableId, String.valueOf(id), contact)
					.send(application, sessionId, key) == false)
				{
					stop(sessionId, pageId);
					return;
				}
			}
			catch (IOException | RuntimeException e)
			{
				LOG.debug("Pushing the progress of contact {} failed, stopping", id, e);
				stop(sessionId, pageId);
				return;
			}
		}
	}
}

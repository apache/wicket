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
package org.apache.wicket.util.watch;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.wicket.util.lang.Generics;
import org.apache.wicket.util.listener.ChangeListenerSet;
import org.apache.wicket.util.listener.IChangeListener;
import org.apache.wicket.util.thread.ICode;
import org.apache.wicket.util.thread.Task;
import org.apache.wicket.util.time.Duration;
import org.apache.wicket.util.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Monitors one or more <code>IModifiable</code> objects, calling a {@link IChangeListener
 * IChangeListener} when a given object's modification time changes.
 * 
 * @author Jonathan Locke
 * @since 1.2.6
 */
public class ModificationWatcher implements IModificationWatcher
{
	/** logger */
	private static final Logger log = LoggerFactory.getLogger(ModificationWatcher.class);

	/** maps <code>IModifiable</code> objects to <code>Entry</code> objects */
	private final Map<IModifiable, Entry> modifiableToEntry = Generics.newConcurrentHashMap();

	/** the <code>Task</code> to run */
	private Task task;

	/**
	 * Container class for holding modifiable entries to watch.
	 */
	protected static final class Entry
	{
		// The most recent lastModificationTime polled on the object
		public Time lastModifiedTime;

		// The set of listeners to call when the modifiable changes
		public final ChangeListenerSet listeners = new ChangeListenerSet();

		// The modifiable thing
		public IModifiable modifiable;
	}

	/**
	 * Default constructor for two-phase construction.
	 */
	public ModificationWatcher()
	{
	}

	/**
	 * Constructor that accepts a <code>Duration</code> argument representing the poll frequency.
	 * 
	 * @param pollFrequency
	 *            how often to check on <code>IModifiable</code>s
	 */
	public ModificationWatcher(final Duration pollFrequency)
	{
		start(pollFrequency);
	}

	@Override
	public final boolean add(final IModifiable modifiable, final IChangeListener listener)
	{
		// Look up entry for modifiable
		final Entry entry = modifiableToEntry.get(modifiable);

		// Found it?
		if (entry == null)
		{
			Time lastModifiedTime = modifiable.lastModifiedTime();
			if (lastModifiedTime != null)
			{
				// Construct new entry
				final Entry newEntry = new Entry();

				newEntry.modifiable = modifiable;
				newEntry.lastModifiedTime = lastModifiedTime;
				newEntry.listeners.add(listener);

				// Put in map
				modifiableToEntry.put(modifiable, newEntry);
			}
			else
			{
				// The IModifiable is not returning a valid lastModifiedTime
				log.info("Cannot track modifications to resource " + modifiable);
			}

			return true;
		}
		else
		{
			// Add listener to existing entry
			return !entry.listeners.add(listener);
		}
	}

	@Override
	public IModifiable remove(final IModifiable modifiable)
	{
		final Entry entry = modifiableToEntry.remove(modifiable);
		if (entry != null)
		{
			return entry.modifiable;
		}
		return null;
	}

	@Override
	public void start(final Duration pollFrequency)
	{
		// Construct task with the given polling frequency
		task = new Task("ModificationWatcher");

		task.run(pollFrequency, new ICode()
		{
			@Override
			public void run(final Logger log)
			{
				checkModified();
			}
		});
	}

	/**
	 * Checks which IModifiables were modified and notifies their listeners
	 */
	protected void checkModified()
	{
		for (Entry entry : modifiableToEntry.values())
		{
			// If the modifiable has been modified after the last known
			// modification time
			final Time modifiableLastModified = entry.modifiable.lastModifiedTime();
			if ((modifiableLastModified != null) &&
					modifiableLastModified.after(entry.lastModifiedTime))
			{
				// Notify all listeners that the modifiable was modified
				entry.listeners.notifyListeners();

				// Update timestamp
				entry.lastModifiedTime = modifiableLastModified;
			}
		}
	}

	@Override
	public void destroy()
	{
		if (task != null)
		{
			// task.stop();
			task.interrupt();
		}
	}

	@Override
	public final Set<IModifiable> getEntries()
	{
		return modifiableToEntry.keySet();
	}
}

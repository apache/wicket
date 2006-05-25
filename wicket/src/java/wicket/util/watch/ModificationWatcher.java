/*
 * $Id: ModificationWatcher.java 5771 2006-05-19 12:04:06 +0000 (Fri, 19 May
 * 2006) joco01 $ $Revision$ $Date: 2006-05-19 12:04:06 +0000 (Fri, 19
 * May 2006) $
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.util.watch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.util.listener.ChangeListenerSet;
import wicket.util.listener.IChangeListener;
import wicket.util.thread.ICode;
import wicket.util.thread.Task;
import wicket.util.time.Duration;
import wicket.util.time.Time;

/**
 * Monitors one or more Modifiable objects, calling a ChangeListener when a
 * given object's modification time changes.
 * 
 * @author Jonathan Locke
 */
public final class ModificationWatcher
{
	/** Logging */
	private static final Log log = LogFactory.getLog(ModificationWatcher.class);

	/** Maps Modifiable objects to Entry objects */
	private final Map<IModifiable, Entry> modifiableToEntry = new HashMap<IModifiable, Entry>();

	private Task task;

	// MarkupContainer class for holding modifiable entries to watch
	private static final class Entry
	{
		// The most recent lastModificationTime polled on the object
		Time lastModifiedTime;

		// The set of listeners to call when the modifiable changes
		final ChangeListenerSet listeners = new ChangeListenerSet();

		// The modifiable thing
		IModifiable modifiable;
	}

	/**
	 * For two-phase construction
	 */
	public ModificationWatcher()
	{
	}

	/**
	 * Constructor
	 * 
	 * @param pollFrequency
	 *            How often to check on modifiables
	 */
	public ModificationWatcher(final Duration pollFrequency)
	{
		start(pollFrequency);
	}

	/**
	 * Adds a Modifiable object and an IChangeListener to call when the
	 * modifiable object is modified.
	 * 
	 * @param modifiable
	 *            The modifiable thing to monitor
	 * @param listener
	 *            The listener to call if the modifiable is modified
	 * @return <tt>true</tt> if the set did not already contain the specified
	 *         element.
	 */
	public final boolean add(final IModifiable modifiable, final IChangeListener listener)
	{
		// Look up entry for modifiable
		final Entry entry = modifiableToEntry.get(modifiable);

		// Found it?
		if (entry == null)
		{
			if (modifiable.lastModifiedTime() != null)
			{
				// Construct new entry
				final Entry newEntry = new Entry();

				newEntry.modifiable = modifiable;
				newEntry.lastModifiedTime = modifiable.lastModifiedTime();
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
			return entry.listeners.add(listener);
		}
	}

	/**
	 * Remove all entries associated with 'modifiable'
	 * 
	 * @param modifiable
	 * @return the object removed, else null
	 */
	public IModifiable remove(final IModifiable modifiable)
	{
		final Entry entry = modifiableToEntry.remove(modifiable);
		if (entry != null)
		{
			return entry.modifiable;
		}
		return null;
	}

	/**
	 * Start watching at a given polling rate
	 * 
	 * @param pollFrequency
	 *            The poll rate
	 */
	public void start(final Duration pollFrequency)
	{
		// Construct task with the given polling frequency
		task = new Task("ModificationWatcher");

		task.run(pollFrequency, new ICode()
		{
			public void run(final Log log)
			{
				// Iterate over a copy of the list of entries to avoid
				// concurrent
				// modification problems without the associated liveness issues
				// of holding a lock while potentially polling file times!
				for (final Iterator<Entry> iterator = new ArrayList<Entry>(modifiableToEntry
						.values()).iterator(); iterator.hasNext();)
				{
					// Get next entry
					final Entry entry = iterator.next();

					// If the modifiable has been modified after the last known
					// modification time
					final Time modifiableLastModified = entry.modifiable.lastModifiedTime();

					if (modifiableLastModified.after(entry.lastModifiedTime))
					{
						// Notify all listeners that the modifiable was modified
						entry.listeners.notifyListeners();

						// Update timestamp
						entry.lastModifiedTime = modifiableLastModified;
					}
				}
			}
		});
	}

	/**
	 * stops the modification watcher from watching.
	 */
	public void destroy()
	{
		if (task != null)
		{
			task.stop();
		}
	}
}

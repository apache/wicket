/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.util.watch;

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
 * Monitors changes to changeables, calling a change listener when a changeable changes.
 * @author Jonathan Locke
 */
public final class Watcher
{
    // Code broadcaster for reporting
    private static final Log log = LogFactory.getLog(Watcher.class);

    // The changeable to entry map
    private final Map changeableToEntry = new HashMap();

    /**
     * For two-phase construction
     */
    public Watcher()
    {
    }

    /**
     * Constructor
     * @param pollFrequency How often to check on changeables
     */
    public Watcher(final Duration pollFrequency)
    {
        start(pollFrequency);
    }

    /**
     * Start watching at a given polling rate
     * @param pollFrequency The poll rate
     */
    public void start(final Duration pollFrequency)
    {
        // Construct task with the given polling frequency
        final Task task = new Task("Watcher");

        task.run(pollFrequency, new ICode()
        {
            public void run(final Log codeListener)
            {
                for (final Iterator iterator = changeableToEntry.values().iterator(); iterator
                        .hasNext();)
                {
                    // Get next entry
                    final Entry entry = (Entry) iterator.next();

                    // If the changeable has been modified after the last known
                    // modification time
                    final Time changeableLastModified = entry.changeable.lastModifiedTime();

                    if (changeableLastModified.after(entry.lastModifiedTime))
                    {
                        // Notify all listeners that the changeable changed
                        entry.listeners.notifyListeners();

                        // Update timestamp
                        entry.lastModifiedTime = changeableLastModified;
                    }
                }
            }
        });
    }

    /**
     * Adds a changeable and change listener to this monitor
     * @param changeable The changeable thing to monitor
     * @param listener The listener to call if the changeable changes
     */
    public final void add(final IChangeable changeable, final IChangeListener listener)
    {
        // Look up entry for changeable
        final Entry entry = (Entry) changeableToEntry.get(changeable);

        // Found it?
        if (entry == null)
        {
            if (changeable.lastModifiedTime() != null)
            {
                // Construct new entry
                final Entry newEntry = new Entry();

                newEntry.changeable = changeable;
                newEntry.lastModifiedTime = changeable.lastModifiedTime();
                newEntry.listeners.add(listener);

                // Put in map
                changeableToEntry.put(changeable, newEntry);
            }
            else
            {
                log.info("Cannot track changes to resource " + changeable);
            }
        }
        else
        {
            // Add listener to existing entry
            entry.listeners.add(listener);
        }
    }

    // Container class for holding changeable entries to watch
    private static final class Entry
    {
        // The changeable
        IChangeable changeable;

        // The last time the changeable was changed
        Time lastModifiedTime;

        // The set of listeners to call when the changeable changes
        final ChangeListenerSet listeners = new ChangeListenerSet();
    }
}

///////////////////////////////// End of File /////////////////////////////////

/*
 * $Id$
 * $Revision$ $Date$
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

import java.util.Map;

import wicket.util.concurrent.ConcurrentHashMap;
import wicket.util.listener.ChangeListenerSet;
import wicket.util.listener.IChangeListener;

/**
 * Similar to ModificationWatcher but manually triggered, calling all registered
 * ChangeListeners when a given object is triggered.
 * 
 * @author Juergen Donnerstag
 */
public final class Watcher
{
	/** Maps objects to change listener sets */
	private final Map keyToEntry  = new ConcurrentHashMap();

	// Class for holding entries to watch
	private static final class Entry
	{
		// The value associated with the key
		Object data;

		// The set of listeners to call when triggered
		final ChangeListenerSet listeners = new ChangeListenerSet();
	}

	/**
	 * Constructor
	 */
	public Watcher()
	{
	}

	/**
	 * Adds a key and an IChangeListener to call when the key is triggered.
	 * <p>
	 * Note: the value is ignored if the key and an associated value already
	 * exists. Only the listeners is added (if an equals does not already
	 * exist).
	 * 
	 * @param key
	 *            The key to identifiy a ChangeListenerSet
	 * @param listener
	 *            The listener to call if the key gets triggered
	 * @return <tt>true</tt> if the set did not already contain the specified
	 *         element.
	 */
	public final boolean add(final Object key, final IChangeListener listener)
	{
		// Look up entry for modifiable
		final Entry entry = (Entry)keyToEntry.get(key);

		// Found it?
		if (entry == null)
		{
			// Construct new entry
			final Entry newEntry = new Entry();
			if (listener != null)
			{
				newEntry.listeners.add(listener);
			}

			// Put in map
			keyToEntry.put(key, newEntry);

			return true;
		}
		else
		{
			// Add listener to existing entry
			if (listener != null)
			{
				return entry.listeners.add(listener);
			}
		}

		return false;
	}

	/**
	 * Remove all entries associated with 'modifiable'
	 * 
	 * @param key
	 * @return the object removed, else null
	 */
	public Object remove(final Object key)
	{
		return keyToEntry.remove(key);
	}

	/**
	 * Remove all entries
	 */
	public void clear()
	{
		keyToEntry.clear();
	}

	/**
	 * trigger all listeners registered with key
	 * 
	 * @param key
	 *            The key to identify the ChangeListenerSet
	 */
	public void notifyListeners(final Object key)
	{
		// Look up entry for modifiable
		final Entry entry = (Entry)keyToEntry.get(key);
		if (entry != null)
		{
			entry.listeners.notifyListeners();
		}
	}
}

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
package wicket.util.listener;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Holds a set of listeners, allowing for easy notification dispatching.
 * @author Jonathan Locke
 */
public final class ChangeListenerSet
{
    // Set of listeners
    private final Set listeners = new HashSet();

    /**
     * Adds a listener to this set of listeners.
     * @param listener The listener to add
     */
    public void add(final IChangeListener listener)
    {
        listeners.add(listener);
    }

    /**
     * Removes a listener from this set.
     * @param listener The listener to remove
     */
    public void remove(final IChangeListener listener)
    {
        listeners.remove(listener);
    }

    /**
     * Notify listeners of a change.
     */
    public void notifyListeners()
    {
        // Notify all listeners that the file changed
        final Set copy = new HashSet(listeners);

        for (final Iterator iterator = copy.iterator(); iterator.hasNext();)
        {
            ((IChangeListener) iterator.next()).changed();
        }
    }
}

///////////////////////////////// End of File /////////////////////////////////

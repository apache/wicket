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
package wicket.markup.html.form;

import java.util.ArrayList;
import java.util.Collection;

import wicket.RequestCycle;

/**
 * Partial adapter for {@link wicket.markup.html.form.IIdList} that makes
 * it easier to work with anonymous implementations.
 * <p>
 * An example of how to use this:
 * <pre>
 *  class TypesList extends IdListAdapter
 *  {
 *      // load all needed object when attaching
 *      public void doAttach(RequestCycle cycle)
 *      {
 *          List definitionTypes = definitionDAO.findDefinitionTypes();
 *          addAll(definitionTypes);
 *      }
 *
 *      // clear the list when detaching
 *      public void doDetach(RequestCycle cycle)
 *      {
 *          clear();
 *      }
 *
 *      // gets the value that is used for displaying
 *      public String getDisplayValue(int row)
 *      {
 *          DefinitionType type = (DefinitionType)get(row);
 *          return type.getName();
 *      }
 *
 *      // gets the backing id used for rendering the selection
 *      public String getIdValue(int row)
 *      {
 *          DefinitionType type = (DefinitionType)get(row);
 *          return type.getId().toString();
 *      }
 *
 *      // gets the object based on the given id
 *      public Object getObjectById(String id)
 *      {
 *          if(id == null) return null;
 *          return definitionDAO.loadDefinitionType(Long.valueOf(id));
 *          // note: more efficient would be to just look up the object in this list
 *          // as it is loaded allready
 *      }
 *  }
 * </pre>
 * </p>
 *
 * @author Eelco Hillenius
 */
public abstract class IdListAdapter extends ArrayList implements IIdList
{
    /**
     * Transient flag to prevent multiple detach/attach scenario.
     */
    private transient boolean attached = false;

    /**
     * Construct.
     */
    public IdListAdapter()
    {
        super();
    }

    /**
     * Construct.
     * @param initialCapacity the initial capacity
     */
    public IdListAdapter(int initialCapacity)
    {
        super(initialCapacity);
    }

    /**
     * Construct.
     * @param collection a collection
     */
    public IdListAdapter(Collection collection)
    {
        super(collection);
    }

    /**
     * Looks up the object with the given row, and call getIdValue with that object.
     * @see wicket.markup.html.form.IIdList#getIdValue(int)
     */
    public String getIdValue(int row)
    {
        return getIdValue(get(row));
    }

    /**
     * Attach to the current request.
     * @see wicket.markup.html.form.IIdList#attach(wicket.RequestCycle)
     */
    public final void attach(final RequestCycle cycle)
    {
        if (attached)
        {
            return;
        }

        doAttach(cycle);
        attached = true;
    }

    /**
     * Detach from the current request.
     * @see wicket.markup.html.form.IIdList#detach(wicket.RequestCycle)
     */
    public final void detach(final RequestCycle cycle)
    {
        if (!attached)
        {
            return;
        }

        doDetach(cycle);
        attached = false;
    }

    /**
     * Attach to the current request. Implement this method with custom behaviour, such
     * as loading the list of object you need for this list.
     * @param cycle the current request cycle
     */
    protected void doAttach(final RequestCycle cycle)
    {
    }

    /**
     * Detach from the current request. Implement this method with custom behaviour, such as
     * clearing the list.
     * @param cycle the current request cycle
     */
    protected void doDetach(final RequestCycle cycle)
    {
    }
}

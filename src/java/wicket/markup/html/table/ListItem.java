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
package wicket.markup.html.table;

import java.util.Collections;

import wicket.RequestCycle;
import wicket.markup.html.HtmlContainer;
import wicket.markup.html.link.Link;
import wicket.model.Model;


/**
 * Items of the ListView. 
 * 
 * @author Jonathan Locke
 */
public class ListItem extends HtmlContainer
{ // TODO finalize javadoc
	/** The index of the ListItem in the parent listView*/
    private final int index;

    /** The parent ListView, the ListItem is part of */
    private ListView listView;
    
    /**
     * A constructor which uses the index and the list provided to create a 
     * ListItem. This constructor is the default one.
     * 
     * @param listView The listView that holds this listItem
     * @param index The listItem number
     */
    protected ListItem(final int index, final ListView listView)
    {
        this(index, new Model(listView.getListObject(index)));

        this.listView = listView;
    }
    
    /**
     * This is a special constructor, which allows to create listItems without
     * an underlying listView. Paged table navigation bar is good example.
     * Be aware that some methods e.g. isLast() will throw an exception, because 
     * no underlying List is available.
     * 
     * @param index The listItem number
     * @param model The model object for the listItem
     */
    protected ListItem(final int index, final Model model)
    {
        super(Integer.toString(index), model);

        this.index = index;
        this.listView = null;
    }

    /**
     * Returns a link that will move the given listItem "up" (towards the 
     * beginning) in the listView.
     *
     * @param componentName Name of move-up link component to create
     * @return The link component
     */
    public final Link moveUpLink(final String componentName)
    {
        final Link link = new Link(componentName)
        {
			public void linkClicked(final RequestCycle cycle)
            {
                // Swap listItems and invalidate listView
                Collections.swap(listView.getList(), index, index - 1);
                listView.invalidateModel();
            }
        };

        if (index == 0)
        {
            link.setVisible(false);
        }

        return link;
    }

    /**
     * Returns a link that will move the given listItem "down" (towards 
     * the end) in the listView.
     * 
     * @param componentName Name of move-down link component to create
     * @return The link component
     */
    public final Link moveDownLink(final String componentName)
    {
        final Link link = new Link(componentName)
        {
			public void linkClicked(final RequestCycle cycle)
            {
                // Swap listeItem and invalidate listView
                Collections.swap(listView.getList(), index, index + 1);
                listView.invalidateModel();
            }
        };

        if (index == (listView.getList().size() - 1))
        {
            link.setVisible(false);
        }

        return link;
    }

    /**
     * Returns a link that will remove this listItem from the listView t
     * hat holds it.
     * 
     * @param componentName Name of remove link component to create
     * @return The link component
     */
    public final Link removeLink(final String componentName)
    {
        return new Link(componentName)
        {
			public void linkClicked(final RequestCycle cycle)
            {
                // Remove listItem and invalidate listView
			    listView.getList().remove(index);
			    listView.invalidateModel();
            }
        };
    }

    /**
     * Get the listView that holds this cell.
     * 
     * @return Returns the table.
     */
    protected final ListView getListView()
    {
        return listView;
    }

    /**
     * Gets the index of the listItem in the parent listView.
     * 
     * @return The index of this listItem in the parent listView
     */
    public final int getIndex()
    {
        return index;
    }

    /**
     * @return True if this listItem is the first listItem in the containing 
     * 	   listView
     */
    public final boolean isFirst()
    {
        return index == 0;
    }

    /**
     * @return True if this listItem is the last listItem in the containing listView.
     */
    public final boolean isLast()
    {
        int size = listView.getList().size();
        return ((size == 0) || (index == (size - 1)));
    }
    
    /**
     * Convinience method for ListViews with alternating style for colouring
     * 
     * @return True, if index is even ((index % 2) == 0)
     */
    public final boolean isEvenIndex()
    {
        return (getIndex() % 2) == 0;
    }
}

///////////////////////////////// End of File /////////////////////////////////

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

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import wicket.RenderException;
import wicket.RequestCycle;
import wicket.markup.MarkupStream;
import wicket.markup.html.HtmlContainer;
import wicket.model.IModel;


/**
 * A ListView holds ListItems of information. The listItem can be re-ordered 
 * and deleted, either one at a time or many at a time.
 * <p>
 * Example:
 * <pre>
 *  &lt;tbody&gt;
 *    &lt;tr id="wicket-rows" class="even"&gt;
 *        &lt;td&gt;&lt;span id="wicket-id"&gt;Test ID&lt;/span>&lt;/td&gt;
 *    ...    
 * </pre><p>
 * Though this example is about a HTML table, ListView is not at all limited
 * to HTML tables. Any kind of list can be rendered using ListView.
 * <p> 
 * And the related Java code:
 * <pre>
 *   add(new ListView("rows", listData)
 *      {
 *          public void populateItem(final ListItem item)
 *          {
 *              final UserDetails user = (UserDetails) item.getModelObject();
 *              cell.add(new Label("id", user.getId()));
 *          }
 *      });
 * </pre><p>
 * <p>Note:
 * Because Wicket model object must be seralizable, java.util.List.subList() 
 * can not be used for model object, as the List implementation return by 
 * subList() is protected and can not be subclassed. Which is why Wicket
 * implements subList functionality with ListView.
 * 
 * @author Jonathan Locke
 * @author Juergen Donnerstag
 */
public abstract class ListView extends HtmlContainer 
{
    /** Index of the first listItem to show */
    private int firstIndex = 0;
 
    /** max number (not index) of listItems to show */
    protected int viewSize = Integer.MAX_VALUE;
    
    /**
     * Creates a ListView that uses the provided {@link IModel}
     * as its model. All components have names. A component's name 
     * cannot be null. 
     * 
     * @param name The non-null name of this component
     * @param model
     * @throws RenderException Thrown if the component has been given a null name.
     */
    public ListView(String name, IModel model)
    {
        super(name, model);
    }

    /**
     * Creates a ListView that uses the provided instance of {@link IModel}as a
     * dynamic model. This model will be wrapped in an instance of 
     * {@link wicket.model.PropertyModel} using the provided expression. 
     * Thus, using this constructor is a short-hand for:
     * 
     * <pre>
     * new MyComponent(name, new PropertyModel(myIModel, expression));
     * </pre>
     * 
     * All components have names. A component's name cannot be null.
     * 
     * @param name The non-null name of this component
     * @param model The instance of {@link IModel}from which the model object 
     * 	          will be used as the subject for the given expression
     * @param expression the OGNL expression that works on the given object
     * @throws RenderException Thrown if the component has been given a null name.
     */
    public ListView(String name, IModel model, String expression)
    {
        super(name, model, expression);
    }

    /**
     * Creates a ListView that uses the provided object as a simple model. This
     * object will be wrapped in an instance of {@link wicket.model.Model}. 
     * All components have names. A component's name cannot be null.
     * 
     * @param name The non-null name of this component
     * @param object the object that will be used as a simple model
     * @throws RenderException Thrown if the component has been given a null name.
     */
    public ListView(String name, Serializable object)
    {
        super(name, object);
    }

    /**
     * Creates a ListView that uses the provided object as a dynamic model. 
     * This object will be wrapped in an instance of 
     * {@link wicket.model.Model} that will be wrapped in an
     * instance of {@link wicket.model.PropertyModel} using the 
     * provided expression. Thus, using this constructor is a short-hand for:
     * 
     * <pre>
     * new MyComponent(name, new PropertyModel(new Model(object), expression));
     * </pre>
     * 
     * All components have names. A component's name cannot be null.
     * 
     * @param name The non-null name of this component
     * @param object the object that will be used as the subject for the given expression
     * @param expression the OGNL expression that works on the given object
     * @throws RenderException Thrown if the component has been given a null name.
     */
    public ListView(String name, Serializable object, String expression)
    {
        super(name, object, expression);
    }

    /**
     * Creates a ListView that uses the provided object as a simple model. 
     * This object will be wrapped in an instance of 
     * {@link wicket.model.Model}. All components have names.
     * A component's name cannot be null.
     * 
     * @param name The non-null name of this component
     * @param object the object that will be used as a simple model
     * @throws RenderException Thrown if the component has been given a null name.
     */
    public ListView(String name, List object)
    {
        super(name, castToSerializable(object));
    }

    /**
     * Define the maximum number of listItems to render.
     * Default: render all. 
     *
     * @param size Number of listItems to display
     * @return This
     */
    public ListView setViewSize(final int size) 
    {
        this.viewSize = size;
        
        if (viewSize < 0)
        {
            viewSize = Integer.MAX_VALUE;
        }
        
        return this;
    }

    /**
     * Set the index of the first listItem to render
     * 
     * @param startIndex First index of model object's list to display
     * @return This
     */
    public ListView setStartIndex(final int startIndex) 
    {
        this.firstIndex = startIndex;
        
        if (firstIndex < 0)
        {
            firstIndex = 0;
        }
        
        return this;
    }
    
    /**
     * Cast to Serializable or throw an exception if the cast cannot be done.
     * @param object object to cast
     * @return casted object
     */
    protected static final Serializable castToSerializable(Object object)
    {
        if ((object != null) && (!(object instanceof Serializable)))
        {
            throw new RenderException("object must be of type Serializable");
        }

        return (Serializable) object;
    }

    /**
     * Indicates a structural change to the ListView model.
     */
    public void invalidateModel()
    {
        // Now that a structural change has been made to the model,
        // All pages using the same model should be expired
        super.invalidateModel();

        // Remove child listItems from listView since they came 
        // from the old and now invalid model
        removeAll();
    }

    /**
     * Based on the model object's list size, firstIndex and view size, 
     * determine what the view size really will be. E.g. default for
     * viewSize is Integer.MAX_VALUE, if not set via setViewSize(). If the
     * underlying list has 10 elements, the value returned by getViewSize()
     * will be 10 if startIndex = 0.
     * 
     * @return The number of listItems to be populated and rendered.
     */
    public int getViewSize()
    {
        int size = this.viewSize;

        // If model object (table) == null and viewSize has not been 
        // deliberately changed, than size = 0.
        Object modelObject = getModelObject();
        if((modelObject == null) && (viewSize == Integer.MAX_VALUE)) 
        {
            size = 0;
        }
        else if (modelObject instanceof List)
        {
            // Adjust view size to model object's list size
            final int modelSize = ((List)modelObject).size();
            if (firstIndex > modelSize)
            {
                return 0;
            }
            
            if ((size == Integer.MAX_VALUE) || ((firstIndex + size) > modelSize))
            {
                size = modelSize - firstIndex;
            }
        }
        
        // firstIndex + size must be smaller than Integer.MAX_VALUE
        if ((Integer.MAX_VALUE - size) <= firstIndex)
        {
            throw new IllegalArgumentException("firstIndex + size must be smaller than Integer.MAX_VALUE");
        }
        
        return size;
    }
    
    /**
     * Renders this ListView (container).
     * 
     * @param cycle The request cycle
     */
    protected void handleRender(final RequestCycle cycle)
    {
        // Ask parents for markup stream to use
        final MarkupStream markupStream = findMarkupStream();

        // Save position in markup stream
        final int markupStart = markupStream.getCurrentIndex();

        // Get number of listItems to be displayed
        int size = getViewSize();
        if (size > 0)
        {
	        // Loop through the markup in this container for each child container
	        for (int i = 0; i < size; i++)
	        {
	            int lastIndex = firstIndex + i;

	            // Get the name of the component for listItem i
	            final String componentName = Integer.toString(lastIndex);
	            
	            // If this component does not already exist, populate it
	            ListItem listItem = (ListItem) get(componentName);
	            if (listItem == null)
	            {
	                // Create listItem for index i of the list
	                listItem = newItem(lastIndex);
	                populateItem(listItem);
	
	                // Add cell to table
	                add(listItem);
	            }
	
	            // Rewind to start of markup for kids
	            markupStream.setCurrentIndex(markupStart);
	
	            // Render cell
	            renderItem(listItem, cycle, i >= (size - 1));
	        }
        }
        else
        {
            markupStream.skipComponent();
        }
    }

    /**
     * Render a single listItem.
     * 
     * @param listItem the listItem to be rendered
     * @param cycle The request cycle
     * @param lastItem True, if item is last listItem in listView
     */
    protected void renderItem(final ListItem listItem, final RequestCycle cycle, final boolean lastItem)
    {
        listItem.render(cycle);
    }
    
    /**
     * Creates a new listItem  for the given listItem index of this listView.
     * 
     * @param index ListItem index
     * @return The new ListItem
     */
    protected ListItem newItem(final int index)
    {
        Object model = getListObject(index);
        return new ListItem(index, this);
    }

    /**
     * Provide list object at index. May be subclassed for virtual list,
     * which don't implement List.
     * 
     * @param index The list object's index
     * @return the model list's object
     */
    protected Serializable getListObject(final int index)
    {
        Object object = getList().get(index);
        if ((object != null) && !(object instanceof Serializable))
        {
            throw new ClassCastException(
                    "ListView and ListItem model data must be serializable, index: "
                    + index + ", data: " + object); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return (Serializable) object;
    }
    
    /**
     * Populate a given listItem.
     * 
     * @param listItem The listItem to populate
     */
    protected abstract void populateItem(final ListItem listItem);

    /**
     * Gets the list of items in the listView.
     * 
     * @return Returns the list of items in this table.
     */
    public List getList()
    {
        List list = (List) getModelObject();
        return (list != null ? list : Collections.EMPTY_LIST);
    }
    
    /**
     * Get index of first cell in page. Default is: 0.
     * 
     * @return index of first cell in page. Default is: 0
     */
    public int getStartIndex()
    {
        return this.firstIndex;
    }
}

///////////////////////////////// End of File /////////////////////////////////

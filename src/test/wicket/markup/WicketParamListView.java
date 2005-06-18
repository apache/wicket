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
package wicket.markup;

import java.util.List;

import wicket.markup.html.list.ListView;
import wicket.util.string.StringValueConversionException;

/**
 * @author Juergen Donnerstag
 */
public abstract class WicketParamListView extends ListView
{
    /**
     * Construct.
     * @param id 
     * @param list
     */
    public WicketParamListView(final String id, final List list)
    {
        super(id, list);
    }

    /* (non-Javadoc)
	 * @see wicket.markup.html.list.ListView#onRender()
	 */
	protected void onRender()
	{
		final MarkupStream markupStream = findMarkupStream();
		
	    try
	    {
	        ComponentTag tag = (ComponentTag)markupStream.get();
	        this.setViewSize(tag.getAdditionalAttributes().getInt("rowsPerPage"));
	    }
	    catch (StringValueConversionException ex)
	    {
	        // ignore
	    }
	    
		// TODO Auto-generated method stub
		super.onRender();
	}
}

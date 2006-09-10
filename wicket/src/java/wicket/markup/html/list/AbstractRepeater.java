/*
 * $Id: org.eclipse.jdt.ui.prefs 5004 2006-03-17 20:47:08 -0800 (Fri, 17 Mar
 * 2006) eelco12 $ $Revision: 5004 $ $Date: 2006-03-17 20:47:08 -0800 (Fri, 17
 * Mar 2006) $
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
package wicket.markup.html.list;

import wicket.MarkupContainer;
import wicket.markup.IMarkup;
import wicket.markup.html.WebMarkupContainer;
import wicket.model.IModel;
import wicket.util.string.Strings;

/**
 * This is the base class for any component that wishes to act as a repeater.
 * 
 * A repeater passes its markup on to its direct children, in order for this to
 * work the direct child's markup path must be the same as the repeater's so
 * wicket thinks all direct children have the markup of the repeater.
 * 
 * @param <T>
 * 
 * @author ivaynberg
 * 
 */
public abstract class AbstractRepeater<T> extends WebMarkupContainer<T>
{

	/**
	 * Construct.
	 * 
	 * @param parent
	 * @param id
	 * @param model
	 */
	public AbstractRepeater(MarkupContainer parent, String id, IModel<T> model)
	{
		super(parent, id, model);
	}

	/**
	 * Construct.
	 * 
	 * @param parent
	 * @param id
	 */
	public AbstractRepeater(MarkupContainer parent, String id)
	{
		super(parent, id);
	}

	/**
	 * 
	 * @see wicket.MarkupContainer#getMarkupFragmentPath(java.lang.String)
	 */
	@Override
	public final String getMarkupFragmentPath(final String subPath)
	{
		/*
		 * we need to cut out the path of direct children because they inherit
		 * the markup so their markup path must look as if they are the listview
		 * themselves
		 * 
		 * page:listview:1:label -> page:listview:label
		 * 
		 * where 1 was the id of the listitem
		 */
		String path = Strings.afterFirst(subPath, IMarkup.TAG_PATH_SEPARATOR);
		return super.getMarkupFragmentPath(path);
	}
}

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
package wicket.markup.html.list;

import wicket.MarkupContainer;
import wicket.markup.MarkupFragment;
import wicket.markup.html.WebMarkupContainer;
import wicket.model.IModel;

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
	public final MarkupFragment getMarkupFragment(final String subPath)
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
		return getMarkupFragment();
	}
}

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
package wicket.markup.html.form;

import wicket.MarkupContainer;
import wicket.WicketRuntimeException;
import wicket.markup.ComponentTag;
import wicket.markup.html.WebMarkupContainer;

/**
 * Selects and unselects all Check components under the same CheckGroup as
 * itself. Selection toggling is accomplished by generating an onclick
 * javascript event handler.
 * 
 * @param <T>
 *            The type
 * 
 * @see wicket.markup.html.form.CheckGroup
 * @see wicket.markup.html.form.Check
 * 
 * @author Igor Vaynberg (ivaynberg@users.sf.net)
 * 
 */
public class CheckGroupSelector<T> extends WebMarkupContainer<T>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	/**
	 * @see WebMarkupContainer#WebMarkupContainer(MarkupContainer,String)
	 */
	public CheckGroupSelector(MarkupContainer parent, String id)
	{
		super(parent, id);
	}

	@Override
	protected void onComponentTag(ComponentTag tag)
	{
		// must be attached to <input type="checkbox" .../> tag
		checkComponentTag(tag, "input");
		checkComponentTagAttribute(tag, "type", "checkbox");

		CheckGroup group = findParent(CheckGroup.class);
		if (group == null)
		{
			throw new WicketRuntimeException(
					"CheckGroupSelector component ["
							+ getPath()
							+ "] cannot find its parent CheckGroup. All CheckGroupSelector components must be a child of or below in the hierarchy of a CheckGroup component.");
		}

		tag
				.put(
						"onclick",
						"var cb=this.form['"
								+ group.getInputName()
								+ "']; if (cb!=null) { if (!isNaN(cb.length)) { for(var i=0;i<cb.length;i++) { cb[i].checked=this.checked; } } else { cb.checked=this.checked; } }");

		super.onComponentTag(tag);
	}


}

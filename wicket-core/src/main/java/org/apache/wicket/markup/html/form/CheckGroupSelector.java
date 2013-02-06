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
package org.apache.wicket.markup.html.form;

import org.apache.wicket.WicketRuntimeException;

/**
 * Selects and deselects all Check components under the same CheckGroup as itself. Selection
 * toggling is accomplished by generating an onclick javascript event handler. This component must
 * be attached to markup of type &lt;input type="checkbox"/&gt;. Additionally, if
 * {@link #CheckGroupSelector(String)} constuctor is used the selector must be a descendant of the
 * CheckGroup it is meant to affect.
 * 
 * @see org.apache.wicket.markup.html.form.CheckGroup
 * @see org.apache.wicket.markup.html.form.Check
 * 
 * @author Igor Vaynberg
 * @author Carl-Eric Menzel
 */
public class CheckGroupSelector extends AbstractCheckSelector
{
	/** */
	private static final long serialVersionUID = 1L;

	private CheckGroup<?> group;

	/**
	 * A Selector that will look for a {@link CheckGroup} in its parent hierarchy.
	 * 
	 * @param id
	 *            component id
	 */
	public CheckGroupSelector(String id)
	{
		this(id, null);
	}

	/**
	 * A Selector that will work with the given group.
	 * 
	 * @param id
	 *            component id
	 * @param group
	 *            group to work with
	 */
	public CheckGroupSelector(String id, CheckGroup<?> group)
	{
		super(id);

		this.group = group;
	}

	private CheckGroup<?> getGroup()
	{
		CheckGroup<?> group = this.group;
		if (group == null)
		{
			group = findParent(CheckGroup.class);
			this.group = group;
		}
		return group;
	}

	@Override
	protected void onBeforeRender()
	{
		super.onBeforeRender();

		CheckGroup<?> group = getGroup();

		// make sure the form we need outputs its markup id.
		group.getForm().setOutputMarkupId(true);
	}

	@Override
	public boolean isEnabled()
	{
		CheckGroup<?> group = getGroup();
		if (group == null)
		{
			return true;
		}
		else
		{
			return group.isEnableAllowed() && group.isEnabledInHierarchy();
		}
	}

	/**
	 * Find all checkboxes in the containing form with the same input name as the {@link CheckGroup}
	 * .
	 */
	@Override
	protected CharSequence getFindCheckboxesFunction()
	{
		CheckGroup<?> group = getGroup();
		if (group == null)
		{
			throw new WicketRuntimeException(
				"CheckGroupSelector component [" +
					getPath() +
					"] cannot find its parent CheckGroup. All CheckGroupSelector components must be a child of or below in the hierarchy of a CheckGroup component.");
		}

		// we search the complete form because the CheckGroup might not output its markup tag or be
		// located on a <wicket:container>
		return String.format("Wicket.CheckboxSelector.findCheckboxesFunction('%s','%s')",
			group.getForm().getMarkupId(), group.getInputName());
	}
}

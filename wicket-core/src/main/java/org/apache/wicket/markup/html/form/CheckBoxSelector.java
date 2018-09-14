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

import java.util.HashSet;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;


/**
 * A Javascript-based "Select All" checkbox component that works with a loose collection of
 * {@link CheckBox} components. By default, clicking on any of the controlled checkboxes
 * automatically updates the state of the "select all" checkbox. Override
 * {@link AbstractCheckSelector#wantAutomaticUpdate()} to change this.
 * 
 * @author Carl-Eric Menzel <cmenzel@wicketbuch.de>
 */
public class CheckBoxSelector extends AbstractCheckSelector
{
	private static final long serialVersionUID = 1L;

	private final Set<CheckBox> connectedCheckBoxes = new HashSet<>();
	private final Behavior cleanup = new Behavior()
	{
		@Override
		public void onRemove(Component component)
		{
			connectedCheckBoxes.remove(component);
			component.remove(this);
		}
	};

	/**
	 * @param id
	 *            The component ID
	 * @param boxes
	 *            The CheckBoxes this selector will control.
	 */
	public CheckBoxSelector(String id, CheckBox... boxes)
	{
		super(id);
		addCheckbox(boxes);
	}

	public void addCheckbox(CheckBox... boxes)
	{
		for (CheckBox box : boxes)
		{
			connectedCheckBoxes.add(box);
			box.setOutputMarkupId(true);
			box.add(cleanup);
		}
	}

	@Override
	protected CharSequence getFindCheckboxesFunction()
	{
		return String.format("Wicket.CheckboxSelector.getCheckboxesFunction(%s)",
			buildMarkupIdJSArrayLiteral(connectedCheckBoxes));
	}

	/**
	 * Builds a JavaScript array literal containing the markup IDs of the given components. Example:
	 * "['foo', 'bar', 'baz']".
	 * 
	 * @param components
	 *            The components whose IDs we need
	 * @return a properly formatted JS array literal
	 */
	private String buildMarkupIdJSArrayLiteral(final Iterable<CheckBox> components)
	{
		StringBuilder buf = new StringBuilder();
		buf.append('[');
		if (components.iterator().hasNext())
		{
			for (Component component : components)
			{
				buf.append('\'').append(component.getMarkupId()).append("', ");
			}
			buf.delete(buf.length() - 2, buf.length());
		}
		buf.append(']');
		return buf.toString();
	}

}

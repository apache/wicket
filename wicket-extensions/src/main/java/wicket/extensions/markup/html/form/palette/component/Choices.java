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
package wicket.extensions.markup.html.form.palette.component;

import java.util.Iterator;

import wicket.MarkupContainer;
import wicket.extensions.markup.html.form.palette.Palette;
import wicket.markup.ComponentTag;
import wicket.util.value.IValueMap;

/**
 * select box containg all available choices of the palette
 * 
 * @author Igor Vaynberg ( ivaynberg )
 * 
 */
public class Choices extends AbstractOptions
{
	private static final long serialVersionUID = 1L;

	/**
	 * @param parent
	 *            The parent of this component The parent of this component.
	 * @param id
	 *            component id
	 * @param palette
	 *            parent palette
	 */
	public Choices(MarkupContainer parent, final String id, Palette palette)
	{
		super(parent, id, palette);
	}

	@Override
	protected void onComponentTag(ComponentTag tag)
	{
		super.onComponentTag(tag);
		IValueMap attrs = tag.getAttributes();
		String onFocus = getPalette().getChoicesOnFocusJS();
		if (onFocus != null)
		{
			attrs.put("onFocus", onFocus);
		}

		tag.getAttributes().put("ondblclick", getPalette().getAddOnClickJS());
	}

	@Override
	protected Iterator getOptionsIterator()
	{
		return getPalette().getUnselectedChoices();
	}
}

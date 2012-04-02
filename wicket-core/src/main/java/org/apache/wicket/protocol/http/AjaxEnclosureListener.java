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
package org.apache.wicket.protocol.http;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.internal.InlineEnclosure;
import org.apache.wicket.markup.parser.filter.InlineEnclosureHandler;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;

/**
 * This listener adds Enclosures to AjaxTarget, where the child controller of the said Enclosure is
 * already added. This is a part of the fix to the problem:
 * 
 * "Changing the visibility of a child component in Ajax callback method will not affect the entire
 * enclosure but just the child component itself. This is because only the child component is added
 * to the AjaxRequestTarget"
 * 
 * When used with an "Inline" Enclosure, this problem is fixed.
 * 
 * Syntax for inline enclosure:
 * 
 * <tr wicket:enclosure="controllingChildId">
 * 
 * In this example the tag used is tr, but any other tag could be used as well. The attribute
 * "wicket:enclosure" is mandatory, and is used to recognise an inline enclosure. The value of the
 * attribute, here "controllingChildId" can contain an id for the child (controller) element. If the
 * said value is not given, the first element inside the enclosure will be used as a controller
 * child. If there are no elements inside the enclosure, the parsing will fail.
 * 
 * 
 * @see WebApplication
 * @see InlineEnclosure
 * @see InlineEnclosureHandler
 *
 * @author Joonas Hamalainen
 */
public class AjaxEnclosureListener implements AjaxRequestTarget.IListener
{
	/**
	 * Construct.
	 */
	public AjaxEnclosureListener()
	{
	}

	/**
	 * Try to find Enclosures that have their controllers added already, and add them to the target.
	 */
	@Override
	public void onBeforeRespond(final Map<String, Component> map, final AjaxRequestTarget target)
	{
		// We need to iterate over the map, but the map changes if we add an
		// InlineEnclosure to the target. --> make a copy of the map and iterate that instead.
		final List<Component> originalComponents = Collections.unmodifiableList(new ArrayList<Component>(
			map.values()));

		target.getPage().visitChildren(InlineEnclosure.class, new IVisitor<InlineEnclosure, Void>()
		{
			@Override
			public void component(final InlineEnclosure enclosure, final IVisit<Void> visit)
			{
				for (Component component : originalComponents)
				{
					if (isControllerOfEnclosure(component, enclosure))
					{
						// update the visibility of the enclosure
						enclosure.updateVisibility();

						// add enclosure to Ajax target
						target.add(enclosure);
					}
				}
			}
		});
	}

	/**
	 * Check if a given component is the controlling child of a given enclosure
	 * 
	 * @param component
	 * @param enclosure
	 * @return true if the given component is the controlling child of the given InlineEnclosure
	 */
	private boolean isControllerOfEnclosure(final Component component,
		final InlineEnclosure enclosure)
	{
		return enclosure.getParent().get(enclosure.getChildId()) == component;
	}

	@Override
	public void onAfterRespond(final Map<String, Component> map, final AjaxRequestTarget.IJavaScriptResponse response)
	{
	}
}

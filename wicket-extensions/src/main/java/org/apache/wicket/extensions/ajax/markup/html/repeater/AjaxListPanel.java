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
package org.apache.wicket.extensions.ajax.markup.html.repeater;

import java.util.EmptyStackException;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.core.util.string.JavaScriptUtils;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.IMarkupFragment;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.parser.XmlTag.TagType;
import org.apache.wicket.markup.repeater.RepeatingView;

/**
 * An panel for an <it>Ajax-ified</it> list of components.
 * <p>
 * Allows to dynamically append and delete components without an update of a whole container.
 * 
 * @see #append(Component, AjaxRequestTarget)
 * @see #delete(Component, AjaxRequestTarget)
 * 
 * @author svenmeier
 */
public class AjaxListPanel extends Panel
{

	private static final long serialVersionUID = 1L;
	
	private WebMarkupContainer container;

	private RepeatingView repeater;

	public AjaxListPanel(String id)
	{
		super(id);

		this.container = new WebMarkupContainer("container");
		this.container.setOutputMarkupId(true);
		add(this.container);

		this.repeater = new RepeatingView("repeater");
		this.container.add(this.repeater);
	}

	/**
	 * Get an id for a new child to be appended.
	 * 
	 * @return id
	 * 
	 * @see #append(Component, AjaxRequestTarget)
	 */
	public String newChildId() {
		return repeater.newChildId();
	}
	
	/**
	 * Append a component.
	 * 
	 * @param component
	 *            the component
	 * @param target
	 *            optional target
	 * @return the component
	 * 
	 * @param <T> component type
	 */
	public <T extends Component> T append(T component, AjaxRequestTarget target)
	{
		this.repeater.add(component);
		
		if (target != null)
		{
			IMarkupFragment markup = repeater.getMarkup();
			
			// append markup to be updated
			MarkupStream stream = new MarkupStream(markup);
			ComponentTag tag = stream.getTag().mutable();
			tag.getXmlTag().setType(TagType.OPEN_CLOSE);
			tag.getXmlTag().put("id", component.getMarkupId());

			target.prependJavaScript(String.format("Wicket.DOM.add(Wicket.DOM.get('%s'), '%s');",
				container.getMarkupId(), JavaScriptUtils.escapeQuotes(tag.toString())));
			
			// ... then update the appended component 
			target.add(component);
		}

		return component;
	}
	
	/**
	 * Delete a component.
	 * 
	 * @param target
	 *            optional target
	 * @return the component
	 * @throws EmptyStackException if empty
	 * 
	 * @param <T> component type
	 */
	public <T extends Component> T delete(T component, AjaxRequestTarget target) {

		this.repeater.remove(component);
		if (target != null)
		{
			target.appendJavaScript(String.format("Wicket.DOM.remove(Wicket.DOM.get('%s'));", component.getMarkupId()));
		}
		
		return (T)component;
	}
}

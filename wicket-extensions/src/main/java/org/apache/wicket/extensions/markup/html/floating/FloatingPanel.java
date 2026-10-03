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
package org.apache.wicket.extensions.markup.html.floating;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.lang.Args;

/**
 * A window with a title bar and a body, for content floating on top of a page or a component, for
 * example a form editing a row in the
 * {@link org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.dynamic.DynamicDataTable#showOverlay(Component, org.apache.wicket.core.request.handler.IPartialPageRequestHandler)
 * overlay of a DynamicDataTable}.
 * <p>
 * The title bar shows the title, escaped, and a button closing the panel, which calls
 * {@link #onClose(AjaxRequestTarget)}; {@link #isClosable()} hides it. The body is the component
 * returned by {@link #newBody(String)}. The panel needs no inline script or style, so it works
 * under a strict Content Security Policy. The look comes from {@link #CSS}.
 *
 * @since 11.0.0
 */
public abstract class FloatingPanel extends Panel
{
	private static final long serialVersionUID = 1L;

	/** The style sheet of the panel. */
	public static final ResourceReference CSS = new CssResourceReference(FloatingPanel.class,
		"wicket-floating-panel.css");

	/** The id the component returned by {@link #newBody(String)} has to carry. */
	public static final String BODY_ID = "body";

	private final IModel<String> title;

	/**
	 * @param id
	 *            component id
	 * @param title
	 *            the title, written escaped
	 */
	public FloatingPanel(String id, IModel<String> title)
	{
		super(id);
		this.title = Args.notNull(title, "title");
	}

	@Override
	protected void onInitialize()
	{
		super.onInitialize();
		add(new Label("title", title));
		add(new AjaxLink<Void>("close")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onConfigure()
			{
				super.onConfigure();
				setVisible(isClosable());
			}

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				onClose(target);
			}
		}.add(new LabelAttribute()));
		add(newBody(BODY_ID));
	}

	/**
	 * @param id
	 *            the id the body has to carry, {@value #BODY_ID}
	 * @return the body of the panel, for example a form
	 */
	protected abstract Component newBody(String id);

	/**
	 * @return whether the title bar shows a button closing the panel, {@code true} by default
	 */
	protected boolean isClosable()
	{
		return true;
	}

	/**
	 * Called when the user clicks the close button of the title bar. Does nothing by default;
	 * override to remove or hide the panel.
	 *
	 * @param target
	 *            the Ajax request target
	 */
	protected void onClose(AjaxRequestTarget target)
	{
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(CSS));
	}

	@Override
	protected void onDetach()
	{
		title.detach();
		super.onDetach();
	}

	/**
	 * Names the close button for screen readers and as its tooltip, from the resource
	 * {@code FloatingPanel.close}.
	 */
	private static class LabelAttribute extends Behavior
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void onComponentTag(Component component, ComponentTag tag)
		{
			String label = new ResourceModel("FloatingPanel.close", "Close").wrapOnAssignment(
				component).getObject();
			tag.put("aria-label", label);
			tag.put("title", label);
		}
	}
}

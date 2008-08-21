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
package org.apache.wicket.extensions.ajax.markup.html;

import org.apache.wicket.Component;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

/**
 * A panel where you can lazy load another panel. This can be used if you have a panel/component
 * that is pretty heavy in creation and you first want to show the user the page and the replace the
 * panel when it is ready.
 * 
 * @author jcompagner
 * 
 * @since 1.3
 */
public abstract class AjaxLazyLoadPanel extends Panel
{
	private static final long serialVersionUID = 1L;

	/**
	 * @param id
	 */
	public AjaxLazyLoadPanel(String id)
	{
		this(id, null);
	}

	/**
	 * @param id
	 * @param model
	 */
	public AjaxLazyLoadPanel(String id, IModel<?> model)
	{
		super(id, model);
		setOutputMarkupId(true);
		final Component loadingComponent = getLoadingComponent("content");
		add(loadingComponent.setRenderBodyOnly(true));

		add(new AbstractDefaultAjaxBehavior()
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void respond(AjaxRequestTarget target)
			{
				Component component = getLazyLoadComponent("content");
				AjaxLazyLoadPanel.this.replace(component.setRenderBodyOnly(true));
				target.addComponent(AjaxLazyLoadPanel.this);
			}

			@Override
			public void renderHead(IHeaderResponse response)
			{
				super.renderHead(response);
				response.renderOnDomReadyJavascript(getCallbackScript().toString());
			}

			@Override
			public boolean isEnabled(Component component)
			{
				return get("content") == loadingComponent;
			}
		});
	}

	/**
	 * @param markupId
	 *            The components markupid.
	 * @return The component that must be lazy created.
	 */
	public abstract Component getLazyLoadComponent(String markupId);

	/**
	 * @param markupId
	 *            The components markupid.
	 * @return The component to show while the real component is being created.
	 */
	public Component getLoadingComponent(String markupId)
	{
		return new Label(markupId, "<img src=\"" +
			RequestCycle.get().urlFor(AbstractDefaultAjaxBehavior.INDICATOR) + "\"/>").setEscapeModelStrings(false);
	}

}

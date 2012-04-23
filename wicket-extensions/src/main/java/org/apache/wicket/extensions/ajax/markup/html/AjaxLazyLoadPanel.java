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
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;

/**
 * A panel where you can lazy load another panel. This can be used if you have a panel/component
 * that is pretty heavy in creation and you first want to show the user the page and then replace
 * the panel when it is ready.
 * 
 * @author jcompagner
 * 
 * @since 1.3
 */
public abstract class AjaxLazyLoadPanel extends Panel
{
	private static final long serialVersionUID = 1L;

	/**
	 * The component id which will be used to load the lazily loaded component.
	 */
	public static final String LAZY_LOAD_COMPONENT_ID = "content";

	// state,
	// 0:add loading component
	// 1:loading component added, waiting for ajax replace
	// 2:ajax replacement completed
	private byte state = 0;

	/**
	 * Constructor
	 * 
	 * @param id
	 */
	public AjaxLazyLoadPanel(final String id)
	{
		this(id, null);
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 * @param model
	 */
	public AjaxLazyLoadPanel(final String id, final IModel<?> model)
	{
		super(id, model);

		setOutputMarkupId(true);

		add(new AbstractDefaultAjaxBehavior()
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void respond(final AjaxRequestTarget target)
			{
				if (state < 2)
				{
					Component component = getLazyLoadComponent(LAZY_LOAD_COMPONENT_ID);
					AjaxLazyLoadPanel.this.replace(component);
					setState((byte)2);
				}
				target.add(AjaxLazyLoadPanel.this);

			}

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
			{
				super.updateAjaxAttributes(attributes);
				AjaxLazyLoadPanel.this.updateAjaxAttributes(attributes);
			}

			@Override
			public void renderHead(final Component component, final IHeaderResponse response)
			{
				super.renderHead(component, response);
				if (state < 2)
				{
					CharSequence js = getCallbackScript(component);
					handleCallbackScript(response, js, component);
				}
			}
		});
	}

	protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
	{
	}

	/**
	 * Allows subclasses to change the callback script if needed.
	 * 
	 * @param response
	 * @param callbackScript
	 * @param component
	 */
	protected void handleCallbackScript(final IHeaderResponse response,
		final CharSequence callbackScript, final Component component)
	{
		response.render(JavaScriptHeaderItem.forScript(callbackScript,
				String.format("lazy-load-%s-%d", component.getMarkupId(), component.getPage().getAutoIndex())));
	}

	/**
	 * @see org.apache.wicket.Component#onBeforeRender()
	 */
	@Override
	protected void onBeforeRender()
	{
		if (state == 0)
		{
			add(getLoadingComponent(LAZY_LOAD_COMPONENT_ID));
			setState((byte)1);
		}
		super.onBeforeRender();
	}

	/**
	 * 
	 * @param state
	 */
	private void setState(final byte state)
	{
		this.state = state;
		getPage().dirty();
	}

	/**
	 * 
	 * @param markupId
	 *            The components markupid.
	 * @return The component that must be lazy created. You may call setRenderBodyOnly(true) on this
	 *         component if you need the body only.
	 */
	public abstract Component getLazyLoadComponent(String markupId);

	/**
	 * @param markupId
	 *            The components markupid.
	 * @return The component to show while the real component is being created.
	 */
	public Component getLoadingComponent(final String markupId)
	{
		IRequestHandler handler = new ResourceReferenceRequestHandler(
			AbstractDefaultAjaxBehavior.INDICATOR);
		return new Label(markupId, "<img alt=\"Loading...\" src=\"" +
			RequestCycle.get().urlFor(handler) + "\"/>").setEscapeModelStrings(false);
	}

}

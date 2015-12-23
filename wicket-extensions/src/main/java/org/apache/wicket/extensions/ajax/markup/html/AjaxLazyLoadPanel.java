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
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache.wicket.util.time.Duration;

/**
 * A panel where you can lazily load another component. This can be used if you have a
 * panel/component that is pretty heavy in creation and you first want to show the user the page and
 * then replace the panel when it is ready.
 * 
 * This panel will not block the application while waiting for the replacement, provided you
 * overwrite {@link #isReadyForReplacement()} to return false, until the replacement is ready.
 * 
 * This panel will wait with replacing the component until {@link #isReadyForReplacement()} returns
 * {@code true}. It will poll using an AJAX timer behavior that is installed on the page. When the
 * component is replaced, the timer stops. When you have multiple {@code AjaxLazyLoadPanel}s on the
 * same page, only one timer is used and all panels piggyback on this single timer.
 * 
 * This component will also replace the contents when a normal request comes through and the
 * component is ready for replacement.
 * 
 * @since 1.3
 */
public abstract class AjaxLazyLoadPanel extends Panel
{
	private static final long serialVersionUID = 1L;

	/** Key used to store the timer in the page. */
	private static final MetaDataKey<AjaxLazyLoadTimer> AJAX_LAZY_LOAD_TIMER = new MetaDataKey<AjaxLazyLoadTimer>()
	{
		private static final long serialVersionUID = 1L;
	};

	/**
	 * The component id which will be used to load the lazily loaded component.
	 */
	public static final String LAZY_LOAD_COMPONENT_ID = "content";

	/**
	 * States for this panel for replacing the spinner with a component.
	 */
	private enum LoadingState {
		/** Initial state, not ready to replace the spinner */
		WAITING_FOR_LAZY_COMPONENT,

		/** Second state, ready to replace the spinner with the lazy component */
		REPLACING_LAZY_COMPONENT,

		/** Final state, done with replacing the spinner and cleaning up the timer */
		REPLACEMENT_COMPLETED;
	}

	private LoadingState state = LoadingState.WAITING_FOR_LAZY_COMPONENT;

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
	}

	@Override
	protected void onInitialize()
	{
		super.onInitialize();

		// when the timer is not yet installed add it
		AjaxLazyLoadTimer timer = getPage().getMetaData(AJAX_LAZY_LOAD_TIMER);
		if (timer == null)
		{
			timer = new AjaxLazyLoadTimer();
			getPage().setMetaData(AJAX_LAZY_LOAD_TIMER, timer);
			getPage().add(timer);
		}

		// and register this panel with the timer
		timer.addLazyLoadPanel();
	}

	@Override
	public void onEvent(IEvent<?> event)
	{
		super.onEvent(event);

		if (state == LoadingState.REPLACING_LAZY_COMPONENT)
		{
			if (isReadyForReplacement())
			{
				// create the lazy load component
				Component component = getLazyLoadComponent(LAZY_LOAD_COMPONENT_ID);

				// replace the spinner with the new component
				AjaxLazyLoadPanel.this.replace(component);

				// mark replacement as done
				setState(LoadingState.REPLACEMENT_COMPLETED);

				// remove ourselves from the timer
				getPage().getMetaData(AJAX_LAZY_LOAD_TIMER).removeLazyLoadPanel();

				AjaxRequestTarget target = getRequestCycle().find(AjaxRequestTarget.class);

				// notify our subclasses of the updated component
				onComponentLoaded(component, target);

				// repaint our selves if there's an ajax request in play, otherwise let the page
				// redraw itself
				if (target != null)
				{
					target.add(AjaxLazyLoadPanel.this);
				}
			}
		}
	}

	protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
	{
	}

	/**
	 * Allows subclasses to change the callback script if needed.
	 * 
	 * @param response
	 *            the current response that writes to the header
	 * @param callbackScript
	 *            the JavaScript to write in the header
	 * @param component
	 *            the component which produced the callback script
	 */
	protected void handleCallbackScript(final IHeaderResponse response,
		final CharSequence callbackScript, final Component component)
	{
		response.render(OnDomReadyHeaderItem.forScript(callbackScript));
	}

	/**
	 * @see org.apache.wicket.Component#onBeforeRender()
	 */
	@Override
	protected void onBeforeRender()
	{
		if (state == LoadingState.WAITING_FOR_LAZY_COMPONENT)
		{
			add(getLoadingComponent(LAZY_LOAD_COMPONENT_ID));
			setState(LoadingState.REPLACING_LAZY_COMPONENT);
		}
		super.onBeforeRender();
	}

	/**
	 * Determines that the component we're waiting for is ready for replacement, typically used in
	 * polling background threads for their result. Override this to implement your own check.
	 * 
	 * @return true when the LazyLoadPanel should replace its contents with the actual component
	 */
	protected boolean isReadyForReplacement()
	{
		return true;
	}

	/**
	 * 
	 * @param newState
	 */
	private void setState(final LoadingState newState)
	{
		this.state = newState;
		getPage().dirty();
	}

	/**
	 * Called when the placeholder component is replaced with the lazy loaded one.
	 *
	 * @param component
	 *            The lazy loaded component
	 * @param target
	 *            The Ajax request handler, can be null
	 */
	protected void onComponentLoaded(Component component, AjaxRequestTarget target)
	{
	}

	/**
	 * Gets the spinner component shown when the lazy component is not ready yet.
	 * 
	 * @param markupId
	 *            The components markupid.
	 * @return The component to show while the real component is being created.
	 */
	public Component getLoadingComponent(final String markupId)
	{
		IRequestHandler handler = new ResourceReferenceRequestHandler(
			AbstractDefaultAjaxBehavior.INDICATOR);
		return new Label(markupId,
			"<img alt=\"Loading...\" src=\"" + RequestCycle.get().urlFor(handler) + "\"/>")
				.setEscapeModelStrings(false);
	}

	/**
	 * Factory method for creating the lazily loaded component that replaces the spinner after
	 * {@link #isReadyForReplacement()} returns {@code true}. You may call setRenderBodyOnly(true)
	 * on this component if you need the body only.
	 * 
	 * @param markupId
	 *            The components markupid.
	 * @return The component that must be lazy created.
	 */
	public abstract Component getLazyLoadComponent(String markupId);

	/**
	 * The AJAX timer for updating the AjaxLazyLoadPanel. Is designed to be a page-local singleton
	 * keeping track of multiple LazyLoadPanels using reference counting.
	 */
	private class AjaxLazyLoadTimer extends AbstractAjaxTimerBehavior
	{
		private static final long serialVersionUID = 1L;

		private int lazyLoadPanels = 0;

		public AjaxLazyLoadTimer()
		{
			super(Duration.ONE_SECOND);
		}

		public void addLazyLoadPanel()
		{
			lazyLoadPanels++;
		}

		public void removeLazyLoadPanel()
		{
			lazyLoadPanels--;
		}

		@Override
		protected void onTimer(AjaxRequestTarget target)
		{
			if (lazyLoadPanels <= 0)
			{
				stop(target);
				getPage().remove(this);
				getPage().setMetaData(AJAX_LAZY_LOAD_TIMER, null);
			}
		}
	}
}
